package net.exenco.lightshow.util.api;

import net.exenco.lightshow.util.PacketHandler;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderCrystal;
import org.bukkit.util.Vector;

public class EndCrystalApi {
    private final Vector start;
    private boolean spawned;
    private Vector destination;
    private EntityEnderCrystal entityCrystal;

    private final PacketHandler packetHandler;
    public EndCrystalApi(Vector location, PacketHandler packetHandler) {
        this.start = location;
        this.packetHandler = packetHandler;
    }

    public void setDestination(Vector destination) {
        if(this.destination != null && this.destination.equals(destination))
            return;
        this.destination = destination;
        this.entityCrystal.a(new BlockPosition(destination.getX(), destination.getY(), destination.getZ()));
        packetHandler.updateEntity(entityCrystal);
    }

    public void spawn() {
        if(this.start == null)
            return;
        spawned = true;
        Vector destination = this.destination;
        if(this.destination == null)
            destination = start;

        this.entityCrystal = new EntityEnderCrystal(EntityTypes.u, packetHandler.getWorld());
        this.entityCrystal.e(start.getX(), start.getY(), start.getZ());
        this.entityCrystal.a(false);
        this.entityCrystal.a(new BlockPosition(destination.getX(), destination.getY(), destination.getZ()));

        packetHandler.spawnEntity(entityCrystal);
    }

    public void destroy() {
        spawned = false;
        packetHandler.destroyEntity(entityCrystal.ae());
    }

    public boolean isSpawned() {
        return spawned;
    }
}