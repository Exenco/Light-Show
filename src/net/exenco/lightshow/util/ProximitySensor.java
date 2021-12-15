package net.exenco.lightshow.util;

import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A sensor whether a player is in the specified radius of a predefined location. If they entered the radius they are stored
 * in a list for further use. Analogical goes for leaving the radius and being removed from the list.
 * Everyone that enters is being sent every change the plugin has made so far.
 */
public class ProximitySensor {
    private final List<PlayerConnection> playerConnectionList;

    private Location anchor;
    private double radius;

    private final ShowSettings showSettings;
    private PacketHandler packetHandler;
    public ProximitySensor(ShowSettings showSettings) {
        this.showSettings = showSettings;
        this.playerConnectionList = new ArrayList<>();

        load();
    }

    /**
     * Loads all necessary information.
     */
    public void load() {
        this.playerConnectionList.clear();

        this.radius = showSettings.stage().radius();
        this.anchor = showSettings.stage().location();
    }

    /**
     * Check if a player is joining or leaving the area.
     * @param player to check.
     */
    public void playerMove(Player player) {
        Location playerLocation = player.getLocation();
        CraftPlayer craftPlayer = (CraftPlayer) player;

        PlayerConnection playerConnection = craftPlayer.getHandle().b;
        if(playerLocation.distance(anchor) <= radius) {
            if(playerConnectionList.contains(playerConnection))
                return;
            playerConnectionList.add(playerConnection);
            craftPlayer.sendMessage(showSettings.stage().termsOfService());
            packetHandler.set(playerConnection);
        } else {
            if(!playerConnectionList.contains(playerConnection))
                return;
            playerConnectionList.remove(playerConnection);
            packetHandler.reset(playerConnection);
        }
    }

    /**
     * Sets the {@link PacketHandler} to use. Cannot be done in constructor since packetHandler is requiring an instance of
     * ProximitySensor as well.
     * @param packetHandler to be initialised with.
     */
    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    /**
     * @return List of {@link PlayerConnection}s which are currently in range.
     */
    public List<PlayerConnection> getPlayerConnectionList() {
        return playerConnectionList;
    }
}
