package net.exenco.lightshow.show.stage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.show.artnet.ArtNetClient;
import net.exenco.lightshow.show.song.SongManager;
import net.exenco.lightshow.show.stage.effects.*;
import net.exenco.lightshow.util.*;
import net.exenco.lightshow.util.file.ConfigHandler;
import net.exenco.lightshow.util.registries.FireworkRegistry;
import net.exenco.lightshow.util.registries.LogoRegistry;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class StageManager {
    private final TreeMap<Integer, HashMap<Integer, ArrayList<ShowEffect>>> dmxMap = new TreeMap<>();
    private final ArtNetClient artNetClient;
    private DmxReader dmxReader;

    private final LightShow lightShow;
    private final ConfigHandler configHandler;
    private final ShowSettings showSettings;
    private final SongManager songManager;
    private final PacketHandler packetHandler;
    private final FireworkRegistry fireworkRegistry;
    private final LogoRegistry logoRegistry;
    public StageManager(LightShow lightShow, ConfigHandler configHandler, ShowSettings showSettings, SongManager songManager,
                        PacketHandler packetHandler, FireworkRegistry fireworkRegistry, LogoRegistry logoRegistry) {
        this.artNetClient = new ArtNetClient(lightShow, showSettings.artNet().port());

        this.lightShow = lightShow;
        this.configHandler = configHandler;
        this.showSettings = showSettings;
        this.songManager = songManager;
        this.packetHandler = packetHandler;
        this.fireworkRegistry = fireworkRegistry;
        this.logoRegistry = logoRegistry;

        load();
    }

    public void load() {
        dmxMap.clear();
        for(Map.Entry<Integer, String> entry : showSettings.dmxUniverseMap().entrySet()) {
            int universeId = entry.getKey();
            JsonArray jsonArray = configHandler.getDmxUniverseJson(entry.getValue());

            for(JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                int id = jsonObject.get("DmxId").getAsInt() - 1;
                int universe = universeId - 1;

                if(id < 0 || universeId < 0)
                    throw new IllegalArgumentException("There is no such Dmx-Channel: " + universeId + "-" + id);

                if(!dmxMap.containsKey(universe))
                    dmxMap.put(universe, new HashMap<>());

                HashMap<Integer, ArrayList<ShowEffect>> subMap = dmxMap.get(universe);
                String type = jsonObject.get("DmxType").getAsString();

                switch(type) {
                    case "SongSelector" -> put(subMap, id, new SongSelector(showSettings, songManager));
                    case "MovingHead" -> put(subMap, id, new MovingHead(jsonObject, showSettings, packetHandler));
                    case "Beacon" -> put(subMap, id, new BeaconBeam(jsonObject, showSettings, packetHandler));
                    case "FireworkLauncher" -> put(subMap, id, new FireworkLauncher(jsonObject, packetHandler, fireworkRegistry));
                    case "ParticleFlare" -> put(subMap, id, new ParticleFlare(jsonObject, packetHandler));
                    case "LogoDisplay" -> put(subMap, id, new LogoDisplay(jsonObject, packetHandler, logoRegistry));
                    case "FogMachine" -> put(subMap, id, new FogMachine(jsonObject, packetHandler));
                    case "BlockUpdater" -> put(subMap, id, new BlockUpdater(jsonObject, packetHandler));
                    default -> lightShow.getLogger().warning("Given Dmx-Type " + type + " is not a valid type.");
                }
            }
        }
    }

    private void put(HashMap<Integer, ArrayList<ShowEffect>> map, int id, ShowEffect showEffect) {
        if(!map.containsKey(id))
            map.put(id, new ArrayList<>());
        map.get(id).add(showEffect);
    }

    public boolean start() {
        String address = showSettings.artNet().ip();
        if(dmxReader != null) {
            return this.artNetClient.start(address);
        }

        if(this.artNetClient.start(address)) {
            this.dmxReader = new DmxReader();
            this.dmxReader.runTaskAsynchronously(lightShow);
            return true;
        }
        return false;
    }

    public boolean stop() {
        if(dmxReader == null)
            return this.artNetClient.stop();

        dmxReader.cancel();
        dmxReader = null;
        return this.artNetClient.stop();
    }

    private class DmxReader extends BukkitRunnable {
        private boolean running;
        @Override
        public void run() {
            running = true;
            while(running) {
                for (Map.Entry<Integer, HashMap<Integer, ArrayList<ShowEffect>>> entry : dmxMap.entrySet()) {
                    byte[] data = artNetClient.readDmx(entry.getKey());
                    for (Map.Entry<Integer, ArrayList<ShowEffect>> subEntry : entry.getValue().entrySet()) {
                        int id = subEntry.getKey();
                        for(ShowEffect showEffect : subEntry.getValue()) {
                            int size = showEffect.getDmxSize();
                            int[] dataArr = new int[size];
                            for (int x = 0; x < size; x++)
                                dataArr[x] = (data[id + x] & 0xFF);
                            showEffect.applyState(dataArr);
                        }
                    }
                }
            }
        }

        @Override
        public synchronized void cancel() throws IllegalStateException {
            running = false;
            super.cancel();
        }
    }
}
