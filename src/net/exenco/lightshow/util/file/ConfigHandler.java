package net.exenco.lightshow.util.file;

import com.google.gson.*;
import net.exenco.lightshow.LightShow;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ConfigHandler {
    private final File config = new File("plugins//Light-Show//config.json");

    private final File directory = new File("plugins//Light-Show");
    private final File dmxDirectory = new File(directory + "//DmxUniverses");
    private final File songDirectory = new File(directory + "//Songs");
    private final File fireworkDirectory = new File(directory + "//Fireworks");
    private final File logoDirectory = new File(directory + "//Logos");

    private final LightShow lightShow;
    public ConfigHandler(LightShow lightShow) {
        this.lightShow = lightShow;
        load();
    }

    public void load() {
        createDirectory(directory);
        createDirectory(dmxDirectory);
        createDirectory(songDirectory);
        createDirectory(fireworkDirectory);
        createDirectory(logoDirectory);

        if(!config.exists())
            lightShow.saveResource("config.json", false);
    }

    private void createDirectory(File directory) {
        if(!directory.exists())
            if(!directory.mkdir())
                lightShow.getLogger().severe("There has been an error creating directory " + directory);
    }

    public JsonObject getSongJson(String fileName) {
        File file = new File(songDirectory + "//" + fileName + ".json");
        return Objects.requireNonNull(getJsonFromFile(file)).getAsJsonObject();
    }

    public JsonArray getDmxUniverseJson(String fileName) {
        File file = new File(dmxDirectory + "//" + fileName + ".json");
        return Objects.requireNonNull(getJsonFromFile(file)).getAsJsonArray();
    }

    public JsonObject getLogoJson(String fileName) {
        File file = new File(logoDirectory + "//" + fileName + ".json");
        return Objects.requireNonNull(getJsonFromFile(file)).getAsJsonObject();
    }

    public JsonObject getFireworkJson(String fileName) {
        File file = new File( fireworkDirectory + "//" + fileName + ".json");
        return Objects.requireNonNull(getJsonFromFile(file)).getAsJsonObject();
    }

    public JsonObject getConfigJson() {
        return Objects.requireNonNull(getJsonFromFile(config)).getAsJsonObject();
    }

    private JsonElement getJsonFromFile(File file) {
        if(!file.exists()) {
            lightShow.getLogger().warning("Requested file " + file + " does not exist!");
            return null;
        }

        try {
            JsonParser jsonParser = new JsonParser();
            FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
            return jsonParser.parse(fileReader);
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

    public static net.minecraft.world.item.ItemStack translateNMSFirework(JsonObject jsonObject) {
        ItemStack itemStack = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta fireworkMeta = (FireworkMeta) itemStack.getItemMeta();
        if(fireworkMeta == null)
            return null;

        JsonArray effectsArray = jsonObject.getAsJsonArray("Effects");
        for(JsonElement jsonElement : effectsArray) {
            fireworkMeta.addEffect(translateFireworkEffect(jsonElement.getAsJsonObject()));
        }

        int power = jsonObject.get("Power").getAsInt();
        fireworkMeta.setPower(power);

        itemStack.setItemMeta(fireworkMeta);
        return CraftItemStack.asNMSCopy(itemStack);
    }

    private static FireworkEffect translateFireworkEffect(JsonObject jsonObject) {
        FireworkEffect.Builder builder = FireworkEffect.builder();
        if(jsonObject.has("Type"))
            builder.with(FireworkEffect.Type.valueOf(jsonObject.get("Type").getAsString().toUpperCase()));
        if(jsonObject.has("Flicker"))
            builder.flicker(jsonObject.get("Flicker").getAsBoolean());
        if(jsonObject.has("Trail"))
            builder.trail(jsonObject.get("Trail").getAsBoolean());

        JsonArray colourArray = jsonObject.getAsJsonArray("Colours");
        for(JsonElement colourElement : colourArray) {
            builder.withColor(translateColor(colourElement.getAsJsonObject()));
        }

        JsonArray fadeArray = jsonObject.getAsJsonArray("Fade");
        for(JsonElement fadeElement : fadeArray) {
            builder.withFade(translateColor(fadeElement.getAsJsonObject()));
        }

        return builder.build();
    }

    public static Color translateColor(JsonObject jsonObject) {
        int red = jsonObject.get("Red").getAsInt();
        int green = jsonObject.get("Green").getAsInt();
        int blue = jsonObject.get("Blue").getAsInt();
        return Color.fromRGB(red, green, blue);
    }
}
