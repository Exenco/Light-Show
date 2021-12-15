package net.exenco.lightshow.show.artnet;

import net.exenco.lightshow.LightShow;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArtNetClient {
    private final LightShow lightShow;
    private final Logger logger;
    private final int port;

    private SocketReader socketReader;
    private final ArtNetBuffer artNetBuffer;
    private DatagramSocket datagramSocket;

    public ArtNetClient(LightShow lightShow, int port) {
        this.lightShow = lightShow;
        this.logger = lightShow.getLogger();
        this.port = port;

        this.artNetBuffer = new ArtNetBuffer();
    }

    public boolean start(String networkInterfaceAddress) {
        if(socketReader != null || datagramSocket != null) {
            logger.severe("Art-Net has already been started.");
            return false;
        }
        try {
            InetAddress inetAddress = InetAddress.getByName(networkInterfaceAddress);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);

            artNetBuffer.clear();

            datagramSocket = new DatagramSocket(inetSocketAddress);
            datagramSocket.setReuseAddress(true);
            datagramSocket.setBroadcast(true);

            socketReader = new SocketReader();
            socketReader.runTaskAsynchronously(lightShow);
            logger.info("Art-Net started at: " + inetAddress.getHostAddress() + ":" + port);

            return true;
        } catch (SocketException | UnknownHostException e) {
            logger.log(Level.SEVERE, "There has been an error starting Art-Net.", e);
        }
        return false;
    }

    public boolean stop() {
        if(socketReader == null || datagramSocket == null)
            return false;

        String address = datagramSocket.getLocalAddress().getHostAddress();
        int port = datagramSocket.getLocalPort();

        datagramSocket.close();
        datagramSocket = null;
        socketReader.cancel();
        socketReader = null;
        logger.info("Stopped Art-Net from: " + address + ":" + port);
        return true;
    }

    private ArtNetPacket parse(byte[] raw) {
        ArtNetPacket packet = null;
        if (raw.length <= 10)
            return null;
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
        for(int i = 0; i < 8 && isEqual; i++) {
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
}
