package net.exenco.lightshow.show.stage.effects;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.file.ConfigHandler;
import org.bukkit.util.Vector;

public abstract class ShowEffect {
    protected final Vector location;
    protected long tickSize = 100;
    public ShowEffect(JsonObject jsonObject) {
        if(!jsonObject.has("Location")) {
            this.location = new Vector(0, 0, 0);
            return;
        }
        this.location = ConfigHandler.translateVector(jsonObject.getAsJsonObject("Location"));
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
