package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.ShowSettings;
import net.exenco.lightshow.util.VectorUtils;
import net.exenco.lightshow.util.api.GuardianBeamApi;
import net.minecraft.core.Vector3f;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.*;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;

public class MovingHeadFixture extends ShowFixture {

    private int state;
    private float yaw;
    private float pitch;

    private EntityArmorStand headArmorStand = null;
    private EntityArmorStand lightArmorStand = null;
    private final PacketHandler packetHandler;
    private final double maxDistance;

    private final GuardianBeamApi guardianBeamApi;
    private final String offTexture;
    private final String lowTexture;
    private final String mediumTexture;
    private final String highTexture;
    public MovingHeadFixture(JsonObject jsonObject, StageManager stageManager) {
        super(jsonObject, stageManager);
        this.packetHandler = stageManager.getPacketHandler();
        ShowSettings showSettings = stageManager.getShowSettings();
        this.offTexture = showSettings.showEffects().movingLight().offTexture();
        this.lowTexture = showSettings.showEffects().movingLight().lowTexture();
        this.mediumTexture = showSettings.showEffects().movingLight().mediumTexture();
        this.highTexture = showSettings.showEffects().movingLight().highTexture();

        this.guardianBeamApi = new GuardianBeamApi(location.clone().subtract(new Vector(0, 0.5, 0)), packetHandler);

        this.maxDistance = jsonObject.has("MaxDistance") ? jsonObject.get("MaxDistance").getAsDouble() : 100;

        spawnHeadArmorStand(offTexture);
        spawnLightArmorStand();
        this.state = 0;
    }

    private ItemStack getSpotlightHead(String headTexture) {
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), null);
        gameProfile.getProperties().put("textures", new Property("textures", headTexture));
        try {
            Field field = Objects.requireNonNull(skullMeta).getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(skullMeta, gameProfile);
        } catch(NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        itemStack.setItemMeta(skullMeta);
        return itemStack;
    }

    private void lookAt(float yaw, float pitch) {
        Vector3f vector = new Vector3f(pitch, yaw, 0);
        this.headArmorStand.a(vector);
        packetHandler.updateEntity(this.headArmorStand);

        this.lightArmorStand.a(vector);
        packetHandler.updateEntity(this.lightArmorStand);
    }

    private void spawnHeadArmorStand(String headTexture) {
        Vector spotlightLocation = location.clone().subtract(new Vector(0, 1.5, 0));
        double x = spotlightLocation.getX();
        double y = spotlightLocation.getY();
        double z = spotlightLocation.getZ();
        this.headArmorStand = new EntityArmorStand(packetHandler.getWorld(), x, y, z);
        this.headArmorStand.p(0.0F);
        this.headArmorStand.o(0.0F);
        this.headArmorStand.e(true);
        this.headArmorStand.j(true);
        this.headArmorStand.a(EnumItemSlot.f, CraftItemStack.asNMSCopy(getSpotlightHead(headTexture)));

        this.packetHandler.spawnEntity(this.headArmorStand);
    }

    private void updateHeadArmorStand(String headTexture) {
        this.headArmorStand.a(EnumItemSlot.f, CraftItemStack.asNMSCopy(getSpotlightHead(headTexture)));
        this.packetHandler.updateEntityEquipment(this.headArmorStand);
    }

    private void spawnLightArmorStand() {
        Vector lightLocation = location.clone().subtract(new Vector(0, 0.775, 0));
        double x = lightLocation.getX();
        double y = lightLocation.getY();
        double z = lightLocation.getZ();
        this.lightArmorStand = new EntityArmorStand(packetHandler.getWorld(), x, y, z);
        this.lightArmorStand.p(0.0F);
        this.lightArmorStand.o(0.0F);
        this.lightArmorStand.e(true);
        this.lightArmorStand.a(true);
        this.lightArmorStand.j(true);
        this.lightArmorStand.a(EnumItemSlot.f, CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)));
        this.lightArmorStand.a(new Vector3f(yaw, pitch, 0));

        this.packetHandler.spawnEntity(this.lightArmorStand);
    }

    private void updateLightArmorStand(Material material) {
        this.lightArmorStand.a(EnumItemSlot.f, CraftItemStack.asNMSCopy(new ItemStack(material)));
        this.packetHandler.updateEntityEquipment(this.lightArmorStand);
    }

    @Override
    public int getDmxSize() {
        return 7;
    }

    @Override
    public void applyState(int[] data) {
        int dim = asRoundedPercentage(data[0]);
        float pan = 360 * -((float) (data[1]<<8 | data[2]) / 65535);
        float tilt = 360 * -((float) (data[3]<<8 | data[4]) / 65535);
        double distance = valueOfMax(this.maxDistance, data[5]);
        boolean colourChange = data[6] > 0;

        if(dim > 66) {
            if(state != 3) {
                state = 3;
                updateHeadArmorStand(highTexture);
                updateLightArmorStand(Material.TORCH);
            }
        } else if(dim > 33) {
            if(state != 2) {
                state = 2;
                updateHeadArmorStand(mediumTexture);
                updateLightArmorStand(Material.SOUL_TORCH);
            }
        } else if(dim > 0) {
            if(state != 1) {
                state = 1;
                updateHeadArmorStand(lowTexture);
                updateLightArmorStand(Material.REDSTONE_TORCH);
            }
        } else {
            if(state != 0) {
                state = 0;
                updateHeadArmorStand(offTexture);
                updateLightArmorStand(Material.AIR);
            }
        }

        if(this.yaw != pan || this.pitch != tilt) {
            this.yaw = pan;
            this.pitch = tilt;
            lookAt(pan, tilt);
        }

        boolean enableBeam = dim > 0 && distance > 0;
        if(enableBeam && !guardianBeamApi.isSpawned()) {
            guardianBeamApi.spawn();
        } else if(!enableBeam && guardianBeamApi.isSpawned()) {
            guardianBeamApi.destroy();
        }

        if(enableBeam && guardianBeamApi.isSpawned()) {
            guardianBeamApi.setDestination(getDestination(yaw, pitch, distance));
            if(colourChange && isTick())
                guardianBeamApi.callColorChange();
        }
    }

    private Vector getDestination(float yaw, float pitch, double distance) {
        Vector vector = VectorUtils.getDirectionVector(yaw - 90, pitch + 90);
        Vector start = this.location.clone();

        if(distance == 0)
            return start;

        return rayTrace(start, start.clone().add(vector.multiply(distance)));
    }

    private Vector rayTrace(Vector location, Vector destination) {
        Vector end = destination.clone();
        double maxDistance = location.distance(end);
        Vector start = location.clone();
        Vector direction = end.clone().subtract(start).normalize();
        start.add(direction);
        CraftWorld world = packetHandler.getWorld().getWorld();
        Location startLocation = start.toLocation(world);
        RayTraceResult rayTraceResult = world.rayTraceBlocks(startLocation, direction, maxDistance, FluidCollisionMode.NEVER, true);
        if(rayTraceResult != null)
            return rayTraceResult.getHitPosition();
        return end;
    }
}
