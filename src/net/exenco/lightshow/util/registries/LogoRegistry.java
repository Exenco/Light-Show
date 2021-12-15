package net.exenco.lightshow.util.registries;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exenco.lightshow.util.ShowSettings;
import net.exenco.lightshow.util.file.ConfigHandler;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LogoRegistry {
    private final HashMap<Integer, Logo> logoMap = new HashMap<>();

    private final ConfigHandler configHandler;
    private final ShowSettings showSettings;
    public LogoRegistry(ConfigHandler configHandler, ShowSettings showSettings) {
        this.configHandler = configHandler;
        this.showSettings = showSettings;

        load();
    }

    public void load() {
        logoMap.clear();
        for(Map.Entry<Integer, String> entry : showSettings.logoMap().entrySet()) {
            int id = entry.getKey();
            JsonObject jsonObject = configHandler.getLogoJson(entry.getValue());
            logoMap.put(id, new Logo(jsonObject));
        }
    }

    public Logo getLogoById(int id) {
        return logoMap.get(id);
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

        public record ParticleDot(ParticleEntry particleEntry, Vector location) {
            private static ParticleDot valueOf(JsonObject jsonObject) {
                Particle particle = ParticleRegistry.valueOf(jsonObject.get("Particle").getAsString()).getBukkitParticle();
                Particle.DustOptions data = null;
                if(particle.getDataType() == Particle.DustOptions.class)
                    data = ParticleEntry.dataValueOf(jsonObject.getAsJsonObject("Data"));
                Vector location = ConfigHandler.translateVector(jsonObject.getAsJsonObject("Location"));
                return new ParticleDot(new ParticleEntry(particle, data), location);
            }
        }
        public record ParticleLine(ParticleEntry particleEntry, Vector origin, Vector destination) {
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

        public ArrayList<ParticleDot> getParticleDotList() {
            return particleDotList;
        }

        public ArrayList<ParticleLine> getParticleLineList() {
            return particleLineList;
        }

        public record ParticleEntry(Particle particle, Particle.DustOptions data) {
            public static Particle.DustOptions dataValueOf(JsonObject jsonObject) {
                Color color = ConfigHandler.translateColor(jsonObject);
                float size = jsonObject.get("Size").getAsFloat();
                return new Particle.DustOptions(color, size);
            }
        }
    }
}
