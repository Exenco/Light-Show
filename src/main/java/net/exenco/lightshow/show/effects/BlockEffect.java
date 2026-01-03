package net.exenco.lightshow.show.effects;

import com.mojang.math.Transformation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.function.Supplier;

public class BlockEffect extends ShowEffect {

    protected final Display.BlockDisplay blockDisplay;

    public BlockEffect(World world, Vector origin, Vector rotation, Supplier<Collection<Player>> audienceSupplier, BlockState blockState, Vector scale) {
        super(world, origin, rotation, audienceSupplier);

        this.blockDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, this.level);
        this.blockDisplay.setPos(origin.getX(), origin.getY(), origin.getZ());
        this.blockDisplay.setBlockState(blockState);

        Quaternionf rot = new Quaternionf(defaultRotation);
        Vector3f translation = new Vector3f((float) (-scale.getX() / 2), (float) (-scale.getY() / 2), (float) (-scale.getZ() / 2));
        translation.rotate(rot);
        this.blockDisplay.setTransformation(new Transformation(
                translation,
                rot,
                scale.toVector3f(),
                new Quaternionf()
        ));

        spawnEntity(this.blockDisplay);
    }
}
