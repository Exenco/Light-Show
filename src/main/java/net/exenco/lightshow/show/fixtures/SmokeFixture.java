package net.exenco.lightshow.show.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.ShowStage;
import net.exenco.lightshow.show.effects.BlockEffect;
import net.exenco.lightshow.show.effects.ParticleEffect;
import net.exenco.lightshow.util.VectorUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

public class SmokeFixture extends ShowFixture{

    protected BlockEffect blockEffect;
    protected ParticleEffect particleEffect;

    public SmokeFixture(JsonObject configJson, ShowStage showStage) {
        super(configJson, showStage);

        BlockState blockState = Blocks.DISPENSER.defaultBlockState();
        blockState = blockState.setValue(DispenserBlock.FACING, Direction.DOWN);

        this.rotation.add(new Vector(180, 0, 0));

        this.blockEffect = new BlockEffect(
                this.world,
                this.location,
                this.rotation,
                showStage::getPlayersInRange,
                blockState,
                new Vector(0.35, 0.35, 0.35)
        );
        this.effects.add(blockEffect);

        // add offset of 0.2 to the location to avoid particles spawning inside the block but make it be rotated with the fixture
        this.location.add(VectorUtils.rotateVectorByVector(new Vector(0, -0.35, 0), this.rotation));

        Vector particleRotation = new Vector(this.rotation.getX(), this.rotation.getY(), this.rotation.getZ());
        this.particleEffect = new ParticleEffect(
                this.world,
                this.location,
                particleRotation,
                showStage::getPlayersInRange,
                Particle.CLOUD,
                new Vector(0.125, 0.125, 0.125)
        );
    }

    @Override
    protected Vector getDefaultOffset() {
        return new Vector(0, 0, 0);
    }

    @Override
    public int getDmxSize() {
        return 2;
    }

    @Override
    public void applyState(int[] data) {
        int smokeIntensity = data[0];
        float fan = (float) data[1] / 255 * 3;

        for (int i = 0; i < smokeIntensity; i++) {
            particleEffect.spawnParticle(fan);
        }
    }
}
