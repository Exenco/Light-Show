package net.exenco.lightshow.show.receiver;

import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.scheduler.BukkitRunnable;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.logging.Logger;

public class ServerReceiver implements ReceiverMethod {

    private boolean running;
    private BukkitRunnable bukkitRunnable;
    private DatagramSocket datagramSocket;
    private Cipher cipher;

    private final StageManager stageManager;
    private final ShowSettings showSettings;
    private final Logger logger;
    public ServerReceiver(StageManager stageManager, ShowSettings showSettings) {
        this.stageManager = stageManager;
        this.showSettings = showSettings;
        this.logger = stageManager.getLightShow().getLogger();
    }

    @Override
    public boolean isRunning() {
        return bukkitRunnable != null || datagramSocket != null;
    }

    @Override
    public boolean start() {
        if(this.isRunning()) {
            logger.warning("Could not start receiver: Already running!");
            return false;
        }

        int port = showSettings.artNet().serverReceiver().port();
        String key = showSettings.artNet().serverReceiver().key();
        String iv = showSettings.artNet().serverReceiver().iv();
        try {
            this.cipher = Cipher.getInstance("AES/CFB/NoPadding");
            SecretKey secretKey = getKeyFromPassword(key);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, generateIv(iv));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeySpecException | InvalidKeyException e) {
            logger.severe("Cannot initialize cipher!");
            e.printStackTrace();
        }
        try {
            datagramSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            logger.severe("Could not initialize Socket!");
            e.printStackTrace();
        }

        this.running = true;
        this.bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[530];
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                try {
                    while(running) {
                        try {
                            datagramSocket.receive(receivedPacket);
                        } catch (SocketTimeoutException ignored) {}
                        byte[] raw = receivedPacket.getData();
                        try {
                            raw = cipher.doFinal(raw);
                        } catch(IllegalBlockSizeException | BadPaddingException e) {
                            e.printStackTrace();
                        }
                        stageManager.receiveArtNet(raw);
                        stageManager.updateFixtures();
                    }
                    datagramSocket.close();
                } catch (IOException e) {
                    if(!this.isCancelled())
                        e.printStackTrace();
                }
            }
        };
        bukkitRunnable.runTaskAsynchronously(stageManager.getLightShow());
        logger.info("Starting Art-Net server at port " + port);

        return true;
    }

    @Override
    public boolean stop() {
        if(!this.isRunning()) {
            logger.warning("Could not stop Art-Net server: Not currently running.");
            return false;
        }
        this.running = false;
        datagramSocket.close();
        datagramSocket = null;
        bukkitRunnable.cancel();
        bukkitRunnable = null;
        logger.info("Stopped Art-Net server.");

        return true;
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
