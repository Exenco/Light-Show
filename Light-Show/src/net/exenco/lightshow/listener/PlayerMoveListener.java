package net.exenco.lightshow.listener;

import net.exenco.lightshow.util.ProximitySensor;
import net.minecraft.server.commands.CommandExecute;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public record PlayerMoveListener(ProximitySensor proximitySensor) implements Listener {

    /**
     * Called whenever a player updates their position.
     * @param event listened event
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to != null && !from.toVector().equals(to.toVector()))
            proximitySensor.playerMove(event.getPlayer());
    }
}
