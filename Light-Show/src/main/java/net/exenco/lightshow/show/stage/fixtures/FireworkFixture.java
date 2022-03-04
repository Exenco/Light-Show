package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ConfigHandler;
import net.exenco.lightshow.util.PacketHandler;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FireworkFixture extends ShowFixture {

    private boolean fire = true;

    private final PacketHandler packetHandler;
    private final HashMap<Integer, List<ItemStack>> fireworksMap = new HashMap<>();
    public FireworkFixture(JsonObject configJson, StageManager stageManager) {
        super(configJson, stageManager);
        ConfigHandler configHandler = stageManager.getConfigHandler();
        this.packetHandler = stageManager.getPacketHandler();

        File config = new File("plugins//Light-Show//fireworks.json");
        if(!config.exists()) {
            try {
                if(!config.createNewFile())
                    throw new IOException("Could not create fireworks.json!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JsonArray jsonArray = configHandler.getJsonFromFile(config).getAsJsonArray();
        for(JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int id = jsonObject.get("Id").getAsInt();

            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("Item", "minecraft:firework_rocket");
            itemJson.addProperty("Count", 1);
            itemJson.addProperty("Nbt", jsonObject.get("FireworkNbt").getAsString());
            ItemStack itemStack = ConfigHandler.getItemStackFromJsonObject(itemJson);
            if(!fireworksMap.containsKey(id))
                fireworksMap.put(id, new ArrayList<>());
            fireworksMap.get(id).add(itemStack);
        }
        this.tickSize = configJson.has("TickSize") ? configJson.get("TickSize").getAsInt() : 200;
    }

    @Override
    public int getDmxSize() {
        return 2;
    }

    @Override
    public void applyState(int[] data) {
        boolean spawn = data[0] > 0;

        if(!spawn) {
            fire = true;
            return;
        }

        if(data[0] == 255 && isTick())
            fire = true;

        if(!fire)
            return;

        fire = false;
        int id = data[1];

        if(fireworksMap.containsKey(id)) {
            for (ItemStack itemStack : fireworksMap.get(id)) {
                FireworkRocketEntity entityFireworks = new FireworkRocketEntity(packetHandler.getLevel(), location.getX(), location.getY(), location.getZ(), itemStack);
                this.packetHandler.spawnFirework(entityFireworks);
            }
        }
    }
}
