package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ConfigHandler;
import net.exenco.lightshow.util.PacketHandler;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Lightable;

public class BlockUpdaterFixture extends ShowFixture {
    private final boolean lit;
    private final BlockData enabledState;
    private final BlockData disabledState;

    private final PacketHandler packetHandler;
    public BlockUpdaterFixture(JsonObject jsonObject, StageManager stageManager) {
        super(jsonObject, stageManager);
        this.packetHandler = stageManager.getPacketHandler();

        this.lit = !jsonObject.has("Lit") || jsonObject.get("Lit").getAsBoolean();
        Material enabledMaterial = jsonObject.has("EnabledState") ? ConfigHandler.getMaterialFromName(jsonObject.get("EnabledState").getAsString()) : Material.REDSTONE_LAMP;
        this.enabledState = enabledMaterial.createBlockData();
        Material disabledMaterial = jsonObject.has("DisabledState") ? ConfigHandler.getMaterialFromName(jsonObject.get("DisabledState").getAsString()) : Material.REDSTONE_LAMP;
        this.disabledState = disabledMaterial.createBlockData();
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
            updateBlockData = enabledState;
            if(lit && updateBlockData instanceof Lightable lightable)
                lightable.setLit(true);
        } else {
            updateBlockData = disabledState;
            if(lit && updateBlockData instanceof Lightable lightable)
                lightable.setLit(false);
        }
        if(updateBlockData instanceof Levelled levelled)
            levelled.setLevel(data[0] / 16);
        packetHandler.sendBlockChange(location, updateBlockData);
    }
}
