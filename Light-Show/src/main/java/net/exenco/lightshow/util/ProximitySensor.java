package net.exenco.lightshow.util;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * A sensor whether a player is in the specified radius of a predefined location. If they entered the radius they are stored
 * in a list for further use. Analogical goes for leaving the radius and being removed from the list.
 * Everyone that enters is being sent every change the plugin has made so far.
 */
public class ProximitySensor {
    private final List<CraftPlayer> playerList;
    private final List<CraftPlayer> toggleList;

    private Location anchor;
    private double radius;

    private final ShowSettings showSettings;
    private PacketHandler packetHandler;
    public ProximitySensor(ShowSettings showSettings) {
        this.showSettings = showSettings;
        this.playerList = new ArrayList<>();
        this.toggleList = new ArrayList<>();

        load();
    }

    /**
     * Loads all necessary information.
     */
    public void load() {
        this.playerList.clear();

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

        if(playerLocation.distance(anchor) <= radius) {
            if(playerList.contains(craftPlayer) || toggleList.contains(craftPlayer))
                return;
            playerList.add(craftPlayer);
            craftPlayer.sendMessage(showSettings.stage().termsOfService());
            packetHandler.set(craftPlayer);
        } else {
            if(!playerList.contains(craftPlayer))
                return;
            playerList.remove(craftPlayer);
            packetHandler.reset(craftPlayer);
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
     * @return List of {@link CraftPlayer}s which are currently in range.
     */
    public List<CraftPlayer> getPlayerList() {
        return playerList;
    }

    public void addTogglePlayer(Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        packetHandler.reset(craftPlayer);
        toggleList.add(craftPlayer);
        playerList.remove(craftPlayer);
    }

    public void removeTogglePlayer(Player player) {
        toggleList.remove((CraftPlayer) player);
    }

    public boolean containsTogglePlayer(Player player) {
        return toggleList.contains((CraftPlayer) player);
    }
}
