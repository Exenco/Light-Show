package net.exenco.lightshow.util.api;

import net.exenco.lightshow.util.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import org.bukkit.util.Vector;

public class EndCrystalApi {
    private final Vector start;
    private boolean spawned;
    private Vector destination;
    private EndCrystal entityCrystal;

    private final PacketHandler packetHandler;
    public EndCrystalApi(Vector location, PacketHandler packetHandler) {
        this.start = location;
        this.packetHandler = packetHandler;
    }

    public void setDestination(Vector destination) {
        if(this.destination != null && this.destination.equals(destination))
            return;
        this.destination = destination;
        this.entityCrystal.setBeamTarget(new BlockPos(destination.getBlockX(), destination.getBlockY(), destination.getBlockZ()));
        packetHandler.updateEntity(entityCrystal);
    }

    public void spawn() {
        if(this.start == null)
            return;
        spawned = true;
        Vector destination = this.destination;
        if(this.destination == null)
            destination = start;

        this.entityCrystal = new EndCrystal(EntityType.END_CRYSTAL, packetHandler.getLevel());
        this.entityCrystal.setPos(start.getX(), start.getY(), start.getZ());
        this.entityCrystal.setShowBottom(false);
        this.entityCrystal.setBeamTarget(new BlockPos(destination.getBlockX(), destination.getBlockY(), destination.getBlockZ()));

        packetHandler.spawnEntity(entityCrystal);
    }

    public void destroy() {
        spawned = false;
        packetHandler.destroyEntity(entityCrystal.getId());
    }

    public boolean isSpawned() {
        return spawned;
    }
}