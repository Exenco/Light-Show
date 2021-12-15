package net.exenco.lightshow.show.stage.effects;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.registries.LogoRegistry;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.api.LogoApi;

import java.util.logging.Logger;

public class LogoDisplay extends ShowEffect {
    private final LogoRegistry logoRegistry;

    private final LogoApi logoApi;
    private final double maxSize;

    private int type = -1;
    private double size = -1;

    public LogoDisplay(JsonObject jsonObject, PacketHandler packetHandler, LogoRegistry logoRegistry) {
        super(jsonObject);

        this.logoRegistry = logoRegistry;

        this.maxSize = jsonObject.has("Size") ? jsonObject.get("Size").getAsDouble() : 2.0D;
        double yaw = jsonObject.has("Yaw") ? jsonObject.get("Yaw").getAsDouble() : 0.0D;
        double pitch = jsonObject.has("Pitch") ? jsonObject.get("Pitch").getAsDouble() : 0.0D;
        this.logoApi = new LogoApi(packetHandler, yaw, pitch);
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
            logoApi.setLogo(location, logoRegistry.getLogoById(type), size);
        }
        logoApi.playLogo();
    }
}
