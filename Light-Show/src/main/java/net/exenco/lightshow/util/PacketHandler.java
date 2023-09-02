package net.exenco.lightshow.util;

import com.mojang.datafixers.util.Pair;
import net.exenco.lightshow.LightShow;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Manager for cases when the plugins need to modify client behaviour for specific players.
 */
public class PacketHandler {
    private final LightShow lightShow;
    private final ProximitySensor proximitySensor;
    private final Level level;
    public PacketHandler(LightShow lightShow, ProximitySensor proximitySensor, ShowSettings showSettings) {
        this.lightShow = lightShow;
        this.proximitySensor = proximitySensor;

        this.level = ((CraftWorld) Objects.requireNonNull(showSettings.stage().location().getWorld())).getHandle();
    }

    private void sendPacketToAllPlayers(Packet<? extends PacketListener> packet) {
        for (CraftPlayer player : proximitySensor.getPlayerList()) {
            player.getHandle().connection.send(packet);
        }
    }

    /**
     * Gets the world in which the stage is located in.
     * @return the stage-world.
     */
    public Level getLevel() {
        return level;
    }

    /* ----------------------- SET ----------------------- */

    /**
     * Sets everything altered in stage world for given player.
     * @param craftPlayer that receives changes.
     */
    public void set(CraftPlayer craftPlayer) {
        setPlayerBlocks(craftPlayer);
        setPlayerEntities(craftPlayer);
        setPlayerTeams(craftPlayer);
    }

    /* ----------------------- RESET ----------------------- */

    /**
     * Resets everything altered in stage world for given player
     * @param craftPlayer that receives changes.
     */
    public void reset(CraftPlayer craftPlayer) {
        resetPlayerBlocks(craftPlayer);
        resetPlayerEntities(craftPlayer);
    }

    /**
     * Resets everything for every participating player.
     */
    public void resetEverything() {
        proximitySensor.getPlayerList().forEach(this::reset);

        alteredBlocksMap.clear();
        entityMap.clear();
        scoreboardTeamList.clear();
    }


    /* ----------------------- BLOCK CHANGE ----------------------- */

    private final Map<Location, BlockData> alteredBlocksMap = new HashMap<>();

