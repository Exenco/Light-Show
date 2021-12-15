package net.exenco.lightshow.util.registries;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.ShowSettings;
import net.exenco.lightshow.util.file.ConfigHandler;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FireworkRegistry {
    private final HashMap<Integer, ItemStack> fireworkItemStacks = new HashMap<>();

    private final ConfigHandler configHandler;
    private final ShowSettings showSettings;
    public FireworkRegistry(ConfigHandler configHandler, ShowSettings showSettings) {
        this.configHandler = configHandler;
        this.showSettings = showSettings;

        load();
    }

    public void load() {
        fireworkItemStacks.clear();
        for(Map.Entry<Integer, String> entry : showSettings.fireworkMap().entrySet()) {
            int id = entry.getKey();
            JsonObject jsonObject = configHandler.getFireworkJson(entry.getValue());
            ItemStack itemStack = ConfigHandler.translateNMSFirework(jsonObject);
            fireworkItemStacks.put(id, itemStack);
        }
    }

    public ItemStack getFireworkById(int id) {
        int size = fireworkItemStacks.size();
        if(id == 0 && size > 0) {
            int i = 0;
            int random = ThreadLocalRandom.current().nextInt(size);
            for(ItemStack itemStack : fireworkItemStacks.values())
                if(i == random)
                    return itemStack;
                else
                    i++;
        }
        return fireworkItemStacks.get(id);
    }
}
