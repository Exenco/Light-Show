package net.exenco.lightshow.show.effects;

import com.mojang.math.Transformation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Color;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.function.Supplier;

public class SurfaceEffect extends ShowEffect {

    private static final float CHAR_OFFSET_X = -0.0125f;
    private static final float CHAR_OFFSET_Y = -0.5f;

    private static final float SCALE_FACTOR_X = 8.0f;
    private static final float SCALE_FACTOR_Y = 4.0f;

    protected int brightness = 15;
    protected Color colour = Color.fromARGB(0, 255, 255, 255);
    protected float scaleWidth = 1.0f;
    protected float scaleHeight = 1.0f;

    protected final Display.TextDisplay textDisplay;

    public SurfaceEffect(World world, Vector origin, Vector rotation, Supplier<Collection<Player>> audienceSupplier, float scaleWidth, float scaleHeight) {
        super(world, origin, rotation, audienceSupplier);
        this.scaleWidth = scaleWidth;
        this.scaleHeight = scaleHeight;

        this.textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
        textDisplay.setPos(origin.getX(), origin.getY(), origin.getZ());
        textDisplay.setBillboardConstraints(Display.BillboardConstraints.FIXED);
        textDisplay.setShadowStrength(0.0f);
        textDisplay.getEntityData().set(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, colour.asARGB());
        textDisplay.setTextOpacity((byte) 0);
        textDisplay.setBrightnessOverride(new Brightness(brightness, brightness));
        textDisplay.setText(Component.literal(" "));

        float scalarWidth = scaleWidth * SCALE_FACTOR_X;
        float scalarHeight = scaleHeight * SCALE_FACTOR_Y;

        Quaternionf rot = new Quaternionf(defaultRotation);

        Vector3f translation = new Vector3f(CHAR_OFFSET_X * scalarWidth, CHAR_OFFSET_Y * scaleHeight, 0);
        translation.rotate(rot);
        textDisplay.setTransformation(new Transformation(
                translation,
                rot,
                new Vector3f(scalarWidth, scalarHeight, 0),
                new Quaternionf()
        ));

        spawnEntity(textDisplay);
    }

    public void updateColour(Color colour) {
        if (colour == null) {
            return;
        }
        this.colour = colour;
        textDisplay.getEntityData().set(Display.TextDisplay.DATA_BACKGROUND_COLOR_ID, colour.asARGB());
    }

    @Override
    public void spawnEntity(Entity entity) {
        super.spawnEntity(entity);
    }
}
