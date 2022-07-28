package net.exenco.messagerelay;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MessageListener implements Listener {

    private final MessageRelay showController;
    public MessageListener(MessageRelay showController) {
        this.showController = showController;
    }

    private long time = 0;

    @EventHandler
    public void onMessageReceive(PluginMessageEvent event) {
        String channel = event.getTag();
        if (channel.equals("lightshow:artnet")) {
            ProxiedPlayer player = ((ProxiedPlayer) event.getSender());
            if (!player.getUniqueId().equals(showController.getAllowedUUID())) {
                if(time <= System.currentTimeMillis()) {
                    showController.getLogger().severe("(" + player.getName() + ", " + player.getUniqueId() + ", " + player.getSocketAddress() + ") tries to send Art-Net packets.");
                    time = System.currentTimeMillis() + 20000;
                }
                return;
            }
            for(ServerInfo server : showController.getProxy().getServers().values()) {
                server.sendData("BungeeCord", event.getData());
            }
        }
    }
}