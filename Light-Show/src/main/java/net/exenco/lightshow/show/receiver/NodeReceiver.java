package net.exenco.lightshow.show.receiver;

import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.*;
import java.util.logging.Logger;

public class NodeReceiver implements ReceiverMethod {
    private boolean running;
    private BukkitRunnable bukkitRunnable;
    private DatagramSocket datagramSocket;

    private final StageManager stageManager;
    private final ShowSettings showSettings;
    private final Logger logger;
    public NodeReceiver(StageManager stageManager, ShowSettings showSettings) {
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

        String networkInterfaceAddress = showSettings.artNet().receiverNode().ip();
        int port = showSettings.artNet().receiverNode().port();
        try {
            InetAddress inetAddress = InetAddress.getByName(networkInterfaceAddress);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);
            datagramSocket = new DatagramSocket(inetSocketAddress);
            datagramSocket.setReuseAddress(true);
            datagramSocket.setBroadcast(true);
            datagramSocket.setSoTimeout(1);
        } catch (SocketException | UnknownHostException e) {
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
                        stageManager.receiveArtNet(receivedPacket.getData());
                        stageManager.updateFixtures();
                    }
                    datagramSocket.close();
                } catch (IOException e) {
                    if(running)
                        e.printStackTrace();
                }
            }
        };
        bukkitRunnable.runTaskAsynchronously(stageManager.getLightShow());
        logger.info("Starting Art-Net at: " + networkInterfaceAddress + ":" + port);
        return true;
    }

    @Override
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
}
