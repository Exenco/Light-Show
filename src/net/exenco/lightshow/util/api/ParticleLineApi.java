package net.exenco.lightshow.util.api;

import net.exenco.lightshow.util.PacketHandler;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class ParticleLineApi {

    private Vector start;
    private Vector destination;
    private double maxDistance;

    private Particle particle;
    private double offsetX = 0;
    private double offsetY = 0;
    private double offsetZ = 0;
    private double time = 0;
    private Object data = null;

    private final PacketHandler packetHandler;
    public ParticleLineApi(Vector location, PacketHandler packetHandler) {
        this.start = location;
        this.packetHandler = packetHandler;
    }

    public void play() {
        Vector iterator = start.clone();
        Vector direc = destination.clone().subtract(start).normalize().multiply(0.15);
        for(double distance = 0; distance < maxDistance; distance = iterator.distance(start)) {
            packetHandler.spawnParticle(particle, iterator, 1, offsetX, offsetY, offsetZ, time, data);
            iterator.add(direc);
        }
    }

    public void setStart(Vector start) {
        this.start = start;
    }

    public void setDestination(Vector destination) {
        this.destination = destination;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public void setParticle(Particle particle) {
        this.particle = particle;
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
