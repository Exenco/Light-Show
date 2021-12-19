package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.api.LogoApi;
import net.exenco.lightshow.util.ConfigHandler;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class LogoFixture extends ShowFixture {

    private final HashMap<Integer, LogoApi.Logo> logoMap = new HashMap<>();

    private final LogoApi logoApi;
    private final double maxSize;

    private int type = -1;
    private double size = -1;

    public LogoFixture(JsonObject configJson, StageManager stageManager) {
        super(configJson, stageManager);
        ConfigHandler configHandler = stageManager.getConfigHandler();

        File directory = new File("plugins//Light-Show//Logos");
        configHandler.createDirectory(directory);

        for(File file : Objects.requireNonNull(directory.listFiles())) {
            if(!file.getName().endsWith(".json"))
                continue;
            try {
                JsonObject jsonObject = configHandler.getJsonFromFile(file).getAsJsonObject();
                int id = jsonObject.get("Id").getAsInt();
                LogoApi.Logo logo = new LogoApi.Logo(jsonObject);
                logoMap.put(id, logo);
            } catch(Exception ignored) {}
        }

        this.maxSize = configJson.has("Size") ? configJson.get("Size").getAsDouble() : 2.0D;
        double yaw = configJson.has("Yaw") ? configJson.get("Yaw").getAsDouble() : 0.0D;
        double pitch = configJson.has("Pitch") ? configJson.get("Pitch").getAsDouble() : 0.0D;
        this.logoApi = new LogoApi(stageManager.getPacketHandler(), yaw, pitch);
    }

    @Override
    public int getDmxSize() {
        return 3;
    }

    @Override
    public void applyState(int[] data) {
        int enabled = data[0];
        int type = data[1];
        double size = maxSize * ((double) data[2] / 255);

        if(enabled == 0 || !isTick())
            return;

        if(this.type != type || this.size != size) {
            this.type = type;
            this.size = size;

            logoApi.setLogo(location, logoMap.get(type), size);
        }
        logoApi.playLogo();
    }
}
