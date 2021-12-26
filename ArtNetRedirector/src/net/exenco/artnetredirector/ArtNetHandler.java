package net.exenco.artnetredirector;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.List;

public class ArtNetHandler {

    private Thread socketReader;
    private DatagramSocket readSocket;
    private DatagramSocket writeSocket;
    private List<InetSocketAddress> writeAddressList;
    private JLabel log;

    private Cipher cipher;

    public int start(InetSocketAddress readAddress, List<InetSocketAddress> writeAddressList, String password, String iv, JLabel log) {
        this.log = log;
        if(readAddress == null || writeAddressList == null) {
            log.setText("Entered IP-Address wrong.");
            return -1;
        }

        if(socketReader != null || readSocket != null) {
            System.err.println("Art-Net has already been started.");
            log.setText("Art-Net has already been started");
            return 0;
        }
        try {
            readSocket = new DatagramSocket(readAddress);
            readSocket.setReuseAddress(true);
            readSocket.setBroadcast(true);

            this.writeAddressList = writeAddressList;
            writeSocket = new DatagramSocket();
            writeSocket.setReuseAddress(true);
            writeSocket.setBroadcast(true);
            System.out.println("Art-Net redirector has been established: " + readAddress);
            log.setText("Art-Net established.");
            for(InetSocketAddress address : writeAddressList)
                System.out.println(address);

            this.cipher = Cipher.getInstance("AES/CFB/NoPadding");
            SecretKey secretKey = getKeyFromPassword(password);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, generateIv(iv));

            socketReader = new Thread(new SocketReader());
            socketReader.start();

            return 1;
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException | InvalidKeyException | InvalidAlgorithmParameterException e){
            System.err.println("There has been an error starting Art-Net.");
            log.setText("Error: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public void stop() {
        if(socketReader == null || readSocket == null || writeSocket == null)
            return;

        socketReader.interrupt();
        socketReader = null;
        readSocket.close();
        readSocket = null;
        writeSocket.close();
        writeSocket = null;
        System.out.println("Stopped Art-Net redirector.");
        log.setText("Stopped Art-Net.");
    }

    private class SocketReader extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[2048];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            while(!isInterrupted()) {
                try {
                    System.out.println("Receiving packet...");
                    readSocket.receive(receivedPacket);
                    System.out.println("Received packet!");

                    byte[] data = receivedPacket.getData();
                    byte[] encrypted = cipher.doFinal(data);
                    receivedPacket.setData(encrypted);

                    for(InetSocketAddress address : writeAddressList) {
                        receivedPacket.setAddress(address.getAddress());
                        receivedPacket.setPort(address.getPort());
                        writeSocket.send(receivedPacket);
                    }
                } catch(Exception ignored) {
                    this.interrupt();
                }
            }
            if(readSocket != null)
                readSocket.close();
        }
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
