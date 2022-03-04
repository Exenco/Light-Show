package net.exenco.lightshow.show.artnet;

import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.show.stage.StageManager;
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
import java.util.logging.Logger;

public class ArtNetClient {
    /* Behavioral variables */
    private final LightShow lightShow;
    private final Logger logger;
    private DatagramSocket datagramSocket;
    private ArtNetReader artNetReader;

    /* Connection variables */
    private boolean server;
    private Cipher cipher;
    private boolean receiving;

    /* Interface variables */
    private final StageManager stageManager;
    private final ArtNetBuffer artNetBuffer;

    public ArtNetClient(StageManager stageManager) {
        this.lightShow = stageManager.getLightShow();
        this.logger = lightShow.getLogger();
        this.stageManager = stageManager;
        this.artNetBuffer = new ArtNetBuffer();
    }

    private void initializeClient(String networkInterfaceAddress, int port) throws UnknownHostException, SocketException {
        this.server = false;
        InetAddress inetAddress = InetAddress.getByName(networkInterfaceAddress);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);
        datagramSocket = new DatagramSocket(inetSocketAddress);
        datagramSocket.setReuseAddress(true);
        datagramSocket.setBroadcast(true);
        datagramSocket.setSoTimeout(1);
    }

    private void initializeServer(int port, String key, String iv) throws SocketException {
        this.server = true;
        try {
            this.cipher = Cipher.getInstance("AES/CFB/NoPadding");
            SecretKey secretKey = getKeyFromPassword(key);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, generateIv(iv));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException | InvalidKeyException e) {
            logger.severe("Cannot initialize cipher!");
            e.printStackTrace();
        }
        datagramSocket = new DatagramSocket(port);
    }

    public boolean start(boolean server, String address, int port, String key, String iv) throws SocketException, UnknownHostException {
        if ((artNetReader != null && artNetReader.running) || datagramSocket != null) {
            logger.severe("Art-Net has already been started.");
            return false;
        }

        if (server)
            initializeServer(port, key, iv);
        else
            initializeClient(address, port);

        datagramSocket.setReuseAddress(true);
        datagramSocket.setBroadcast(true);
        datagramSocket.setSoTimeout(1);

        this.artNetReader = new ArtNetReader();
        this.artNetReader.runTaskAsynchronously(lightShow);

        logger.info("Starting Art-Net at: " + address + ":" + port);
        return true;
    }

    public boolean stop() {
        if(artNetReader == null || datagramSocket == null)
            return false;

        datagramSocket.close();
        datagramSocket = null;
        artNetReader.cancel();
        artNetReader = null;
        logger.info("Stopped Art-Net.");
        return true;
    }

    public byte[] readDmx(int universeId) {
        return artNetBuffer.getDmxData(universeId);
    }

    private boolean isValidHeader(byte[] data) {
        boolean equal = true;
        for (int i = 0; i < ArtNetPacket.PACKET_HEADER.length && equal; i++)
            equal = data[i] == ArtNetPacket.PACKET_HEADER[i];
        return equal;
    }

    private ArtNetPacket parsePacket(byte[] raw) {
        if (raw.length <= 10)
            return null;
        if (server) {
            try {
                raw = cipher.doFinal(raw);
            } catch(IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
            }
        }
        int opCode = ByteBuffer.wrap(raw, 8, 16).order(ByteOrder.LITTLE_ENDIAN).getShort();
        if (opCode == 0x5000 && isValidHeader(raw))
            return new ArtNetPacket(raw);
        return null;
    }

    public boolean confirmReceiving(int timeout) {
        this.receiving = false;
        long start = System.currentTimeMillis();
        long current = start;
        while(!receiving) {
            Thread.onSpinWait();
            if(current >= start + timeout) {
                break;
            }
            current = System.currentTimeMillis();
        }
        return receiving;
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

    private class ArtNetReader extends BukkitRunnable {
        private boolean running = false;

        @Override
        public void run() {
            this.running = true;
            byte[] buffer = new byte[530];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                while(this.running) {
                    try {
                        datagramSocket.receive(receivedPacket);
                    } catch (SocketTimeoutException ignored) {}
                    ArtNetPacket packet = parsePacket(receivedPacket.getData());
                    if (packet != null) {
                        artNetBuffer.setDmxData(packet.getUniverseID(), packet.getDmx());
                        receiving = true;
                    }
                    stageManager.updateFixtures();
                }
                datagramSocket.close();
            } catch (IOException e) {
                if(running)
                    e.printStackTrace();
            }
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            this.running = false;
            super.cancel();
        }
    }
}
