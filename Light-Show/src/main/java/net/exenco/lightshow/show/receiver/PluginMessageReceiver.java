package net.exenco.lightshow.show.receiver;

import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PluginMessageReceiver implements ReceiverMethod, PluginMessageListener {

    private boolean running;
    private BukkitRunnable task;

    private final StageManager stageManager;
    private final ShowSettings showSettings;
    public PluginMessageReceiver(StageManager stageManager, ShowSettings showSettings) {
        LightShow lightShow = stageManager.getLightShow();
        lightShow.getServer().getMessenger().unregisterIncomingPluginChannel(lightShow);
        lightShow.getServer().getMessenger().registerIncomingPluginChannel(lightShow, "BungeeCord", this);
        lightShow.getServer().getMessenger().registerIncomingPluginChannel(lightShow, "lightshow:artnet", this);
        this.stageManager = stageManager;
        this.showSettings = showSettings;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean start() {
        if(running || task != null) {
            return false;
        }
        this.running = true;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                while(running)
                    stageManager.updateFixtures();
            }
        };
        task.runTaskAsynchronously(stageManager.getLightShow());
        return true;
    }

    @Override
    public boolean stop() {
        if(!running && task == null) {
            return false;
        }
        running = false;
        task.cancel();
        task = null;
        return true;
    }

    private long time = 0;
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (showSettings.artNet().pluginMessage().bungeeCord()) {
            if (!channel.equals("BungeeCord"))
                return;
            byte[] raw = new byte[message.length - 2];
            System.arraycopy(message, 2, raw, 0, message.length - 2);
            stageManager.receiveArtNet(raw);
        } else {
            if (!channel.equals("lightshow:artnet"))
                return;
            if (!player.getUniqueId().equals(UUID.fromString(showSettings.artNet().pluginMessage().senderUuid()))) {
                if(time <= System.currentTimeMillis()) {
                    stageManager.getLightShow().getLogger().severe("(" + player.getName() + ", " + player.getUniqueId() + ", " + player.getAddress() + ") tries to send Art-Net packets.");
                    time = System.currentTimeMillis() + 20000;
                }
                return;
            }
            byte[] raw = new byte[message.length - 2];
            System.arraycopy(message, 2, raw, 0, message.length - 2);
            stageManager.receiveArtNet(raw);
        }
    }
}
