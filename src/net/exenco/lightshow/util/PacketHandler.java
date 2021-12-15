package net.exenco.lightshow.util;

import com.mojang.datafixers.util.Pair;
import net.exenco.lightshow.LightShow;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.ParticleParam;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.scores.ScoreboardTeam;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_17_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.data.CraftBlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Manager for cases when the plugins need to send packets to a player.
 */
public class PacketHandler {
    private final LightShow lightShow;
    private final ProximitySensor proximitySensor;
    private final World world;
    public PacketHandler(LightShow lightShow, ProximitySensor proximitySensor, ShowSettings showSettings) {
        this.lightShow = lightShow;
        this.proximitySensor = proximitySensor;

        org.bukkit.World w = showSettings.stage().location().getWorld();
        if(w == null)
            throw new NullPointerException("World entered for stage location is not valid!");
        this.world = ((CraftWorld) w).getHandle();
    }

    private void sendPacketToAllPlayers(Packet<? extends PacketListener> packet) {
        for(PlayerConnection playerConnection : proximitySensor.getPlayerConnectionList())
            playerConnection.sendPacket(packet);
    }

    public World getWorld() {
        return world;
    }

    /* ----------------------- SET ----------------------- */

    private List<Packet<? extends PacketListener>> getSetPackets() {
        List<Packet<? extends PacketListener>> packetList = new ArrayList<>();
        alteredBlocksMap.forEach((key, value) -> packetList.add(getBlockChangePacket(key, value)));
        entityMap.values().forEach(entity -> packetList.addAll(getEntitySpawnPackets(entity)));
        scoreboardTeamList.forEach(scoreboardTeam -> packetList.add(getTeamCreationPacket(scoreboardTeam)));

        return packetList;
    }

    public void set(PlayerConnection playerConnection) {
        List<Packet<? extends PacketListener>> packetList = getSetPackets();
        packetList.forEach(playerConnection::sendPacket);
    }

    /* ----------------------- RESET ----------------------- */

    private List<Packet<? extends PacketListener>> getResetPackets() {
        List<Packet<? extends PacketListener>> packetList = new ArrayList<>();
        for(Vector location : alteredBlocksMap.keySet()) {
            BlockPosition blockPos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            Block block = CraftBlock.at(world, blockPos);
            IBlockData blockData = ((CraftBlockData) block.getBlockData()).getState();
            PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(blockPos, blockData);
            packetList.add(packet);
        }
        entityMap.keySet().forEach(id -> packetList.add(getEntityDestroyPacket(id)));
        return packetList;
    }

    public void reset(PlayerConnection playerConnection) {
        List<Packet<? extends PacketListener>> packetList = getResetPackets();
        packetList.forEach(playerConnection::sendPacket);
    }

    public void resetEverything() {
        List<Packet<? extends PacketListener>> packetList = getResetPackets();
        packetList.forEach(this::sendPacketToAllPlayers);

        alteredBlocksMap.clear();
        entityMap.clear();
        scoreboardTeamList.clear();
    }


    /* ----------------------- BLOCK CHANGE ----------------------- */

    private final Map<Vector, CraftBlockData> alteredBlocksMap = new HashMap<>();

    private PacketPlayOutBlockChange getBlockChangePacket(Vector location, BlockData blockData) {
        BlockPosition blockPos = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IBlockData iBlockData = ((CraftBlockData) blockData).getState();
        return new PacketPlayOutBlockChange(blockPos, iBlockData);
    }

    public void sendBlockChange(Vector location, Material material) {
        sendBlockChange(location, material.createBlockData());
    }

    public void sendBlockChange(Vector location, BlockData blockData) {
        PacketPlayOutBlockChange packet = getBlockChangePacket(location, blockData);
        sendPacketToAllPlayers(packet);
        alteredBlocksMap.put(location, (CraftBlockData) blockData);
    }


    /* ----------------------- ENTITIES ----------------------- */

    private final Map<Integer, Entity> entityMap = new HashMap<>();
    private final List<ScoreboardTeam> scoreboardTeamList = new ArrayList<>();

    private List<Packet<? extends PacketListener>> getEntitySpawnPackets(Entity entity) {
        List<Packet<? extends PacketListener>> packetList = new ArrayList<>();
        packetList.add(new PacketPlayOutSpawnEntity(entity));
        packetList.add(new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true));

