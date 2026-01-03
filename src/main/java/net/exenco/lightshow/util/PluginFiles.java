package net.exenco.lightshow.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import net.exenco.lightshow.LightShowPlugin;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.TreeMap;

public class PluginFiles {

    private static final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();

    private static final File DIRECTORY = new File("plugins//lightshow");
    private static final File CONFIG_FILE = new File(DIRECTORY + "//config.yml");
    private static final File FIXTURES_FILE = new File(DIRECTORY + "//fixtures.json");

    private final LightShowPlugin plugin;

    public PluginFiles(LightShowPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        if (!DIRECTORY.exists()) {
            if (!DIRECTORY.mkdir()) {
                plugin.getLogger().severe("There has been an error creating directory " + DIRECTORY);
            }
        }
        if (!CONFIG_FILE.exists()) {
            plugin.saveResource("config.yml", false);
        }
        if (!FIXTURES_FILE.exists()) {
            plugin.saveResource("fixtures.json", false);
        }
    }

    public JsonArray loadDmxJsonArray() {
        if (!FIXTURES_FILE.exists()) {
            return null;
        }
        try (FileReader reader = new FileReader(FIXTURES_FILE)) {
            return gson.fromJson(reader, JsonArray.class);
        } catch (IOException e) {
            plugin.getLogger().severe("There has been an error reading " + FIXTURES_FILE);
            return null;
        }
    }
}
