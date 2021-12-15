package net.exenco.lightshow.show.stage.effects;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.PacketHandler;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;

public class BlockUpdater extends ShowEffect {
    private final boolean lit;
    private final Material enabledState;
    private final Material disabledState;

    private final PacketHandler packetHandler;
    public BlockUpdater(JsonObject jsonObject, PacketHandler packetHandler) {
        super(jsonObject);
        this.packetHandler = packetHandler;

        this.lit = !jsonObject.has("Lit") || jsonObject.get("Lit").getAsBoolean();
        this.enabledState = jsonObject.has("EnabledState") ? Material.valueOf(jsonObject.get("EnabledState").getAsString()) : Material.REDSTONE_LAMP;
        this.disabledState = jsonObject.has("DisabledState") ? Material.valueOf(jsonObject.get("DisabledState").getAsString()) : Material.REDSTONE_LAMP;
    }

    @Override
    public int getDmxSize() {
        return 1;
    }

    private boolean lastState;
    @Override
    public void applyState(int[] data) {
        boolean enabled = data[0] > 0;

        if(lastState == enabled)
            return;

        lastState = enabled;
        BlockData updateBlockData;

        if(enabled) {
            updateBlockData = enabledState.createBlockData();
            if(lit && updateBlockData instanceof Lightable lightable)
                lightable.setLit(true);
        } else {
            updateBlockData = disabledState.createBlockData();
            if(lit && updateBlockData instanceof Lightable lightable)
                lightable.setLit(false);
        }
        packetHandler.sendBlockChange(location, updateBlockData);
    }
}
