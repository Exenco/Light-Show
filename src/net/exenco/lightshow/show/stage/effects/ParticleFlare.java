package net.exenco.lightshow.show.stage.effects;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.registries.ParticleRegistry;
import org.bukkit.Color;
import org.bukkit.Particle;

public class ParticleFlare extends ShowEffect {
    private final double maxXOffset;
    private final double maxYOffset;
    private final double maxZOffset;
    private final double maxTime;
    private final float maxSize;

    private final PacketHandler packetHandler;
    public ParticleFlare(JsonObject jsonObject, PacketHandler packetHandler) {
        super(jsonObject);
        this.packetHandler = packetHandler;

        this.maxXOffset = jsonObject.has("MaxXOffset") ? jsonObject.get("MaxXOffset").getAsDouble() : 1;
        this.maxYOffset = jsonObject.has("MaxYOffset") ? jsonObject.get("MaxYOffset").getAsDouble() : 1;
        this.maxZOffset = jsonObject.has("MaxZOffset") ? jsonObject.get("MaxZOffset").getAsDouble() : 1;
        this.maxTime = jsonObject.has("MaxTime") ? jsonObject.get("MaxTime").getAsDouble() : 20;
        this.maxSize = jsonObject.has("MaxSize") ? jsonObject.get("MaxSize").getAsFloat() : 1;
    }

    @Override
    public int getDmxSize() {
        return 8;
    }

    @Override
    public void applyState(int[] data) {
        int count = asRoundedPercentage(data[0]);
        int particleId = data[1];
        double offset = valueOf(data[2]);
        double time = valueOfMax(maxTime, data[3]);
        int red = data[4];
        int green = data[5];
        int blue = data[6];
        float size = (float) valueOfMax(maxSize, data[7]);

        Particle particle = ParticleRegistry.getById(particleId);
        if(particle == null)
            return;
        Object particleData = null;
        if(particle.getDataType() == Particle.DustOptions.class)
            particleData = new Particle.DustOptions(Color.fromRGB(red, green, blue), size);

        double offsetX = maxXOffset * offset;
        double offsetY = maxYOffset * offset;
        double offsetZ = maxZOffset * offset;

        if(isTick() && count > 0) {
            packetHandler.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, time, particleData, true);
        }
    }
}
