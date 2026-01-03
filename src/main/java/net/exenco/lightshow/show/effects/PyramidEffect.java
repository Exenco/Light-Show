package net.exenco.lightshow.show.effects;

import com.mojang.math.Transformation;
import net.exenco.lightshow.util.VectorUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.function.Supplier;

public class PyramidEffect extends ShowEffect {

    private static final float LEFT_CHAR_OFFSET_X = -0.0625f;
    private static final float RIGHT_CHAR_OFFSET_X = 0.0375f;
    private static final float CHAR_OFFSET_Y = -0.025f;
    private static final float CHAR_SCALE_FACTOR = 5.0f;

    private static final float CHAR_START_WIDTH = 0.01475f;
    private static final float CHAR_TARGET_WIDTH = 1f;

    private static final float CHAR_WIDTH_GROWTH_FACTOR = 10.0f;

    private final Display.TextDisplay[] textDisplays = new Display.TextDisplay[8];

    private Location target;

    private float zoom = 1.0f;
    private Color colour = Color.fromRGB(255, 255, 255);
    private int brightness = 15;
    private byte opacity = 0b1000000;

    private float yOffset = 0;

    public PyramidEffect(World world, Vector origin, Vector rotation, Supplier<Collection<Player>> audienceSupplier) {
        super(world, origin, rotation, audienceSupplier);

        for (int i = 0; i < 8; i++) {
            Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
            textDisplay.setPos(origin.getX(), origin.getY(), origin.getZ());
            textDisplay.setBillboardConstraints(Display.BillboardConstraints.FIXED);
            textDisplay.setShadowStrength(0.0f);
            textDisplay.getEntityData().set(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, 0);
            textDisplay.setTextOpacity(opacity);
            textDisplay.setBrightnessOverride(new Brightness(brightness, brightness));
            textDisplay.setText(Component.literal(i % 2 == 0 ? "◥" : "◤").withColor(colour.asARGB()));

            textDisplays[i] = textDisplay;
            spawnEntity(textDisplay);
        }
    }

    public void updateTarget(Location target) {
        this.target = target;
        if (target == null || origin == null) {
            return;
        }

        double distance = origin.distance(target.toVector());

        float widthAtTarget = (float) (zoom * distance / CHAR_WIDTH_GROWTH_FACTOR * CHAR_TARGET_WIDTH);

        float scalarWidth = widthAtTarget * CHAR_SCALE_FACTOR;
        float scalarHeight = (float) distance * CHAR_SCALE_FACTOR;

        Vector3f direction = VectorUtils.getDirectionVector(origin, target.toVector()).toVector3f();
        defaultRotation.conjugate(new Quaternionf()).transform(direction);
        Quaternionf localRotation = VectorUtils.getRotationFromDirection(Vector.fromJOML(direction));
        Quaternionf baseRotation = new Quaternionf(defaultRotation).mul(localRotation);

        Vector3f scale = new Vector3f(scalarWidth, scalarHeight, 0);

        for (int i = 0; i < 4; i++) {
            Quaternionf rotation = new Quaternionf(baseRotation);
            rotation.rotateY((float) Math.toRadians(i * 90));
            rotation.rotateX((float) Math.toRadians(2.6 * zoom));

            // Translation for facing target
            Vector3f translationLeft = new Vector3f(LEFT_CHAR_OFFSET_X * scalarWidth, CHAR_OFFSET_Y * scalarHeight, 0);
            Vector3f translationRight = new Vector3f(RIGHT_CHAR_OFFSET_X * scalarWidth, CHAR_OFFSET_Y * scalarHeight, 0);

            // Translation for offsetting origin based on rotation
            float originWidthOffset = (float) ((CHAR_START_WIDTH * CHAR_SCALE_FACTOR * zoom * distance / CHAR_WIDTH_GROWTH_FACTOR) / 2);
            translationLeft.add(new Vector3f(0, 0, originWidthOffset));
            translationRight.add(new Vector3f(0, 0, originWidthOffset));

            // Translation for yOffset
            translationLeft.add(new Vector3f(0, yOffset, 0));
            translationRight.add(new Vector3f(0, yOffset, 0));

            // rotate translation
            translationRight.rotate(rotation);
            translationLeft.rotate(rotation);

            Transformation transformationLeft = new Transformation(translationLeft, rotation, scale, new Quaternionf());
            Transformation transformationRight = new Transformation(translationRight, rotation, scale, new Quaternionf());

            if (textDisplays[i * 2] == null || !textDisplays[i * 2].isAlive() || textDisplays[i * 2 + 1] == null || !textDisplays[i * 2 + 1].isAlive()) {
                return;
            }

            Display.TextDisplay leftChar = textDisplays[i * 2];
            leftChar.setTransformation(transformationLeft);
            updateEntity(leftChar);

            Display.TextDisplay rightChar = textDisplays[i * 2 + 1];
            rightChar.setTransformation(transformationRight);
            updateEntity(leftChar);
        }
    }

    public void updateZoom(float zoom) {
        this.zoom = zoom;
        updateTarget(target);
    }

    public void updateBrightness(int brightness) {
        this.brightness = brightness;
        for (Display.TextDisplay textDisplay : textDisplays) {
            textDisplay.setBrightnessOverride(new Brightness(brightness, brightness));
            updateEntity(textDisplay);
        }
    }

    public void updateColor(Color color) {
        this.colour = color;
        for (Display.TextDisplay textDisplay : textDisplays) {
            textDisplay.setText(textDisplay.getText().copy().withColor(colour.asARGB()));
            updateEntity(textDisplay);
        }
    }

    public void updateOpacity(byte opacity) {
        this.opacity = opacity;
        for (Display.TextDisplay textDisplay : textDisplays) {
            textDisplay.setTextOpacity(opacity);
            updateEntity(textDisplay);
        }
    }

    public void updateYOffset(float yOffset) {
        this.yOffset = yOffset;
        updateTarget(target);
    }
}
