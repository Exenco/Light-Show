package net.exenco.lightshow.util.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exenco.lightshow.util.VectorUtils;
import net.exenco.lightshow.util.ConfigHandler;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.ParticleRegistry;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class LogoApi {
    private final ArrayList<ParticleLineApi> particleLineApis = new ArrayList<>();
    private final ArrayList<ParticleFlareApi> particleFlareApis = new ArrayList<>();

    private final double yaw;
    private final double pitch;

    private final PacketHandler packetHandler;
    public LogoApi(PacketHandler packetHandler, double yaw, double pitch) {
        this.packetHandler = packetHandler;

        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void setLogo(Vector location, Logo logo, double size) {
        particleFlareApis.clear();
        particleLineApis.clear();

        if(logo == null)
            return;

        for(Logo.ParticleDot particleDot : logo.getParticleDotList()) {
            Vector loc = particleDot.location().clone().multiply(size);
            loc = VectorUtils.getRotatedVector(loc, yaw, pitch);

            ParticleFlareApi particleFlareApi = new ParticleFlareApi(loc.add(location), packetHandler);
            Logo.ParticleEntry particleEntry = particleDot.particleEntry();
            particleFlareApi.setParticle(particleEntry.particle());
            particleFlareApi.setData(particleEntry.data());
            particleFlareApis.add(particleFlareApi);
        }
        for(Logo.ParticleLine particleLine : logo.getParticleLineList()) {
            Vector origin = particleLine.origin().clone().multiply(size);
            Vector destination = particleLine.destination().clone().multiply(size);
            origin = VectorUtils.getRotatedVector(origin, yaw, pitch);
            destination = VectorUtils.getRotatedVector(destination, yaw, pitch);

            ParticleLineApi particleLineApi = new ParticleLineApi(origin.add(location), packetHandler);
            particleLineApi.setDestination(destination.add(location));
            particleLineApi.setMaxDistance(origin.distance(destination));
            Logo.ParticleEntry particleEntry = particleLine.particleEntry();
            particleLineApi.setParticle(particleEntry.particle());
            particleLineApi.setData(particleEntry.data());
            particleLineApis.add(particleLineApi);
        }
    }

    public void playLogo() {
        particleFlareApis.forEach(ParticleFlareApi::play);
        particleLineApis.forEach(ParticleLineApi::play);
    }

    public static class Logo {
        private final ArrayList<ParticleDot> particleDotList;
        private final ArrayList<ParticleLine> particleLineList;

        public Logo(JsonObject jsonObject) {
            this.particleDotList = new ArrayList<>();
            this.particleLineList = new ArrayList<>();

            JsonArray lineArray = jsonObject.getAsJsonArray("ParticleLines");
            for(JsonElement jsonElement : lineArray)
                particleLineList.add(ParticleLine.valueOf(jsonElement.getAsJsonObject()));

            JsonArray dotArray = jsonObject.getAsJsonArray("ParticleDots");
            for(JsonElement jsonElement : dotArray)
                particleDotList.add(ParticleDot.valueOf(jsonElement.getAsJsonObject()));
        }

        private record ParticleDot(ParticleEntry particleEntry, Vector location) {
            private static ParticleDot valueOf(JsonObject jsonObject) {
                Particle particle = ParticleRegistry.valueOf(jsonObject.get("Particle").getAsString()).getBukkitParticle();
                Particle.DustOptions data = null;
                if(particle.getDataType() == Particle.DustOptions.class)
                    data = ParticleEntry.dataValueOf(jsonObject.getAsJsonObject("Data"));
                Vector location = ConfigHandler.translateVector(jsonObject.getAsJsonObject("Location"));
                return new ParticleDot(new ParticleEntry(particle, data), location);
            }
        }
        private record ParticleLine(ParticleEntry particleEntry, Vector origin, Vector destination) {
            private static ParticleLine valueOf(JsonObject jsonObject) {
                Particle particle = ParticleRegistry.valueOf(jsonObject.get("Particle").getAsString()).getBukkitParticle();
                Particle.DustOptions data = null;
                if(particle.getDataType() == Particle.DustOptions.class)
                    data = ParticleEntry.dataValueOf(jsonObject.getAsJsonObject("Data"));
                Vector origin = ConfigHandler.translateVector(jsonObject.getAsJsonObject("Origin"));
                Vector destination = ConfigHandler.translateVector(jsonObject.getAsJsonObject("Destination"));
                return new ParticleLine(new ParticleEntry(particle, data), origin, destination);
            }
        }

        private ArrayList<ParticleDot> getParticleDotList() {
            return particleDotList;
        }

        private ArrayList<ParticleLine> getParticleLineList() {
            return particleLineList;
        }

        private record ParticleEntry(Particle particle, Particle.DustOptions data) {
            public static Particle.DustOptions dataValueOf(JsonObject jsonObject) {
                Color color = ConfigHandler.translateColor(jsonObject);
                float size = jsonObject.get("Size").getAsFloat();
                return new Particle.DustOptions(color, size);
            }
        }
    }
}
