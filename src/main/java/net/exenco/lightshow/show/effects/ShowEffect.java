package net.exenco.lightshow.show.effects;

import net.exenco.lightshow.util.VectorUtils;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import java.util.*;
import java.util.function.Supplier;

public class ShowEffect {

    private final Map<Location, BlockData> alteredBlocksMap = new HashMap<>();
    private final Map<Integer, Entity> entityMap = new HashMap<>();
    private final Supplier<Collection<Player>> audienceSupplier;

    protected final Level level;
    protected final Vector origin;
    protected final Quaternionf defaultRotation;
    protected Vector defaultOffset;

    public ShowEffect(World world, Vector origin, Vector rotation, Supplier<Collection<Player>> audienceSupplier) {
        this.level = ((CraftWorld) world).getHandle();
        this.origin = origin;
        this.defaultRotation = VectorUtils.getMagicQRotationQuaternion(rotation);

        this.audienceSupplier = audienceSupplier;
        this.defaultOffset = new Vector(0.0, 0.0, 0.0);
    }

    public void showToPlayer(CraftPlayer player) {
        for (Map.Entry<Location, BlockData> entry : alteredBlocksMap.entrySet()) {
            player.sendBlockChange(entry.getKey(), entry.getValue());
        }
        ServerGamePacketListenerImpl connection = player.getHandle().connection;
        for (Entity entity : entityMap.values()) {
            for (Packet<? extends PacketListener> packet : getEntitySpawnPackets(entity)) {
                connection.send(packet);
            }
        }
    }

    public void hideFromPlayer(CraftPlayer player) {
        for (Map.Entry<Location, BlockData> entry : alteredBlocksMap.entrySet()) {
            Block block = entry.getKey().getBlock();
            player.sendBlockChange(entry.getKey(), block.getBlockData());
        }
        ServerGamePacketListenerImpl connection = player.getHandle().connection;
        for (Entity entity : entityMap.values()) {
            connection.send(getEntityRemovePacket(entity));
        }
    }

    public void releaseUpdate() {
        for (Player player : audienceSupplier.get()) {
            for (Map.Entry<Location, BlockData> entry : alteredBlocksMap.entrySet()) {
                player.sendBlockChange(entry.getKey(), entry.getValue());
            }
            ServerGamePacketListenerImpl connection = ((CraftPlayer) player).getHandle().connection;
            for (Entity entity : entityMap.values()) {
                ClientboundSetEntityDataPacket entityMetadataPacket = getEntityMetadataPacket(entity);
                if (entityMetadataPacket != null) {
                    connection.send(entityMetadataPacket);
                }
            }
        }
    }


    public void alterBlock(@NotNull Location location, @NotNull BlockData blockData) {
        alteredBlocksMap.put(location, blockData);
        for (Player player : audienceSupplier.get()) {
            if (!(player instanceof CraftPlayer craftPlayer)) {
                continue;
            }
            craftPlayer.sendBlockChange(location, blockData);
        }
    }

    public void spawnEntity(Entity entity) {
        entityMap.put(entity.getId(), entity);
        for (Player player : audienceSupplier.get()) {
            if (!(player instanceof CraftPlayer craftPlayer)) {
                continue;
            }
            for (Packet<? extends PacketListener> packet : getEntitySpawnPackets(entity)) {
                craftPlayer.getHandle().connection.send(packet);
            }
        }
    }

    public void updateEntity(Entity entity) {
        entityMap.put(entity.getId(), entity); 
    }

    private List<Packet<? extends PacketListener>> getEntitySpawnPackets(Entity entity) {
        List<Packet<? extends PacketListener>> packetList = new ArrayList<>();
        packetList.add(entity.getAddEntityPacket(new ServerEntity(level.getMinecraftWorld(), entity, 0, false, null, Set.of())));

        // If entity has metadata, send packet
        ClientboundSetEntityDataPacket metadataPacket = getEntityMetadataPacket(entity);
        if (metadataPacket != null) {
            packetList.add(metadataPacket);
        }
        return packetList;
    }

    private ClientboundSetEntityDataPacket getEntityMetadataPacket(Entity entity) {
        SynchedEntityData entityData = entity.getEntityData();
        if (entityData.getNonDefaultValues() == null) {
            return null;
        }
        return new ClientboundSetEntityDataPacket(entity.getId(), entityData.getNonDefaultValues());
    }

    private ClientboundRemoveEntitiesPacket getEntityRemovePacket(Entity entity) {
        return new ClientboundRemoveEntitiesPacket(entity.getId());
    }
}
