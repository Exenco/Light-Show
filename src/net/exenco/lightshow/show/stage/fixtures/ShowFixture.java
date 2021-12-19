package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ConfigHandler;
import org.bukkit.util.Vector;

public abstract class ShowFixture {
    protected final Vector location;
    protected final StageManager stageManager;
    protected long tickSize;
    public ShowFixture(JsonObject configJson, StageManager stageManager) {
        this.stageManager = stageManager;
        this.location = configJson.has("Location") ? ConfigHandler.translateVector(configJson.getAsJsonObject("Location")) : new Vector(0, 0, 0);
        this.tickSize = configJson.has("TickSize") ? configJson.get("TickSize").getAsInt() : 100;
    }

    public abstract int getDmxSize();

    public abstract void applyState(int[] data);

    protected double valueOf(int data) {
        return (double) data / 255;
    }
    protected double valueOfMax(double max, int data) {
        return max * ((double) data / 255);
    }
    protected int asRoundedPercentage(int data) {
        return Math.round(100.0F * ((float) data / 255));
    }
    private long millis = 0;
    protected boolean isTick() {
        long current = System.currentTimeMillis();
        boolean value = millis + tickSize < current;
        if(value)
            millis = current;
        return value;
    }
}