        if (entity instanceof EntityLiving entityLiving) {
            packetList.add(getEntityEquipmentPacket(entityLiving));
        }
        return packetList;
    }

    private PacketPlayOutEntityMetadata getEntityUpdatePacket(Entity entity) {
        return new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);
    }

    private PacketPlayOutEntityEquipment getEntityEquipmentPacket(EntityLiving entity) {
        List<Pair<EnumItemSlot, ItemStack>> equipment = new ArrayList<>();
        equipment.add(new Pair<>(EnumItemSlot.a, entity.getEquipment(EnumItemSlot.a)));
        equipment.add(new Pair<>(EnumItemSlot.b, entity.getEquipment(EnumItemSlot.b)));
        equipment.add(new Pair<>(EnumItemSlot.c, entity.getEquipment(EnumItemSlot.c)));
        equipment.add(new Pair<>(EnumItemSlot.d, entity.getEquipment(EnumItemSlot.d)));
        equipment.add(new Pair<>(EnumItemSlot.e, entity.getEquipment(EnumItemSlot.e)));
        equipment.add(new Pair<>(EnumItemSlot.f, entity.getEquipment(EnumItemSlot.f)));
        return new PacketPlayOutEntityEquipment(entity.getId(), equipment);
    }

    private PacketPlayOutEntityTeleport getEntityMovePacket(Entity entity) {
        return new PacketPlayOutEntityTeleport(entity);
    }

    private PacketPlayOutEntityDestroy getEntityDestroyPacket(int id) {
        return new PacketPlayOutEntityDestroy(id);
    }

    private PacketPlayOutScoreboardTeam getTeamCreationPacket(ScoreboardTeam scoreboardTeam) {
        return PacketPlayOutScoreboardTeam.a(scoreboardTeam, true);
    }

    public void spawnEntity(Entity entity) {
        entity.t = world;
        entityMap.put(entity.getId(), entity);
        getEntitySpawnPackets(entity).forEach(this::sendPacketToAllPlayers);
    }

    public void updateEntity(Entity entity) {
        entityMap.put(entity.getId(), entity);
        sendPacketToAllPlayers(getEntityUpdatePacket(entity));
    }

    public void updateEntityEquipment(EntityLiving entity) {
        entityMap.put(entity.getId(), entity);
        sendPacketToAllPlayers(getEntityEquipmentPacket(entity));
    }

    public void moveEntity(Entity entity) {
        entityMap.put(entity.getId(), entity);
        sendPacketToAllPlayers(getEntityMovePacket(entity));
    }

    public void destroyEntity(int id) {
        entityMap.remove(id);
        sendPacketToAllPlayers(getEntityDestroyPacket(id));
    }

    public void createTeam(ScoreboardTeam scoreboardTeam) {
        scoreboardTeamList.add(scoreboardTeam);
        sendPacketToAllPlayers(getTeamCreationPacket(scoreboardTeam));
    }


    /* ----------------------- PARTICLES ----------------------- */

    private <T extends ParticleParam> PacketPlayOutWorldParticles getParticlePacket(T particle, boolean force, Vector location, double offsetX, double offsetY, double offsetZ, double time, int count) {
        return new PacketPlayOutWorldParticles(particle, force, location.getX(), location.getY(), location.getZ(), (float) offsetX, (float) offsetY, (float) offsetZ, (float) time, count);
    }

    public void spawnParticle(Particle particle, Vector location, int count, double offsetX, double offsetY, double offsetZ, double time, Object data, boolean force) {
        sendPacketToAllPlayers(getParticlePacket(CraftParticle.toNMS(particle, data), force, location, offsetX, offsetY, offsetZ, time, count));
    }


    /* ----------------------- FIREWORK ----------------------- */

    public void spawnFirework(EntityFireworks entityFireworks) {
        PacketPlayOutSpawnEntity packetPlayOutSpawnEntity = new PacketPlayOutSpawnEntity(entityFireworks);
        sendPacketToAllPlayers(packetPlayOutSpawnEntity);
        PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityFireworks.getId(), entityFireworks.getDataWatcher(), true);
        sendPacketToAllPlayers(packetPlayOutEntityMetadata);

        new BukkitRunnable() {
            @Override
            public void run() {
                entityFireworks.f = 0;
                PacketPlayOutEntityMetadata packetPlayOutEntityMetadata = new PacketPlayOutEntityMetadata(entityFireworks.getId(), entityFireworks.getDataWatcher(), true);
                sendPacketToAllPlayers(packetPlayOutEntityMetadata);
                PacketPlayOutEntityStatus packetPlayOutEntityStatus = new PacketPlayOutEntityStatus(entityFireworks, (byte) 17);
                sendPacketToAllPlayers(packetPlayOutEntityStatus);
                PacketPlayOutEntityDestroy packetPlayOutEntityDestroy = new PacketPlayOutEntityDestroy(entityFireworks.getId());
                sendPacketToAllPlayers(packetPlayOutEntityDestroy);
            }
        }.runTaskLaterAsynchronously(lightShow, entityFireworks.f);
    }

    /* ----------------------- SOUND ----------------------- */

    public void playSound(Vector location, String sound, SoundCategory soundCategory, float volume, float pitch) {
        sendPacketToAllPlayers(new PacketPlayOutCustomSoundEffect(new MinecraftKey(sound), net.minecraft.sounds.SoundCategory.valueOf(soundCategory.name()), new Vec3D(location.getX(), location.getY(), location.getZ()), volume, pitch));
    }

    public void stopSound(String sound, SoundCategory soundCategory) {
        sendPacketToAllPlayers(new PacketPlayOutStopSound(new MinecraftKey(sound), soundCategory == null ? net.minecraft.sounds.SoundCategory.a : net.minecraft.sounds.SoundCategory.valueOf(soundCategory.name())));
    }
}
