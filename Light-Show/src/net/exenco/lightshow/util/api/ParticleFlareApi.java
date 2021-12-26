package net.exenco.lightshow.util.api;

import net.exenco.lightshow.util.PacketHandler;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ParticleFlareApi {
    private Vector location;
    private Particle particle;
    private int count = 1;
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    private double time = 0;
    private Object data = null;

    private final PacketHandler packetHandler;
    public ParticleFlareApi(Vector location, PacketHandler packetHandler) {
        this.location = location;
        this.packetHandler = packetHandler;
    }

    public void play() {
        packetHandler.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, time, data);
    }

    public void setLocation(Vector location) {
        this.location = location;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public void setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
