package net.exenco.lightshow.util;

import com.mojang.datafixers.util.Pair;
import net.exenco.lightshow.LightShow;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.scores.ScoreboardTeam;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
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
    private final World world;
    public PacketHandler(LightShow lightShow, ProximitySensor proximitySensor, ShowSettings showSettings) {
        this.lightShow = lightShow;
        this.proximitySensor = proximitySensor;

        this.world = ((CraftWorld) Objects.requireNonNull(showSettings.stage().location().getWorld())).getHandle();
    }

    private void sendPacketToAllPlayers(Packet<? extends PacketListener> packet) {
        for(CraftPlayer player : proximitySensor.getPlayerList())
            player.getHandle().b.a(packet);
    }

    /**
     * Gets the world in which the stage is located in.
     * @return the stage-world.
     */
    public World getWorld() {
        return world;
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
        Location loc = location.toLocation(world.getWorld());
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
        packetList.add(new PacketPlayOutSpawnEntity(entity));
        packetList.add(getEntityMetadataPacket(entity));

        if (entity instanceof EntityLiving entityLiving) {
            packetList.add(getEntityEquipmentPacket(entityLiving));
        }
        return packetList;
    }

    /**
     * Creates {@link PacketPlayOutEntityMetadata} object for given {@link Entity}.
     * @param entity to get metadata from
     * @return the created {@link PacketPlayOutEntityMetadata} object.
     */
    private PacketPlayOutEntityMetadata getEntityMetadataPacket(Entity entity) {
        return new PacketPlayOutEntityMetadata(entity.ae(), entity.ai(), true);
    }

    /**
     * Creates {@link PacketPlayOutEntityEquipment} object for given {@link EntityLiving}.
     * @param entity to get equipment from.
     * @return the created {@link PacketPlayOutEntityEquipment} object.
     */
    private PacketPlayOutEntityEquipment getEntityEquipmentPacket(EntityLiving entity) {
        List<Pair<EnumItemSlot, ItemStack>> equipment = new ArrayList<>();
        equipment.add(new Pair<>(EnumItemSlot.a, entity.b(EnumItemSlot.a)));
        equipment.add(new Pair<>(EnumItemSlot.b, entity.b(EnumItemSlot.b)));
        equipment.add(new Pair<>(EnumItemSlot.c, entity.b(EnumItemSlot.c)));
        equipment.add(new Pair<>(EnumItemSlot.d, entity.b(EnumItemSlot.d)));
        equipment.add(new Pair<>(EnumItemSlot.e, entity.b(EnumItemSlot.e)));
        equipment.add(new Pair<>(EnumItemSlot.f, entity.b(EnumItemSlot.f)));
        return new PacketPlayOutEntityEquipment(entity.ae(), equipment);
    }

    /**
     * Creates {@link PacketPlayOutEntityTeleport} object for given {@link Entity}.
     * @param entity to get coordinates from
     * @return the created {@link PacketPlayOutEntityTeleport} object.
     */
    private PacketPlayOutEntityTeleport getEntityMovePacket(Entity entity) {
        return new PacketPlayOutEntityTeleport(entity);
    }

    /**
     * Creates {@link PacketPlayOutEntityDestroy} object for given id.
     * @param id entity id to destroy.
     * @return the created {@link PacketPlayOutEntityDestroy} object.
     */
    private PacketPlayOutEntityDestroy getEntityDestroyPacket(int id) {
        return new PacketPlayOutEntityDestroy(id);
    }

    /**
     * Spawns an entity for every participating player.
     * @param entity to spawn.
     */
    public void spawnEntity(Entity entity) {
        entity.t = this.world;
        entityMap.put(entity.ae(), entity);
        getEntitySpawnPackets(entity).forEach(this::sendPacketToAllPlayers);
    }

    /**
     * Updates an entity for every participating player.
     * @param entity to update.
     */
    public void updateEntity(Entity entity) {
        entityMap.put(entity.ae(), entity);
        sendPacketToAllPlayers(getEntityMetadataPacket(entity));
    }

    /**
     * Updates entity equipment for every participating player.
     * @param entity to update.
     */
    public void updateEntityEquipment(EntityLiving entity) {
        entityMap.put(entity.ae(), entity);
        sendPacketToAllPlayers(getEntityEquipmentPacket(entity));
    }

    /**
     * Moves an entity for every participating player.
     * @param entity to move.
     */
    public void moveEntity(Entity entity) {
        entityMap.put(entity.ae(), entity);
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
        entityMap.values().forEach(entity -> getEntitySpawnPackets(entity).forEach(packet -> player.getHandle().b.a(packet)));
    }

    /**
     * Resets entities for given player.
     * @param player to reset entities for.
     */
    private void resetPlayerEntities(CraftPlayer player) {
        entityMap.keySet().forEach(id -> player.getHandle().b.a(getEntityDestroyPacket(id)));
    }

    /* ----------------------- SCOREBOARD ----------------------- */

    private final List<ScoreboardTeam> scoreboardTeamList = new ArrayList<>();

    /**
     * Creates {@link PacketPlayOutScoreboardTeam} object for given {@link ScoreboardTeam}.
     * @param scoreboardTeam to get team from.
     * @return the created {@link PacketPlayOutScoreboardTeam} object.
     */
    private PacketPlayOutScoreboardTeam getTeamCreationPacket(ScoreboardTeam scoreboardTeam) {
        return PacketPlayOutScoreboardTeam.a(scoreboardTeam, true);
    }

    /**
     * Creates team for all participating players.
     * @param scoreboardTeam to create.
     */
    public void createTeam(ScoreboardTeam scoreboardTeam) {
        scoreboardTeamList.add(scoreboardTeam);
        sendPacketToAllPlayers(getTeamCreationPacket(scoreboardTeam));
    }

    /**
     * Sets teams for given player
     * @param player to set teams for.
     */
    private void setPlayerTeams(CraftPlayer player) {
        scoreboardTeamList.forEach(scoreboardTeam -> player.getHandle().b.a(getTeamCreationPacket(scoreboardTeam)));
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
            player.spawnParticle(particle, location.toLocation(world.getWorld()), count, offsetX, offsetY, offsetZ, time, data);
    }

    /* ----------------------- FIREWORK ----------------------- */

    /**
     * Spawns firework for every participating player.
     * @param entityFireworks to spawn.
     */
    public void spawnFirework(EntityFireworks entityFireworks) {
        entityFireworks.t = this.world;

        getEntitySpawnPackets(entityFireworks).forEach(this::sendPacketToAllPlayers);

        new BukkitRunnable() {
            @Override
            public void run() {
                entityFireworks.f = 0;
                sendPacketToAllPlayers(getEntityMetadataPacket(entityFireworks));
                sendPacketToAllPlayers(new PacketPlayOutEntityStatus(entityFireworks, (byte) 17));
                sendPacketToAllPlayers(getEntityDestroyPacket(entityFireworks.ae()));
            }
        }.runTaskLaterAsynchronously(lightShow, entityFireworks.f);
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
            player.playSound(location.toLocation(world.getWorld()), sound, soundCategory, volume, pitch);
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
