package net.exenco.lightshow.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exenco.lightshow.util.file.ConfigHandler;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to load and administer settings set by the user via files.
 */
public class ShowSettings {

    private final ConfigHandler configHandler;
    public ShowSettings(ConfigHandler configHandler) {
        this.configHandler = configHandler;
        load();
    }

    private Commands commands;
    private ArtNet artNet;
    private EffectSettings showEffects;
    private Map<Integer, String> dmxUniverseMap;
    private Map<Integer, String> songMap;
    private Map<Integer, String> logoMap;
    private Map<Integer, String> fireworkMap;
    private Stage stage;

    /**
     * Loads all necessary information.
     */
    public void load() {
        JsonObject configJson = configHandler.getConfigJson();
        this.commands = Commands.valueOf(configJson.getAsJsonObject("Commands"));
        this.showEffects = EffectSettings.valueOf(configJson.getAsJsonObject("EffectSettings"));
        this.artNet = ArtNet.valueOf(configJson.getAsJsonObject("ArtNet"));
        this.stage = Stage.valueOf(configJson.getAsJsonObject("Stage"));
        this.dmxUniverseMap = getFileIdentifierMap(configJson.getAsJsonArray("DmxUniverses"));
        this.songMap = getFileIdentifierMap(configJson.getAsJsonArray("Songs"));
        this.logoMap = getFileIdentifierMap(configJson.getAsJsonArray("Logos"));
        this.fireworkMap = getFileIdentifierMap(configJson.getAsJsonArray("Fireworks"));
    }

    /**
     * @return Command Settings used by plugin.
     */
    public Commands commands() {
        return this.commands;
    }

    /**
     * @return Identifier map for DmxUniverses.
     */
    public Map<Integer, String> dmxUniverseMap() {
        return this.dmxUniverseMap;
    }

    /**
     * @return Identifier map for songs.
     */
    public Map<Integer, String> songMap() {
        return this.songMap;
    }

    /**
     * @return Identifier map for logos.
     */
    public Map<Integer, String> logoMap() {
        return this.logoMap;
    }

    /**
     * @return Identifier map for fireworks.
     */
    public Map<Integer, String> fireworkMap() {
        return this.fireworkMap;
    }

    /**
     * @return All effect settings.
     */
    public EffectSettings showEffects() {
        return this.showEffects;
    }

    /**
     * @return Art-Net settings.
     */
    public ArtNet artNet() {
        return this.artNet;
    }

    /**
     * @return Stage settings.
     */
    public Stage stage() {
        return this.stage;
    }

    public record Commands(String noPermission, String notAllowed, String reload) {
        public static Commands valueOf(JsonObject jsonObject) {
            String noPermission = jsonObject.get("NoPermission").getAsString();
            String notAllowed = jsonObject.get("NotAllowed").getAsString();
            String reload = jsonObject.get("Reload").getAsString();
            return new Commands(noPermission, notAllowed, reload);
        }
    }

    public record ArtNet(String ip, int port, String starting, String alreadyStarted, String stopping, String alreadyStopped) {
        public static ArtNet valueOf(JsonObject jsonObject) {
            String ip = jsonObject.get("Ip").getAsString();
            int port = jsonObject.get("Port").getAsInt();
            String starting = jsonObject.get("Starting").getAsString();
            String alreadyStarted = jsonObject.get("AlreadyStarted").getAsString();
            String stopping = jsonObject.get("Stopping").getAsString();
            String alreadyStopped = jsonObject.get("AlreadyStopped").getAsString();
            return new ArtNet(ip, port, starting, alreadyStarted, stopping, alreadyStopped);
        }
    }

    public record EffectSettings(Selector selector, Beacon beacon, MovingLight movingLight) {
        public record Selector(int maxValue) {
            public static Selector valueOf(JsonObject jsonObject) {
                int maxValue = jsonObject.get("MaxValue").getAsInt();
                return new Selector(maxValue);
            }
        }

        public record Beacon(Material disabledBlock) {
            public static Beacon valueOf(JsonObject jsonObject) {
                Material disabledBlock = Material.valueOf(jsonObject.get("DisabledBlock").getAsString().toUpperCase());
                return new Beacon(disabledBlock);
            }
        }

        public record MovingLight(String offTexture, String lowTexture, String mediumTexture, String highTexture) {
            public static MovingLight valueOf(JsonObject jsonObject) {
                String offTexture = jsonObject.get("OffTexture").getAsString();
                String lowTexture = jsonObject.get("LowTexture").getAsString();
                String mediumTexture = jsonObject.get("MediumTexture").getAsString();
                String highTexture = jsonObject.get("HighTexture").getAsString();
                return new MovingLight(offTexture, lowTexture, mediumTexture, highTexture);
            }
        }

        public static EffectSettings valueOf(JsonObject jsonObject) {
            Selector selector = Selector.valueOf(jsonObject.getAsJsonObject("SongSelector"));
            Beacon beacon = Beacon.valueOf(jsonObject.getAsJsonObject("Beacon"));
            MovingLight movingLight = MovingLight.valueOf(jsonObject.getAsJsonObject("MovingLight"));
            return new EffectSettings(selector, beacon, movingLight);
        }
    }

    public record Stage(String information, String noCurrentSong, String termsOfService, Location location, double radius) {
        public static Stage valueOf(JsonObject jsonObject) {
            String information = jsonObject.get("Information").getAsString();
            String noCurrentSong = jsonObject.get("NoCurrentSong").getAsString();
            String termsOfService = jsonObject.get("TermsOfService").getAsString();
            Location location = ConfigHandler.translateLocation(jsonObject.getAsJsonObject("Location"));
            double radius = jsonObject.get("Radius").getAsDouble();
            return new Stage(information, noCurrentSong, termsOfService, location, radius);
        }
    }

    private Map<Integer, String> getFileIdentifierMap(JsonArray jsonArray) {
        Map<Integer, String> map = new HashMap<>();
        for(JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int id = jsonObject.get("Id").getAsInt();
            String file = jsonObject.get("Filename").getAsString();
            map.put(id, file);
        }
        return map;
    }
}
