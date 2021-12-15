package net.exenco.lightshow.show.stage.effects;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.registries.FireworkRegistry;
import net.exenco.lightshow.util.PacketHandler;
import net.minecraft.world.entity.projectile.EntityFireworks;
import net.minecraft.world.item.ItemStack;

public class FireworkLauncher extends ShowEffect {

    private boolean fire = true;

    private final PacketHandler packetHandler;
    private final FireworkRegistry fireworkRegistry;
    public FireworkLauncher(JsonObject jsonObject, PacketHandler packetHandler, FireworkRegistry fireworkRegistry) {
        super(jsonObject);
        this.packetHandler = packetHandler;
        this.fireworkRegistry = fireworkRegistry;

        this.tickSize = 200;
    }

    @Override
    public int getDmxSize() {
        return 2;
    }

    @Override
    public void applyState(int[] data) {
        boolean spawn = data[0] > 0;

        if(!spawn) {
            fire = true;
            return;
        }

        if(data[0] == 255 && isTick())
            fire = true;

        if(!fire)
            return;

        fire = false;
        int id = data[1];

        ItemStack itemStack = fireworkRegistry.getFireworkById(id);
        EntityFireworks entityFireworks = new EntityFireworks(packetHandler.getWorld(), location.getX(), location.getY(), location.getZ(), itemStack);
        this.packetHandler.spawnFirework(entityFireworks);
    }
}
