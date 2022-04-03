package net.exenco.showcontroller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ShowConfig {

    private static final File configFile = new File("config//show_controller_config.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static void createConfig() {
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("Address", "-");
                jsonObject.addProperty("Port", "6454");
                writeConfig(jsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File getFile() {
        return configFile;
    }

    public static JsonObject getConfig() {
        try {
            FileReader fileReader = new FileReader(configFile, StandardCharsets.UTF_8);
            return JsonParser.parseReader(fileReader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeConfig(JsonObject jsonObject) {
        try {
            String str = gson.toJson(jsonObject);
            FileWriter fileWriter = new FileWriter(configFile, StandardCharsets.UTF_8);
            fileWriter.write(str);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
