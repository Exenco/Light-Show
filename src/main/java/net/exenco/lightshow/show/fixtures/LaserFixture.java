package net.exenco.lightshow.show.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.ShowStage;
import net.exenco.lightshow.show.effects.BlockEffect;
import net.exenco.lightshow.show.effects.RectangleEffect;
import net.exenco.lightshow.util.VectorUtils;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class LaserFixture extends ShowFixture {

    protected BlockEffect blockEffect;
    protected RectangleEffect rectangleEffect;

    public LaserFixture(JsonObject configJson, ShowStage showStage) {
        super(configJson, showStage);

        this.blockEffect = new BlockEffect(
                this.world,
                this.location.clone().subtract(getDefaultOffset()),
                this.rotation.clone().add(new Vector(90, 0, 0)),
                showStage::getPlayersInRange,
                Blocks.OBSERVER.defaultBlockState(),
                new Vector(0.4f, 0.35f, 0.4f)
        );

        Vector laserOffset = this.getDefaultOffset().clone().add(new Vector(0.0f, 0.0f, -0.2f));

        this.rectangleEffect = new RectangleEffect(
                this.world,
                this.location.subtract(laserOffset),
                this.rotation.add(new Vector(90, 90, 0)),
                showStage::getPlayersInRange
        );
        this.rectangleEffect.updateScale(0.035f);
//        this.rectangleEffect.updateYOffset(0.2f);

        this.effects.add(blockEffect);
        this.effects.add(rectangleEffect);
    }

    @Override
    protected Vector getDefaultOffset() {
        return new Vector(0, -0.175, 0);
    }

    @Override
    public int getDmxSize() {
        return 9;
    }

    @Override
    public void applyState(int[] data) {
        int dim = data[0];
        float pan = (180 * (float) (data[1] << 8 | data[2]) / 65535) - 180;
        float tilt = (180 * (float) (data[3] << 8 | data[4]) / 65535) - 180;
        int red = data[5];
        int green = data[6];
        int blue = data[7];

        Vector vector = VectorUtils.rayTrace(this.location, this.rotation, pan, tilt, showStage.getRadius(), this.world);
        Location target = vector.toLocation(world);

        this.rectangleEffect.updateTarget(target);
        this.rectangleEffect.updateColor(Color.fromARGB(dim, red, green, blue));
        this.rectangleEffect.releaseUpdate();
    }
}
