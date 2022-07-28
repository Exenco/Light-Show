package net.exenco.lightshow.util;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.exenco.lightshow.LightShow;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ConfigHandler {
    private final File config = new File("plugins//Light-Show//config.json");

    private final File directory = new File("plugins//Light-Show");
    private final File dmxDirectory = new File(directory + "//DmxEntries");

    private final LightShow lightShow;
    public ConfigHandler(LightShow lightShow) {
        this.lightShow = lightShow;
        load();
    }

    public void load() {
        createDirectory(directory);
        createDirectory(dmxDirectory);

        if(!config.exists())
            lightShow.saveResource("config.json", false);
    }

    public void createDirectory(File directory) {
        if(!directory.exists())
            if(!directory.mkdir())
                lightShow.getLogger().severe("There has been an error creating directory " + directory);
    }

    public JsonArray getDmxEntriesJson(String fileName) {
        File file = new File(dmxDirectory + "//" + fileName + ".json");
        return Objects.requireNonNull(getJsonFromFile(file)).getAsJsonArray();
    }

    public JsonObject getConfigJson() {
        return Objects.requireNonNull(getJsonFromFile(config)).getAsJsonObject();
    }

    public JsonElement getJsonFromFile(File file) {
        if(!file.exists()) {
            lightShow.getLogger().warning("Requested file " + file + " does not exist!");
            return null;
        }
        try {
            FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
            return JsonParser.parseReader(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Vector translateVector(JsonObject jsonObject) {
        if(jsonObject == null)
            return null;

        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        double z = jsonObject.get("z").getAsDouble();
        return new Vector(x, y, z);
    }

    public static Location translateLocation(JsonObject jsonObject) {
        if(jsonObject == null)
            return null;

        World world = jsonObject.has("world") ? Bukkit.getWorld(jsonObject.get("world").getAsString()) : null;
        double x = jsonObject.get("x").getAsDouble();
        double y = jsonObject.get("y").getAsDouble();
        double z = jsonObject.get("z").getAsDouble();
        float yaw = jsonObject.has("yaw") ? jsonObject.get("yaw").getAsFloat() : 0.0F;
        float pitch = jsonObject.has("pitch") ? jsonObject.get("pitch").getAsFloat() : 0.0F;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public static Color translateColor(JsonObject jsonObject) {
        int red = jsonObject.get("Red").getAsInt();
        int green = jsonObject.get("Green").getAsInt();
        int blue = jsonObject.get("Blue").getAsInt();
        return Color.fromRGB(red, green, blue);
    }

    public static ItemStack getItemStackFromJsonObject(JsonObject jsonObject) {
        String item = jsonObject.get("Item").getAsString();
        int count = jsonObject.has("Count") ? jsonObject.get("Count").getAsInt() : 1;
        String nbt = jsonObject.has("Nbt") ? jsonObject.get("Nbt").getAsString() : "{}";
        try {
            CompoundTag itemNbt = TagParser.parseTag(nbt);

            CompoundTag nbtTag = new CompoundTag();
            nbtTag.putString("id", item.toLowerCase());
            nbtTag.putInt("Count", count);
            nbtTag.put("tag", itemNbt);
            return ItemStack.of(nbtTag);
        } catch (CommandSyntaxException e) {
            throw new RuntimeException("Cannot parse item " + count + "x " + item + nbt);
        }
    }

    public static Material getMaterialFromName(String name) {
        CompoundTag nbtTagCompound = new CompoundTag();
        nbtTagCompound.putString("id", name.toLowerCase());
        nbtTagCompound.putInt("Count", 1);
        nbtTagCompound.putString("tag", "{}");
        return CraftItemStack.asBukkitCopy(ItemStack.of(nbtTagCompound)).getType();
    }
}
