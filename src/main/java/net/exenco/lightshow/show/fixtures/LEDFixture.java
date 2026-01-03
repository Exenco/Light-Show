package net.exenco.lightshow.show.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.ShowStage;
import net.exenco.lightshow.show.effects.SurfaceEffect;
import org.bukkit.Color;
import org.bukkit.util.Vector;

public class LEDFixture extends ShowFixture {

    protected SurfaceEffect surfaceEffect;

    public LEDFixture(JsonObject configJson, ShowStage showStage) {
        super(configJson, showStage);
        float scaleWidth = configJson.has("width") ? configJson.get("width").getAsFloat() : 1.0f;
        float scaleHeight = configJson.has("height") ? configJson.get("height").getAsFloat() : 1.0f;

        rotation.add(new Vector( -90, 0, 0));

        this.surfaceEffect = new SurfaceEffect(this.world, this.location, this.rotation, showStage::getPlayersInRange, scaleWidth, scaleHeight);

        effects.add(surfaceEffect);
    }

    @Override
    protected Vector getDefaultOffset() {
        return new Vector(0,0,0);
    }

    @Override
    public int getDmxSize() {
        return 4;
    }

    @Override
    public void applyState(int[] data) {
        int red = data[0];
        int green = data[1];
        int blue = data[2];
        int dim = data[3];

        surfaceEffect.updateColour(Color.fromARGB(dim, red, green, blue));
        surfaceEffect.releaseUpdate();
    }
}
