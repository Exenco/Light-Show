package net.exenco.lightshow.show.artnet;

import net.exenco.lightshow.LightShow;
import org.bukkit.scheduler.BukkitRunnable;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArtNetClient {
    private final LightShow lightShow;
    private final Logger logger;

    private volatile boolean connected = false;
    private SocketReader socketReader;
    private final ArtNetBuffer artNetBuffer;
    private DatagramSocket datagramSocket;

    private Cipher cipher;
    private boolean server;

    public ArtNetClient(LightShow lightShow) {
        this.lightShow = lightShow;
        this.logger = lightShow.getLogger();

        this.artNetBuffer = new ArtNetBuffer();
    }

    public int start(boolean server, String networkInterfaceAddress, int port, String key, String iv) {
        if(socketReader != null || datagramSocket != null) {
            logger.severe("Art-Net has already been started.");
            return 0;
        }
        this.server = server;
        try {
            InetAddress inetAddress = InetAddress.getByName(networkInterfaceAddress);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);

            artNetBuffer.clear();
            if(server)
                datagramSocket = new DatagramSocket(port);
            else
                datagramSocket = new DatagramSocket(inetSocketAddress);
            datagramSocket.setReuseAddress(true);
            datagramSocket.setBroadcast(true);

            this.cipher = Cipher.getInstance("AES/CFB/NoPadding");
            SecretKey secretKey = getKeyFromPassword(key);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, generateIv(iv));

            socketReader = new SocketReader();
            socketReader.runTaskAsynchronously(lightShow);
            logger.info("Art-Net started at: " + networkInterfaceAddress + ":" + port);

            return 1;
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException | InvalidKeyException | InvalidAlgorithmParameterException e){
            logger.log(Level.SEVERE, "There has been an error starting Art-Net.", e.getCause());
            e.printStackTrace();
            return -1;
        }
    }

    public boolean stop() {
        if(socketReader == null || datagramSocket == null)
            return false;

        datagramSocket.close();
        datagramSocket = null;
        socketReader.cancel();
        socketReader = null;
        logger.info("Stopped Art-Net.");
        return true;
    }

    private ArtNetPacket parse(byte[] raw) {
        ArtNetPacket packet = null;
        if (raw.length <= 10)
            return null;
        if(server) {
            try {
                raw = cipher.doFinal(raw);
            } catch(IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
        }
        if(!validHeader(raw))
            return null;

        int opCode = ByteBuffer.wrap(raw, 8, 16).order(ByteOrder.LITTLE_ENDIAN).getShort();
        if(opCode == 0x5000) {
            packet = new ArtNetPacket();
            packet.parse(raw);
        }
        return packet;
    }

    private boolean validHeader(byte[] data) {
        boolean isEqual = true;
        for(int i = 0; i < ArtNetPacket.PACKET_HEADER.length && isEqual; i++) {
            isEqual = data[i] == ArtNetPacket.PACKET_HEADER[i];
        }
        return isEqual;
    }

    public byte[] readDmx(int universeId) {
        return artNetBuffer.getDmxData(universeId);
    }

    private class SocketReader extends BukkitRunnable {
        private boolean isRunning = false;
        @Override
        public void run() {
            isRunning = true;
            byte[] buffer = new byte[2048];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                while(isRunning) {
                    datagramSocket.receive(receivedPacket);
                    ArtNetPacket packet = parse(receivedPacket.getData());
                    if(packet == null)
                        continue;
                    artNetBuffer.setDmxData(packet.getUniverseID(), packet.getDmx());
                    connected = true;
                }
                datagramSocket.close();
            } catch (IOException e) {
                if(isRunning)
                    e.printStackTrace();
            }
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            isRunning = false;
            super.cancel();
        }
    }

    public boolean confirmConnected(int timeout) {
        this.connected = false;
        long start = System.currentTimeMillis();
        long current = start;
        while(!connected) {
            Thread.onSpinWait();
            if(current >= start + timeout) {
                break;
            }
            current = System.currentTimeMillis();
        }
        return connected;
    }

    private SecretKey getKeyFromPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), (password.hashCode() + "").getBytes(), 65536, 128);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private IvParameterSpec generateIv(String iv) {
        byte[] ivArr = new byte[16];
        System.arraycopy(iv.getBytes(), 0, ivArr, 0, Math.min(16, iv.getBytes().length));
        return new IvParameterSpec(ivArr);
    }
}
