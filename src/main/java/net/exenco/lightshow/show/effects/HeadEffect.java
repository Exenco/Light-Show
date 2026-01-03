package net.exenco.lightshow.show.effects;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.math.Transformation;
import net.exenco.lightshow.util.VectorUtils;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collection;
import java.util.UUID;
import java.util.function.Supplier;

public class HeadEffect extends ShowEffect {

    private static final float OFFSET_Y = 0.25f;

    private static final ItemStack HEAD_ITEM;

    static {
        HEAD_ITEM = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) HEAD_ITEM.getItemMeta();
        String texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmJiMWEyNDdmZmJmMTdhOWFjODQzNzFmNDI4MWZlYjA1YmZmYmI1YTczYTI5MGNhNjA5YjM0MzhiOGNiZGIzIn19fQ==";
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
        profile.setProperty(new ProfileProperty("textures", texture));
        meta.setPlayerProfile(profile);
        HEAD_ITEM.setItemMeta(meta);
    }

    protected final Display.ItemDisplay itemDisplay;
    protected float scale = 1.0f;

    public HeadEffect(World world, Vector origin, Vector rotation, Supplier<Collection<Player>> audienceSupplier) {
        super(world, origin, rotation, audienceSupplier);

        this.itemDisplay = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
        this.itemDisplay.setPos(origin.getX(), origin.getY(), origin.getZ());
        this.itemDisplay.setItemStack(net.minecraft.world.item.ItemStack.fromBukkitCopy(HEAD_ITEM));

        Quaternionf rot = new Quaternionf(defaultRotation);

        this.itemDisplay.setTransformation(new Transformation(
                getTranslation(rot),
                rot,
                new Vector3f(scale, scale, scale),
                new Quaternionf()
        ));

        spawnEntity(itemDisplay);
    }

    public void updateTarget(Location target) {
        Quaternionf rotation = new Quaternionf(defaultRotation);

        Vector3f localDirection = VectorUtils.getDirectionVector(origin, target.toVector()).toVector3f();
        defaultRotation.conjugate(new Quaternionf()).transform(localDirection);
        Quaternionf localRotation = VectorUtils.getRotationFromDirection(Vector.fromJOML(localDirection));
        rotation.mul(localRotation);

        Transformation transformation = Display.createTransformation(itemDisplay.getEntityData());

        itemDisplay.setTransformation(new Transformation(
                getTranslation(rotation),
                rotation,
                transformation.getScale(),
                new Quaternionf()
        ));

        updateEntity(itemDisplay);
    }

    public void updateScale(float scale) {
        this.scale = scale;

        Transformation transformation = Display.createTransformation(itemDisplay.getEntityData());
        itemDisplay.setTransformation(new Transformation(
                getTranslation(transformation.getLeftRotation()),
                transformation.getLeftRotation(),
                new Vector3f(scale, scale, scale),
                new Quaternionf()
        ));
        updateEntity(itemDisplay);
    }

    private Vector3f getTranslation(Quaternionfc rotation) {
        Vector3f translation = new Vector3f(0, scale * OFFSET_Y, 0);
        translation.rotate(rotation);
        return translation;
    }
}
