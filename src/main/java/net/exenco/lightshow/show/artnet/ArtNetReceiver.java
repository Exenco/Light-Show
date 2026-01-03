package net.exenco.lightshow.show.artnet;

import net.exenco.lightshow.LightShowPlugin;
import net.exenco.lightshow.show.ShowStage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArtNetReceiver {
    private boolean running;
    private BukkitRunnable bukkitRunnable;
    private DatagramSocket datagramSocket;

    private final LightShowPlugin plugin;
    private final ShowStage showStage;
    private final Logger logger;
    public ArtNetReceiver(LightShowPlugin plugin, ShowStage showStage) {
        this.plugin = plugin;
        this.showStage = showStage;
        this.logger = plugin.getLogger();
    }

    public boolean isRunning() {
        return bukkitRunnable != null || datagramSocket != null;
    }

    public boolean start() {
        if(this.isRunning()) {
            logger.warning("Could not start receiver: Already running!");
            return false;
        }

        FileConfiguration config = plugin.getConfig();

        int port = config.getInt("art-net.port", 6454);
        String networkInterfaceAddress = config.getString("art-net.ip", "0.0.0.0");
        try {
            // Get IP
            InetSocketAddress inetSocketAddress = new InetSocketAddress(networkInterfaceAddress, port);

            // Start socket
            datagramSocket = new DatagramSocket(inetSocketAddress);

            datagramSocket.setReuseAddress(true);
            datagramSocket.setBroadcast(true);
            datagramSocket.setSoTimeout(10);

        } catch (SocketException e) {
            logger.log(Level.WARNING, "Cannot start Art-Net on " + networkInterfaceAddress + ":" + port + ". " + e.getMessage());
            return false;
        }

        // Start
        this.bukkitRunnable = new ReceiverRunnable();
        bukkitRunnable.runTaskAsynchronously(plugin);
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
        @Override
        public void run() {
            running = true;
            int packet_length = 530;
            byte[] buffer = new byte[packet_length];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                while (running) {
                    try {
                        datagramSocket.receive(receivedPacket);
                        byte[] data = Arrays.copyOf(receivedPacket.getData(), receivedPacket.getLength());
                        showStage.readPacket(data);
                    } catch (SocketTimeoutException ignored) {}
                    showStage.updateFixtures();
                }
            } catch (IOException e) {
                if (running)
                    logger.log(Level.WARNING, e.getMessage(), e.getCause());
            }
        }
    }
}