    /**
     * Sets all altered blocks for given player.
     * @param player that receives changes.
     */
    private void setPlayerBlocks(Player player) {
        for(Map.Entry<Location, BlockData> entry : alteredBlocksMap.entrySet()) {
            player.sendBlockChange(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Reset all altered block for given player
     * @param player that receives changes.
     */
    private void resetPlayerBlocks(Player player) {
        for(Location location : alteredBlocksMap.keySet()) {
            BlockData blockData = location.getBlock().getBlockData();
            player.sendBlockChange(location, blockData);
        }
    }

    /**
     * Change visual block in stage world for every participating player to see.
     * @param location of block to change.
     * @param blockData that block is to look like.
     */
    public void sendBlockChange(Vector location, BlockData blockData) {
        Location loc = location.toLocation(level.getWorld());
        for(CraftPlayer player : proximitySensor.getPlayerList()) {
            player.sendBlockChange(loc, blockData);
        }
        alteredBlocksMap.put(loc, blockData);
    }

    /* ----------------------- ENTITIES ----------------------- */

    private final Map<Integer, Entity> entityMap = new HashMap<>();

    /**
     * Creates a full list of all necessary packets for spawning an entity.
     * @param entity which is to spawn.
     * @return list of packets for entity spawning.
     */
    private List<Packet<? extends PacketListener>> getEntitySpawnPackets(Entity entity) {
        List<Packet<? extends PacketListener>> packetList = new ArrayList<>();
        packetList.add(new ClientboundAddEntityPacket(entity));

        // If entity has metadata, send packet
        ClientboundSetEntityDataPacket metadataPacket = getEntityMetadataPacket(entity);
        if (metadataPacket != null) {
            packetList.add(metadataPacket);
        }

        if (entity instanceof LivingEntity livingEntity) {
            packetList.add(getEntityEquipmentPacket(livingEntity));
        }
        return packetList;
    }

    /**
     * Creates {@link ClientboundSetEntityDataPacket} object for given {@link Entity}.
     * @param entity to get metadata from
     * @return the created {@link ClientboundSetEntityDataPacket} object.
     */
    private ClientboundSetEntityDataPacket getEntityMetadataPacket(Entity entity) {
        SynchedEntityData entityData = entity.getEntityData();
        if (entityData.getNonDefaultValues() == null) {
            return null;
        }
        return new ClientboundSetEntityDataPacket(entity.getId(), entityData.getNonDefaultValues());
    }

    /**
     * Creates {@link ClientboundSetEquipmentPacket} object for given {@link LivingEntity}.
     * @param entity to get equipment from.
     * @return the created {@link ClientboundSetEquipmentPacket} object.
     */
    private ClientboundSetEquipmentPacket getEntityEquipmentPacket(LivingEntity entity) {
        List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
        equipment.add(new Pair<>(EquipmentSlot.MAINHAND, entity.getItemBySlot(EquipmentSlot.MAINHAND)));
        equipment.add(new Pair<>(EquipmentSlot.OFFHAND, entity.getItemBySlot(EquipmentSlot.OFFHAND)));
        equipment.add(new Pair<>(EquipmentSlot.FEET, entity.getItemBySlot(EquipmentSlot.FEET)));
        equipment.add(new Pair<>(EquipmentSlot.LEGS, entity.getItemBySlot(EquipmentSlot.LEGS)));
        equipment.add(new Pair<>(EquipmentSlot.CHEST, entity.getItemBySlot(EquipmentSlot.CHEST)));
        equipment.add(new Pair<>(EquipmentSlot.HEAD, entity.getItemBySlot(EquipmentSlot.HEAD)));
        return new ClientboundSetEquipmentPacket(entity.getId(), equipment);
    }

    /**
     * Creates {@link ClientboundTeleportEntityPacket} object for given {@link Entity}.
     * @param entity to get coordinates from
     * @return the created {@link ClientboundTeleportEntityPacket} object.
     */
    private ClientboundTeleportEntityPacket getEntityMovePacket(Entity entity) {
        return new ClientboundTeleportEntityPacket(entity);
    }

    /**
     * Creates {@link ClientboundRemoveEntitiesPacket} object for given id.
     * @param id entity id to destroy.
     * @return the created {@link ClientboundRemoveEntitiesPacket} object.
     */
    private ClientboundRemoveEntitiesPacket getEntityDestroyPacket(int id) {
        return new ClientboundRemoveEntitiesPacket(id);
    }

    /**
     * Spawns an entity for every participating player.
     * @param entity to spawn.
     */
    public void spawnEntity(Entity entity) {
        entityMap.put(entity.getId(), entity);
        getEntitySpawnPackets(entity).forEach(this::sendPacketToAllPlayers);
    }

    /**
     * Updates an entity for every participating player.
     * @param entity to update.
     */
    public void updateEntity(Entity entity) {
        entityMap.put(entity.getId(), entity);
        sendPacketToAllPlayers(getEntityMetadataPacket(entity));
    }

    /**
     * Updates entity equipment for every participating player.
     * @param entity to update.
     */
    public void updateEntityEquipment(LivingEntity entity) {
        entityMap.put(entity.getId(), entity);
        sendPacketToAllPlayers(getEntityEquipmentPacket(entity));
    }

    /**
     * Moves an entity for every participating player.
     * @param entity to move.
     */
    public void moveEntity(Entity entity) {
        entityMap.put(entity.getId(), entity);
        sendPacketToAllPlayers(getEntityMovePacket(entity));
    }

    /**
     * Destroys an entity (by id) for every participating player.
     * @param id entity id to destroy.
     */
    public void destroyEntity(int id) {
        entityMap.remove(id);
        sendPacketToAllPlayers(getEntityDestroyPacket(id));
    }

    /**
     * Sets entities for given player.
     * @param player to set entities for.
     */
    private void setPlayerEntities(CraftPlayer player) {
        entityMap.values().forEach(entity ->
                getEntitySpawnPackets(entity).forEach(packet ->
                        player.getHandle().connection.send(packet)));
    }

    /**
     * Resets entities for given player.
     * @param player to reset entities for.
     */
    private void resetPlayerEntities(CraftPlayer player) {
        entityMap.keySet().forEach(id -> player.getHandle().connection.send(getEntityDestroyPacket(id)));
    }

    /* ----------------------- SCOREBOARD ----------------------- */

    private final List<PlayerTeam> scoreboardTeamList = new ArrayList<>();

    /**
     * Creates {@link ClientboundSetPlayerTeamPacket} object for given {@link PlayerTeam}.
     * @param scoreboardTeam to get team from.
     * @return the created {@link ClientboundSetPlayerTeamPacket} object.
     */
    private ClientboundSetPlayerTeamPacket getTeamCreationPacket(PlayerTeam scoreboardTeam) {
        return ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(scoreboardTeam, true);
    }

    /**
     * Creates team for all participating players.
     * @param scoreboardTeam to create.
     */
    public void createTeam(PlayerTeam scoreboardTeam) {
        scoreboardTeamList.add(scoreboardTeam);
        sendPacketToAllPlayers(getTeamCreationPacket(scoreboardTeam));
    }

    /**
     * Sets teams for given player
     * @param player to set teams for.
     */
    private void setPlayerTeams(CraftPlayer player) {
        scoreboardTeamList.forEach(scoreboardTeam ->
                player.getHandle().connection.send(getTeamCreationPacket(scoreboardTeam)));
    }

    /* ----------------------- PARTICLES ----------------------- */

    /**
     * Spawns particle for every participating player.
     * @param particle refer to Spigot docs.
     * @param location refer to Spigot docs.
     * @param count refer to Spigot docs.
     * @param offsetX refer to Spigot docs.
     * @param offsetY refer to Spigot docs.
     * @param offsetZ refer to Spigot docs.
     * @param time refer to Spigot docs.
     * @param data refer to Spigot docs.
     */
    public void spawnParticle(Particle particle, Vector location, int count, double offsetX, double offsetY, double offsetZ, double time, Object data) {
        for(Player player : proximitySensor.getPlayerList())
            player.spawnParticle(particle, location.toLocation(level.getWorld()), count, offsetX, offsetY, offsetZ, time, data);
    }

    /* ----------------------- FIREWORK ----------------------- */

    /**
     * Spawns firework for every participating player.
     * @param entityFireworks to spawn.
     */
    public void spawnFirework(FireworkRocketEntity entityFireworks) {
        getEntitySpawnPackets(entityFireworks).forEach(this::sendPacketToAllPlayers);

        new BukkitRunnable() {
            @Override
            public void run() {
                entityFireworks.lifetime = 0;
                sendPacketToAllPlayers(getEntityMetadataPacket(entityFireworks));
                sendPacketToAllPlayers(new ClientboundEntityEventPacket(entityFireworks, (byte) 17));
                sendPacketToAllPlayers(getEntityDestroyPacket(entityFireworks.getId()));
            }
        }.runTaskLaterAsynchronously(lightShow, entityFireworks.lifetime);
    }

    /* ----------------------- SOUND ----------------------- */

    /**
     * Plays sound for every participating player.
     * @param location to spawn sound at.
     * @param sound path to sound file.
     * @param soundCategory category in which the sound is played in.
     * @param volume of the sound.
     * @param pitch of the sound.
     */
    public void playSound(Vector location, String sound, SoundCategory soundCategory, float volume, float pitch) {
        for(Player player : proximitySensor.getPlayerList())
            player.playSound(location.toLocation(level.getWorld()), sound, soundCategory, volume, pitch);
    }

    /**
     * Stops sound for every participating player.
     * @param sound path to sound file.
     * @param soundCategory category in which the sound was played in.
     */
    public void stopSound(String sound, SoundCategory soundCategory) {
        for(Player player : proximitySensor.getPlayerList())
            player.stopSound(sound, soundCategory);
    }
}
