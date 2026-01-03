package net.exenco.lightshow.show.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.ShowStage;
import net.exenco.lightshow.show.effects.ShowEffect;
import net.exenco.lightshow.util.VectorUtils;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class ShowFixture {

    protected final World world;
    protected final Vector location;

    protected final Vector rotation;
    protected final Vector offset;
    protected final float scale;

    protected final List<ShowEffect> effects;

    protected final ShowStage showStage;
    protected ShowFixture(JsonObject configJson, ShowStage showStage) {
        this.showStage = showStage;

        this.effects = new ArrayList<>();
        this.world = showStage.getCenter().getWorld();

        JsonObject transformationJson = configJson.getAsJsonObject("transformation");
        this.location = parseLocation(transformationJson);
        this.rotation = parseRotation(transformationJson);
        this.scale = transformationJson.has("scale") ? transformationJson.get("scale").getAsFloat() : 1.0f;

        this.offset = VectorUtils.rotateVectorByVector(getDefaultOffset().multiply(scale), rotation);
    }

    protected abstract Vector getDefaultOffset();

    public abstract int getDmxSize();

    public abstract void applyState(int[] data);

    public List<ShowEffect> getEffects() {
        return effects;
    }

    private Vector parseLocation(JsonObject locationJson) {
        if (locationJson == null) {
            return new Vector(0, 0, 0);
        }
        return new Vector(
            locationJson.get("x").getAsDouble(),
            locationJson.get("y").getAsDouble(),
            locationJson.get("z").getAsDouble()
        );
    }

    private Vector parseRotation(JsonObject rotationJson) {
        if (rotationJson == null) {
            return new Vector(0, 0, 0);
        }
        return new Vector(
                rotationJson.has("rotX") ? rotationJson.get("rotX").getAsDouble() : 0,
                rotationJson.has("rotY") ? rotationJson.get("rotY").getAsDouble() : 0,
                rotationJson.has("rotZ") ? rotationJson.get("rotZ").getAsDouble() : 0
        );
    }
}
