package net.exenco.showcontroller;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class ArtNetHandler {

    private Thread socketReader;
    private DatagramSocket readSocket;
    private Logger logger;
    private boolean confirm;

    public boolean start(InetSocketAddress readAddress, Logger logger) {
        this.logger = logger;
        if(readAddress == null) {
            logger.warn("Entered IP-Address wrong.");
            return false;
        }
        try {
            readSocket = new DatagramSocket(readAddress);
            readSocket.setReuseAddress(true);
            readSocket.setBroadcast(true);

            logger.info("Art-Net established.");
            socketReader = new Thread(new SocketReader());
            socketReader.start();

            return true;
        } catch (IOException e) {
            System.err.println("There has been an error starting Art-Net.");
            logger.error("Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isRunning() {
        return !(socketReader == null || readSocket == null);
    }

    public void stop() {
        if(socketReader == null || readSocket == null)
            return;

        socketReader.interrupt();
        socketReader = null;
        readSocket.close();
        readSocket = null;
        logger.info("Stopped Art-Net.");
    }

    public boolean confirmConnection() {
        confirm = false;
        long start = System.currentTimeMillis();
        long current = start;
        while(!confirm) {
            Thread.onSpinWait();
            if(current >= start + 500) {
                break;
            }
            current = System.currentTimeMillis();
        }
        return confirm;
    }

    private class SocketReader extends Thread {
        @Override
        public void run() {
            byte[] buffer = new byte[530];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            while(!isInterrupted()) {
                try {
                    readSocket.receive(receivedPacket);
                    confirm = true;
                    PacketByteBuf packetBuffer = PacketByteBufs.create();
                    packetBuffer.writeByteArray(receivedPacket.getData());
                    ClientPlayNetworking.send(new Identifier("lightshow", "artnet"), packetBuffer);
                } catch(Exception ignored) {
                    ArtNetHandler.this.stop();
                }
            }
            if(readSocket != null)
                readSocket.close();
        }
    }
}
