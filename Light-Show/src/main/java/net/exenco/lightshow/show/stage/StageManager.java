package net.exenco.lightshow.show.stage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.show.artnet.ArtNetPacket;
import net.exenco.lightshow.show.artnet.DmxBuffer;
import net.exenco.lightshow.show.artnet.ArtNetReceiver;
import net.exenco.lightshow.show.song.SongManager;
import net.exenco.lightshow.show.stage.fixtures.*;
import net.exenco.lightshow.util.*;
import net.exenco.lightshow.util.ConfigHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class StageManager {

    /* Fixture types */
    private final HashMap<String, Class<? extends ShowFixture>> fixtureMap = new HashMap<>();

    /* channel mapping */
    private final TreeMap<Integer, HashMap<Integer, ArrayList<ShowFixture>>> dmxMap = new TreeMap<>();

    /* Art-Net */
    private final DmxBuffer dmxBuffer;
    private boolean receiving;
    private ArtNetReceiver artNetReceiver;

    private final LightShow lightShow;
    private final ConfigHandler configHandler;
    private final ShowSettings showSettings;
    private final SongManager songManager;
    private final PacketHandler packetHandler;
    public StageManager(LightShow lightShow, ConfigHandler configHandler, ShowSettings showSettings, SongManager songManager, PacketHandler packetHandler) {
        this.lightShow = lightShow;
        this.configHandler = configHandler;
        this.showSettings = showSettings;
        this.songManager = songManager;
        this.packetHandler = packetHandler;

        this.dmxBuffer = new DmxBuffer();
    }

    public void load() {
        this.artNetReceiver = new ArtNetReceiver(this, showSettings);

        dmxMap.clear();
        for (ShowSettings.DmxEntry dmxEntry : showSettings.dmxEntryList()) {
            int universeId = dmxEntry.universe();
            JsonArray jsonArray = configHandler.getDmxEntriesJson(dmxEntry.filename());

            for (JsonElement jsonElement : jsonArray) {
                JsonObject configJson = jsonElement.getAsJsonObject();

                int id = configJson.get("DmxId").getAsInt() - 1 + dmxEntry.offset();
                int universe = universeId - 1;

                if (id < 0 || universeId < 0) {
                    throw new IllegalArgumentException("There is no such Dmx-Channel: " + universeId + "-" + id);
                }

                if (!dmxMap.containsKey(universe)) {
                    dmxMap.put(universe, new HashMap<>());
                }

                HashMap<Integer, ArrayList<ShowFixture>> subMap = dmxMap.get(universe);
                String type = configJson.get("DmxType").getAsString();

                if (!fixtureMap.containsKey(type)) {
                    lightShow.getLogger().warning("Given Dmx-Type " + type + " is not a valid type.");
                }

                // Using reflections to dynamically create fixture object
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

    public void receiveArtNet(byte[] message) {
        ArtNetPacket packet = ArtNetPacket.valueOf(message);
        if (packet == null) {
            return;
        }
        this.receiving = true;
        dmxBuffer.setDmxData(packet.getUniverseID(), packet.getDmx());
    }

    public void registerFixture(String key, Class<? extends ShowFixture> clazz) {
        fixtureMap.put(key, clazz);
    }

    public boolean start() {
        if (artNetReceiver.isRunning()) {
            return false;
        }
        return artNetReceiver.start();
    }

    public boolean stop() {
        if (!artNetReceiver.isRunning()) {
            return false;
        }
        return artNetReceiver.stop();
    }

    public boolean confirmReceiving() {
        int timeout = showSettings.artNet().timeout();
        this.receiving = false;
        long start = System.currentTimeMillis();
        long current = start;
        while (!receiving) {
            Thread.onSpinWait();
            if (current >= start + timeout) {
                break;
            }
            current = System.currentTimeMillis();
        }
        return receiving;
    }

    public void updateFixtures() {
        dmxMap.entrySet().parallelStream().forEach(entry ->  {
            byte[] data = dmxBuffer.getDmxData(entry.getKey());
            entry.getValue().entrySet().parallelStream().forEach(subEntry -> {
                int id = subEntry.getKey();
                subEntry.getValue().parallelStream().forEach(fixture -> {
                    int size = fixture.getDmxSize();
                    int[] dataArr = new int[size];
                    for (int x = 0; x < size; x++)
                        dataArr[x] = (data[id + x] & 0xFF);
                    fixture.applyState(dataArr);
                });
            });
        });
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

    public LightShow getLightShow() {
        return lightShow;
    }
}
