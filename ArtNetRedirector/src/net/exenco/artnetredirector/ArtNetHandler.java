package net.exenco.artnetredirector;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.List;

public class ArtNetHandler {

    private Thread socketReader;
    private DatagramSocket readSocket;
    private DatagramSocket writeSocket;
    private InetSocketAddress readAddress;
    private List<InetSocketAddress> writeAddressList;
    private JLabel log;
    private JLabel count;

    private Cipher cipher;
    private String key;
    private SecretKeyFactory factory;

    public int start(InetSocketAddress readAddress, boolean external, List<InetSocketAddress> writeAddressList, String password, JLabel log, JLabel count) {
        this.log = log;
        this.count = count;
        this.key = password;
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
            this.readAddress = readAddress;
            if (external) {
                readSocket = new DatagramSocket(readAddress.getPort());
            } else {
                readSocket = new DatagramSocket(readAddress);
            }
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

            this.cipher = Cipher.getInstance("AES/GCM/NoPadding");
            this.factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            socketReader = new SocketReader();
            socketReader.start();

            return 1;
        } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException e){
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
            byte[] buffer = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            while(!isInterrupted()) {
                try {
                    System.out.println("Receiving packet...");
                    readSocket.receive(receivedPacket);
                    System.out.println("Received packet!");

                    if (!receivedPacket.getAddress().getHostAddress().equals(readAddress.getHostString())) {
                        System.out.println("Packet discarded: Invalid source!");
                        continue;
                    }

                    byte[] data = Arrays.copyOf(receivedPacket.getData(), receivedPacket.getLength());

                    byte[] encrypted = encrypt(data);
                    receivedPacket.setData(encrypted);

                    count.setText(count.getText().equals("1") ? "0" : "1");
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

    private byte[] encrypt(byte[] raw) {
        try {
            SecureRandom secureRandom = new SecureRandom();

            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);


            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);

            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 128);
            SecretKey secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encrypted = cipher.doFinal(raw);

            int length = iv.length + salt.length + encrypted.length;
            ByteBuffer buffer = ByteBuffer.allocate(length);
            buffer.put(iv);
            buffer.put(salt);
            buffer.put(encrypted);
            return buffer.array();
        } catch (InvalidAlgorithmParameterException | InvalidKeySpecException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
