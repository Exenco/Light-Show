package net.exenco.lightshow.listener;

import net.exenco.lightshow.util.ProximitySensor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public record PlayerMoveListener(ProximitySensor proximitySensor) implements Listener {

    /**
     * Called whenever a player updates their position.
     * @param event listened event
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Vector from = event.getFrom().toVector();
        Vector to = event.getTo().toVector();
        if (!from.equals(to)) {
            Player player = event.getPlayer();
            proximitySensor.playerMove(player);
        }
    }
}
