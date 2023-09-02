package net.exenco.lightshow.show.artnet;

import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.scheduler.BukkitRunnable;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArtNetReceiver {
    private boolean running;
    private BukkitRunnable bukkitRunnable;
    private DatagramSocket datagramSocket;

    private final StageManager stageManager;
    private final ShowSettings showSettings;
    private final Logger logger;
    public ArtNetReceiver(StageManager stageManager, ShowSettings showSettings) {
        this.stageManager = stageManager;
        this.showSettings = showSettings;
        this.logger = stageManager.getLightShow().getLogger();
    }

    public boolean isRunning() {
        return bukkitRunnable != null || datagramSocket != null;
    }

    public boolean start() {
        if(this.isRunning()) {
            logger.warning("Could not start receiver: Already running!");
            return false;
        }

        ShowSettings.ArtNet settings = showSettings.artNet();

        int port = settings.address().port();
        String networkInterfaceAddress = settings.address().ip();
        try {
            // Get IP
            InetSocketAddress inetSocketAddress = settings.redirector().enabled() ? new InetSocketAddress(port) : new InetSocketAddress(networkInterfaceAddress, port);

            // Start socket
            datagramSocket = new DatagramSocket(inetSocketAddress);

            datagramSocket.setReuseAddress(true);
            datagramSocket.setBroadcast(true);
            datagramSocket.setSoTimeout(100);

        } catch (SocketException e) {
            logger.log(Level.WARNING, "Cannot start Art-Net!");
            e.printStackTrace();
            return false;
        }

        // Start
        this.bukkitRunnable = new ReceiverRunnable();
        bukkitRunnable.runTaskAsynchronously(stageManager.getLightShow());
        logger.info("Starting Art-Net at: " + networkInterfaceAddress + ":" + port);
        return true;
    }

    public boolean stop() {
        if(!this.isRunning()) {
            logger.warning("Could not stop receiver: Not currently running.");
            return false;
        }
        this.running = false;

        datagramSocket.close();
        datagramSocket = null;
        bukkitRunnable.cancel();
        bukkitRunnable = null;

        logger.info("Stopped Art-Net.");
        return true;
    }

    private class ReceiverRunnable extends BukkitRunnable {

        private final Cipher cipher;
        private final SecretKeyFactory factory;

        private ReceiverRunnable() {
            try {
                this.cipher = Cipher.getInstance("AES/GCM/NoPadding");
                this.factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            running = true;
            ShowSettings.ArtNet settings = showSettings.artNet();
            int packet_length = settings.redirector().enabled() ? 1024 + 16 + 16 + 12 : 1024;
            byte[] buffer = new byte[packet_length];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                while(running) {
                    try {
                        datagramSocket.receive(receivedPacket);

//                        if (!receivedPacket.getAddress().getHostAddress().equals(readAddress.getHostString())) {
//                            System.out.println("Packet discarded: Invalid source!");
//                            continue;
//                        }

                        byte[] data = Arrays.copyOf(receivedPacket.getData(), receivedPacket.getLength());

                        if (settings.redirector().enabled()) {
                            data = decrypt(data);
                            if (data == null)
                                continue;
                        }
                        stageManager.receiveArtNet(data);
                    } catch (SocketTimeoutException ignored) {}
                    stageManager.updateFixtures();
                }
            } catch (IOException e) {
                if(running)
                    logger.log(Level.WARNING, e.getMessage(), e.getCause());
            }
        }

        private byte[] decrypt(byte[] raw) {
            try {
                ShowSettings.ArtNet.Redirector settings = showSettings.artNet().redirector();

                String key = settings.key();
                byte[] iv = Arrays.copyOfRange(raw, 0, 12);
                byte[] salt = Arrays.copyOfRange(raw, 12, 28);
                byte[] data = Arrays.copyOfRange(raw, 28, raw.length);

                KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 128);
                SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
                GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

                return cipher.doFinal(data);

            } catch (InvalidAlgorithmParameterException | InvalidKeySpecException | InvalidKeyException |
                     IllegalBlockSizeException | BadPaddingException e) {
                logger.warning("Received packet with wrong encryption!");
            }
            return null;
        }
    }
}
