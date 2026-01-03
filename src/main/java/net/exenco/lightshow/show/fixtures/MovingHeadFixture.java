package net.exenco.lightshow.show.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.ShowStage;
import net.exenco.lightshow.show.effects.MovingHeadEffect;
import org.bukkit.util.Vector;

public abstract class MovingHeadFixture extends ShowFixture {

    protected final MovingHeadEffect movingHeadEffect;

    protected MovingHeadFixture(JsonObject configJson, ShowStage showStage, float scale) {
        super(configJson, showStage);

        this.movingHeadEffect = new MovingHeadEffect(this.world, this.location.add(offset), this.rotation, showStage::getPlayersInRange);
        movingHeadEffect.updateScale(scale * this.scale);
        effects.add(movingHeadEffect);
    }

    @Override
    protected Vector getDefaultOffset() {
        return new Vector(0, 0.34, 0);
    }

}
