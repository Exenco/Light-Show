package net.exenco.lightshow.show.effects;

import com.mojang.math.Transformation;
import net.exenco.lightshow.util.VectorUtils;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collection;
import java.util.function.Supplier;

public class MovingHeadEffect extends HeadEffect {

    private final Display.BlockDisplay baseDisplay;
    private final Display.BlockDisplay leftStrutDisplay;
    private final Display.BlockDisplay rightStrutDisplay;

    public MovingHeadEffect(World world, Vector origin, Vector rotation, Supplier<Collection<Player>> audienceSupplier) {
        super(world, origin, rotation, audienceSupplier);

        this.baseDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        this.baseDisplay.setPos(origin.getX(), origin.getY(), origin.getZ());
        this.baseDisplay.setBlockState(Blocks.NETHERITE_BLOCK.defaultBlockState());
        Quaternionf rot = new Quaternionf(defaultRotation);

        this.baseDisplay.setTransformation(new Transformation(
                new Vector3f(),
                rot,
                new Vector3f(),
                new Quaternionf()
        ));

        this.leftStrutDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        this.leftStrutDisplay.setPos(origin.getX(), origin.getY(), origin.getZ());
        this.leftStrutDisplay.setBlockState(Blocks.NETHERITE_BLOCK.defaultBlockState());
        this.leftStrutDisplay.setTransformation(new Transformation(
                new Vector3f(),
                rot,
                new Vector3f(),
                new Quaternionf()
        ));

        this.rightStrutDisplay = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        this.rightStrutDisplay.setPos(origin.getX(), origin.getY(), origin.getZ());
        this.rightStrutDisplay.setBlockState(Blocks.NETHERITE_BLOCK.defaultBlockState());
        this.rightStrutDisplay.setTransformation(new Transformation(
                new Vector3f(),
                new Quaternionf(rot).rotateY((float) Math.PI),
                new Vector3f(),
                new Quaternionf()
        ));

        spawnEntity(baseDisplay);
        spawnEntity(leftStrutDisplay);
        spawnEntity(rightStrutDisplay);

        updateScale(scale);
    }

    public void updateTarget(Location target) {
        super.updateTarget(target);

        Vector3f localDirection = VectorUtils.getDirectionVector(origin, target.toVector()).toVector3f();
        defaultRotation.conjugate(new Quaternionf()).transform(localDirection);
        float[] yawPitch = VectorUtils.getYawPitchFromDirection(Vector.fromJOML(localDirection));

        Quaternionf rotation = new Quaternionf(defaultRotation).rotateY(-yawPitch[0]);

        Transformation baseTransformation = Display.createTransformation(baseDisplay.getEntityData());
        baseDisplay.setTransformation(new Transformation(
                baseTransformation.getTranslation(),
                rotation,
                baseTransformation.getScale(),
                baseTransformation.getRightRotation()
        ));

        Transformation leftStrutTransformation = Display.createTransformation(leftStrutDisplay.getEntityData());
        leftStrutDisplay.setTransformation(new Transformation(
                leftStrutTransformation.getTranslation(),
                rotation,
                leftStrutTransformation.getScale(),
                leftStrutTransformation.getRightRotation()
        ));

        Transformation rightStrutTransformation = Display.createTransformation(rightStrutDisplay.getEntityData());
        rightStrutDisplay.setTransformation(new Transformation(
                rightStrutTransformation.getTranslation(),
                rotation.rotateY((float) Math.PI),
                rightStrutTransformation.getScale(),
                rightStrutTransformation.getRightRotation()
        ));

        updateTranslation();
    }

    @Override
    public void updateScale(float scale) {
        super.updateScale(scale);

        if (baseDisplay == null || leftStrutDisplay == null || rightStrutDisplay == null) {
            return;
        }

        Transformation baseTransformation = Display.createTransformation(baseDisplay.getEntityData());
        baseDisplay.setTransformation(new Transformation(
                baseTransformation.getTranslation(),
                baseTransformation.getLeftRotation(),
                new Vector3f(0.725f * scale, 0.1f * scale, 0.2f * scale),
                new Quaternionf()
        ));

        Transformation leftStrutTransformation = Display.createTransformation(leftStrutDisplay.getEntityData());
        leftStrutDisplay.setTransformation(new Transformation(
                leftStrutTransformation.getTranslation(),
                leftStrutTransformation.getLeftRotation(),
                new Vector3f(0.1f * scale, 0.45f * scale, 0.2f * scale),
                new Quaternionf()
        ));

        Transformation rightStrutTransformation = Display.createTransformation(rightStrutDisplay.getEntityData());
        rightStrutDisplay.setTransformation(new Transformation(
                rightStrutTransformation.getTranslation(),
                rightStrutTransformation.getLeftRotation(),
                new Vector3f(0.1f * scale, 0.45f * scale, 0.2f * scale),
                new Quaternionf()
        ));

        updateTranslation();
    }

    private void updateTranslation() {
        Transformation baseTransformation = Display.createTransformation(baseDisplay.getEntityData());
        Vector3fc baseScale = baseTransformation.getScale();
        Quaternionfc baseRotation = baseTransformation.getLeftRotation();
        baseDisplay.setTransformation(new Transformation(
                new Vector3f(baseScale.x() / -2, -0.45f * scale, baseScale.z() / -2).rotate(baseRotation),
                baseRotation,
                baseScale,
                new Quaternionf()
        ));

        Transformation leftStrutTransformation = Display.createTransformation(leftStrutDisplay.getEntityData());
        Vector3fc leftStrutScale = leftStrutTransformation.getScale();
        Quaternionfc leftStrutRotation = leftStrutTransformation.getLeftRotation();
        leftStrutDisplay.setTransformation(new Transformation(
                new Vector3f(0.2625f * scale, -0.35f * scale, leftStrutScale.z() / -2).rotate(leftStrutRotation),
                leftStrutRotation,
                leftStrutScale,
                new Quaternionf()
        ));

        Transformation rightStrutTransformation = Display.createTransformation(rightStrutDisplay.getEntityData());
        Vector3fc rightStrutScale = rightStrutTransformation.getScale();
        Quaternionfc rightStrutRotation = rightStrutTransformation.getLeftRotation();
        rightStrutDisplay.setTransformation(new Transformation(
                new Vector3f(0.2625f * scale, -0.35f * scale, rightStrutScale.z() / -2).rotate(rightStrutRotation),
                rightStrutRotation,
                rightStrutScale,
                new Quaternionf()
        ));

        updateEntity(baseDisplay);
        updateEntity(leftStrutDisplay);
        updateEntity(rightStrutDisplay);
    }
}
