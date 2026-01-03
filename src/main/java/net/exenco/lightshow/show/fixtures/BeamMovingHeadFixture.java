package net.exenco.lightshow.show.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.ShowStage;
import net.exenco.lightshow.show.effects.RectangleEffect;
import net.exenco.lightshow.util.VectorUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class BeamMovingHeadFixture extends MovingHeadFixture {

    protected final RectangleEffect rectangleEffect;

    protected int shutterState = 0;
    protected long shutterTime = 0;

    public BeamMovingHeadFixture(JsonObject configJson, ShowStage showStage) {
        super(configJson, showStage, 0.75f);

        this.rectangleEffect = new RectangleEffect(this.world, this.location, this.rotation, showStage::getPlayersInRange);
        this.rectangleEffect.updateScale(0.275f * scale);
        this.rectangleEffect.updateYOffset(0.2f * scale);

        this.effects.add(rectangleEffect);
    }

    @Override
    public int getDmxSize() {
        return 9;
    }

    @Override
    public void applyState(int[] data) {
        int dim = data[0];
        float pan = (360 * (float) (data[1] << 8 | data[2]) / 65535) - 180;
        float tilt = (360 * (float) (data[3] << 8 | data[4]) / 65535) - 180;
        int red = data[5];
        int green = data[6];
        int blue = data[7];
        int shutter = 256 - data[8];

        long now = System.currentTimeMillis();

        if (shutter < 256) {
            int interval = (int)(shutter * 1000f / 255);
            if (now - shutterTime >= interval) {
                shutterState ^= 1;
                shutterTime = now;
            }
            if (shutterState != 0) dim = 0;
        } else {
            shutterState = 0;
            shutterTime = now;
        }

        Vector vector = VectorUtils.rayTrace(this.location, this.rotation, pan, tilt, showStage.getRadius(), this.world);
        Location target = vector.toLocation(world);

        this.rectangleEffect.updateTarget(target);
        this.rectangleEffect.updateColor(Color.fromARGB(dim, red, green, blue));
        this.rectangleEffect.releaseUpdate();

        this.movingHeadEffect.updateTarget(target);
        this.movingHeadEffect.releaseUpdate();
    }
}
