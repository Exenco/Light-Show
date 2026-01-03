package net.exenco.lightshow.listener;

import net.exenco.lightshow.show.ShowStage;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class PlayerMoveListener implements Listener {

    private final ShowStage stage;
    public PlayerMoveListener(ShowStage stage) {
        this.stage = stage;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Vector from = event.getFrom().toVector();
        Vector to = event.getTo().toVector();
        if (!from.equals(to)) {
            Player player = event.getPlayer();
            Location playerLocation = player.getLocation();
            Location stageCenter = stage.getCenter();
            CraftPlayer craftPlayer = (CraftPlayer) player;
            if (playerLocation.distance(stageCenter) < stage.getRadius()) {
                if (!stage.getPlayersInRange().contains(player)) {
                    stage.getPlayersInRange().add(player);
                    stage.showToPlayer(craftPlayer);
                }
            } else if (stage.getPlayersInRange().contains(player)) {
                stage.getPlayersInRange().remove(player);
                stage.hideFromPlayer(craftPlayer);
            }
        }
    }


}
