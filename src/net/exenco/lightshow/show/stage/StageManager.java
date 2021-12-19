package net.exenco.lightshow.show.stage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.show.artnet.ArtNetClient;
import net.exenco.lightshow.show.song.SongManager;
import net.exenco.lightshow.show.stage.fixtures.*;
import net.exenco.lightshow.util.*;
import net.exenco.lightshow.util.ConfigHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class StageManager {

    private final HashMap<String, Class<? extends ShowFixture>> fixtureMap = new HashMap<>();

    private final TreeMap<Integer, HashMap<Integer, ArrayList<ShowFixture>>> dmxMap = new TreeMap<>();
    private final ArtNetClient artNetClient;
    private DmxReader dmxReader;

    private final LightShow lightShow;
    private final ConfigHandler configHandler;
    private final ShowSettings showSettings;
    private final SongManager songManager;
    private final PacketHandler packetHandler;
    public StageManager(LightShow lightShow, ConfigHandler configHandler, ShowSettings showSettings, SongManager songManager,
                        PacketHandler packetHandler) {
        this.artNetClient = new ArtNetClient(lightShow);

        this.lightShow = lightShow;
        this.configHandler = configHandler;
        this.showSettings = showSettings;
        this.songManager = songManager;
        this.packetHandler = packetHandler;
    }

    public void load() {
        dmxMap.clear();
        for(ShowSettings.DmxEntry dmxEntry : showSettings.dmxEntryList()) {
            int universeId = dmxEntry.universe();
            JsonArray jsonArray = configHandler.getDmxEntriesJson(dmxEntry.filename());

            for(JsonElement jsonElement : jsonArray) {
                JsonObject configJson = jsonElement.getAsJsonObject();

                int id = configJson.get("DmxId").getAsInt() - 1 + dmxEntry.offset();
                int universe = universeId - 1;

                if(id < 0 || universeId < 0)
                    throw new IllegalArgumentException("There is no such Dmx-Channel: " + universeId + "-" + id);

                if(!dmxMap.containsKey(universe))
                    dmxMap.put(universe, new HashMap<>());

                HashMap<Integer, ArrayList<ShowFixture>> subMap = dmxMap.get(universe);
                String type = configJson.get("DmxType").getAsString();

                if(!fixtureMap.containsKey(type))
                    lightShow.getLogger().warning("Given Dmx-Type " + type + " is not a valid type.");

                try {
                    Class<? extends ShowFixture> clazz = fixtureMap.get(type);
                    if(clazz == null)
                        continue;

                    if(!subMap.containsKey(id))
                        subMap.put(id, new ArrayList<>());
                    ShowFixture fixture = clazz.getDeclaredConstructor(JsonObject.class, StageManager.class).newInstance(configJson, this);
                    subMap.get(id).add(fixture);
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void registerFixture(String key, Class<? extends ShowFixture> clazz) {
        fixtureMap.put(key, clazz);
    }

    public boolean start() {
        String address = showSettings.artNet().ip();
        int port = showSettings.artNet().port();
        if(dmxReader != null) {
            return this.artNetClient.start(address, port);
        }

        if(this.artNetClient.start(address, port)) {
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
                for (Map.Entry<Integer, HashMap<Integer, ArrayList<ShowFixture>>> entry : dmxMap.entrySet()) {
                    byte[] data = artNetClient.readDmx(entry.getKey());
                    for (Map.Entry<Integer, ArrayList<ShowFixture>> subEntry : entry.getValue().entrySet()) {
                        int id = subEntry.getKey();
                        for(ShowFixture showEffect : subEntry.getValue()) {
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

    public ShowSettings getShowSettings() {
        return showSettings;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public SongManager getSongManager() {
        return songManager;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }
}
