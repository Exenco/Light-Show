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

public class RectangleEffect extends ShowEffect {

    private static final float CHAR_OFFSET_X = -0.0125F;

    private static final float SCALE_FACTOR_X = 8.0f;
    private static final float SCALE_FACTOR_Y = 4.0f;

    private final Display.TextDisplay[] textDisplays = new Display.TextDisplay[4];

    private Location target;

    private int brightness = 15;
    private Color colour = Color.fromARGB(0, 255, 255, 255);
    private float scale = 1.0f;

    private float yOffset = 0.0f;

    public RectangleEffect(World world, Vector origin, Vector rotation, Supplier<Collection<Player>> audienceSupplier) {
        super(world, origin, rotation, audienceSupplier);

        for (int i = 0; i < 4; i++) {
            Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
            textDisplay.setPos(origin.getX(), origin.getY(), origin.getZ());
            textDisplay.setBillboardConstraints(Display.BillboardConstraints.FIXED);
            textDisplay.setShadowStrength(0.0f);
            textDisplay.getEntityData().set(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, colour.asARGB());
            textDisplay.setTextOpacity((byte) 0);
            textDisplay.setBrightnessOverride(new Brightness(brightness, brightness));
            textDisplay.setText(Component.literal(" "));

            textDisplays[i] = textDisplay;
            spawnEntity(textDisplay);
        }
    }

    public void updateTarget(Location target) {
        if (target == null || origin == null || colour.getAlpha() == 0) {
            return;
        }
        double distance = origin.distance(target.toVector());
        if (distance < 0.01) {
            return;
        }
        this.target = target;

        float scalarWidth = this.scale * SCALE_FACTOR_X;
        float scalarHeight = SCALE_FACTOR_Y * ((float) distance - yOffset);

        Vector3f direction = VectorUtils.getDirectionVector(origin, target.toVector()).toVector3f();
        defaultRotation.conjugate(new Quaternionf()).transform(direction);
        Quaternionf localRotation = VectorUtils.getRotationFromDirection(Vector.fromJOML(direction));
        Quaternionf baseRotation = new Quaternionf(defaultRotation).mul(localRotation);

        Vector3f scale = new Vector3f(scalarWidth, scalarHeight, 0);

        for (int i = 0; i < 4; i++) {
            Quaternionf rotation = new Quaternionf(baseRotation);
            rotation.rotateY(i * ((float) Math.PI / 2.0f));

            Vector3f translation = new Vector3f(CHAR_OFFSET_X * scalarWidth, 0, 0);
            translation.add(new Vector3f(0, 0, this.scale / 2));
            translation.add(new Vector3f(0, yOffset, 0));
            translation.rotate(rotation);

            if (textDisplays[i] == null || !textDisplays[i].isAlive()) {
                return;
            }

            Display.TextDisplay textDisplay = textDisplays[i];
            textDisplay.setTransformation(new Transformation(
                    translation,
                    rotation,
                    scale,
                    new Quaternionf()
            ));
            updateEntity(textDisplay);
        }
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
            textDisplay.getEntityData().set(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, colour.asARGB());
            updateEntity(textDisplay);
        }
    }

    public void updateYOffset(float yOffset) {
        this.yOffset = yOffset;

        updateTarget(this.target);
    }

    public void updateScale(float scale) {
        this.scale = scale;

        updateTarget(this.target);
    }
}
