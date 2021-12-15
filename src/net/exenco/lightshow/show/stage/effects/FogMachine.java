package net.exenco.lightshow.show.stage.effects;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.VectorUtils;
import net.exenco.lightshow.util.registries.ParticleRegistry;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class FogMachine extends ShowEffect {
    private final double maxOffset;
    private final double maxVelocity;
    private final Vector direction;

    private final PacketHandler packetHandler;
    public FogMachine(JsonObject jsonObject, PacketHandler packetHandler) {
        super(jsonObject);
        this.packetHandler = packetHandler;
        this.tickSize = 50;

        this.maxOffset = jsonObject.has("MaxOffset") ? jsonObject.get("MaxOffset").getAsDouble() : 0.5;
        this.maxVelocity = jsonObject.has("MaxVelocity") ? jsonObject.get("MaxVelocity").getAsDouble() : 2;
        double yaw = jsonObject.has("Yaw") ? jsonObject.get("Yaw").getAsDouble() : 0;
        double pitch = jsonObject.has("Pitch") ? jsonObject.get("Pitch").getAsDouble() : 0;

        this.direction = VectorUtils.getDirectionVector(-yaw, -pitch + 90).normalize();
    }

    @Override
    public int getDmxSize() {
        return 4;
    }

    @Override
    public void applyState(int[] data) {
        double velocity = valueOfMax(maxVelocity, data[0]);
        int particleId = data[1];
        int count  = data[2];
        double offset = valueOfMax(maxOffset, data[3]);

        Particle particle = ParticleRegistry.getById(particleId);
        if(particle == null || particle.getDataType() != Void.class || velocity == 0.0)
            return;

        if(isTick() && count > 0) {
            for(int i = 0; i < count; i++) {
                double direc = getRandomDouble(offset);
                double smallOffset = offset * 0.1;
                Vector location = this.location.clone().add(direction.clone().multiply(direc));
                location.add(getRandomVector(offset));
                Vector direction = this.direction.clone();
                direction.add(getRandomVector(smallOffset));
                packetHandler.spawnParticle(particle, location, 0, direction.getX(), direction.getY(), direction.getZ(), velocity, null, true);
            }
        }
    }

    private Vector getRandomVector(double max) {
        return new Vector(getRandomDouble(max*2) - max, getRandomDouble(max*2) - max, getRandomDouble(max*2) - max);
    }

    private double getRandomDouble(double max) {
        if(max == 0)
            return 0;
        return ThreadLocalRandom.current().nextDouble(max);
    }
}
