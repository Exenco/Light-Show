package net.exenco.lightshow.show;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exenco.lightshow.LightShowPlugin;
import net.exenco.lightshow.show.artnet.ArtNetPacket;
import net.exenco.lightshow.show.artnet.ArtNetReceiver;
import net.exenco.lightshow.show.fixtures.*;
import net.exenco.lightshow.util.DmxBuffer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiFunction;

public class ShowStage {

    private Location center;
    private double radius;

    private static final HashMap<String, BiFunction<JsonObject, ShowStage, ShowFixture>> fixtureMap = new HashMap<>();
    static {
        fixtureMap.put("BeamMovingHead", BeamMovingHeadFixture::new);
        fixtureMap.put("WashMovingHead", WashMovingHeadFixture::new);
        fixtureMap.put("Laser", LaserFixture::new);
        fixtureMap.put("Smoke", SmokeFixture::new);
        fixtureMap.put("LED", LEDFixture::new);
    }

    private final Set<Player> players;
    private final TreeMap<Integer, HashMap<Integer, List<ShowFixture>>> dmxMap;

    private final DmxBuffer dmxBuffer;
    private ArtNetReceiver artNetReceiver;

    private final LightShowPlugin plugin;
    public ShowStage(LightShowPlugin plugin) {
        this.players = new HashSet<>();
        this.dmxMap = new TreeMap<>();
        this.dmxBuffer = new DmxBuffer();

        this.plugin = plugin;
        load();
    }

    public void load() {

        FileConfiguration config = plugin.getConfig();
        this.center = new Location(
                Bukkit.getWorld(config.getString("stage.location.world", "world")),
                config.getDouble("stage.location.x"),
                config.getDouble("stage.location.y"),
                config.getDouble("stage.location.z")
        );
        this.radius = config.getDouble("stage.radius");

        if (this.artNetReceiver != null && this.artNetReceiver.isRunning()) {
            this.artNetReceiver.stop();
        }
        this.artNetReceiver = new ArtNetReceiver(plugin, this);

        for (Player player : getPlayersInRange()) {
            hideFromPlayer((CraftPlayer) player);
        }
        this.players.clear();

        this.dmxMap.clear();
        JsonArray dmxArray = plugin.getPluginFiles().loadDmxJsonArray();
        loadDmxMap(dmxArray);
    }

    private void loadDmxMap(JsonArray dmxArray) {
        if (dmxArray == null || dmxArray.isEmpty()) {
            plugin.getLogger().warning("DMX configuration is empty or null.");
            return;
        }
        for (JsonElement jsonElement : dmxArray) {
            if (!(jsonElement instanceof JsonObject jsonObject)) {
                plugin.getLogger().warning("Invalid DMX fixture configuration: " + jsonElement);
                continue;
            }

            String fixtureType = jsonObject.get("type").getAsString();
            String[] dmx = jsonObject.get("dmx").getAsString().split("-");
            int universe = Integer.parseInt(dmx[0]) - 1; // Convert to zero-based index
            int address = Integer.parseInt(dmx[1]) - 1; // Convert to zero-based index

            ShowFixture fixture = fixtureMap.get(fixtureType).apply(jsonObject, this);
            dmxMap.putIfAbsent(universe, new HashMap<>());
            dmxMap.get(universe).putIfAbsent(address, new ArrayList<>());
            dmxMap.get(universe).get(address).add(fixture);
        }
    }

    public void readPacket(byte[] message) {
        ArtNetPacket packet = ArtNetPacket.valueOf(message);
        if (packet == null) {
            return;
        }
        dmxBuffer.setDmxData(packet.getUniverseID(), packet.getDmx());
    }

    public void updateFixtures() {
        dmxMap.entrySet().parallelStream().forEach(entry ->  {
            byte[] data = dmxBuffer.getDmxData(entry.getKey());
            if (data == null) {
                return;
            }
            entry.getValue().entrySet().parallelStream().forEach(subEntry -> {
                int id = subEntry.getKey();
                subEntry.getValue().parallelStream().forEach(fixture -> {
                    int size = fixture.getDmxSize();
                    int[] dataArr = new int[size];
                    for (int x = 0; x < size; x++) {
                        dataArr[x] = data[id + x] & 0xFF;
                    }
                    fixture.applyState(dataArr);
                });
            });
        });
    }

    public Location getCenter() {
        return center;
    }

    public double getRadius() {
        return radius;
    }

    public void showToPlayer(CraftPlayer player) {
        dmxMap.values().forEach(
fixtureMap -> fixtureMap.values().forEach(
            fixtures -> fixtures.forEach(
                    fixture -> fixture.getEffects().forEach(showEffect -> showEffect.showToPlayer(player))
                )
            )
        );
    }

    public void hideFromPlayer(CraftPlayer player) {
        dmxMap.values().forEach(
                fixtureMap -> fixtureMap.values().forEach(
                        fixtures -> fixtures.forEach(
                                fixture -> fixture.getEffects().forEach(showEffect -> showEffect.hideFromPlayer(player))
                        )
                )
        );
    }


    public Set<Player> getPlayersInRange() {
        return players;
    }

    public ArtNetReceiver getArtNetReceiver() {
        return artNetReceiver;
    }

    public DmxBuffer getDmxBuffer() {
        return dmxBuffer;
    }
}
