package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ConfigHandler;
import net.exenco.lightshow.util.PacketHandler;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.*;

public class BeaconFixture extends ShowFixture {

    private final PacketHandler packetHandler;
    private boolean enabled;
    private int red;
    private int green;
    private int blue;
    private final Vector[] blockLocations;
    private final BlockData disabledBlockData;
    private final static BlockData airData = Material.AIR.createBlockData();
    public BeaconFixture(JsonObject jsonObject, StageManager stageManager) {
        super(jsonObject, stageManager);
        this.packetHandler = stageManager.getPacketHandler();

        Material disabledBlockMaterial = jsonObject.has("OffBlock") ? ConfigHandler.getMaterialFromName(jsonObject.get("OffBlock").getAsString()) : Material.BLACK_WOOL;
        this.disabledBlockData = disabledBlockMaterial.createBlockData();
        int count = jsonObject.has("ColourBlocks") ? jsonObject.get("ColourBlocks").getAsInt() : 7;

        this.blockLocations = new Vector[count];
        for(int i = 0; i < count; i++) {
            this.blockLocations[i] = location.clone().add(new Vector(0, i, 0));
        }
    }

    @Override
    public int getDmxSize() {
        return 4;
    }

    @Override
    public void applyState(int[] data) {
        boolean enabled = data[0] > 0;
        int red = data[1];
        int green = data[2];
        int blue = data[3];

        boolean updateColours = false;

        if(enabled && !this.enabled) {
            this.enabled = true;
            updateColours = true;
            packetHandler.sendBlockChange(location, airData);
        } else if(!enabled && this.enabled) {
            this.enabled = false;
            packetHandler.sendBlockChange(location, disabledBlockData);
        }

        if(!this.enabled)
            return;

        if(red != this.red || green != this.green || blue != this.blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            updateColours = true;
        }

        if(updateColours)
            updateGlassList();
    }

    private void updateGlassList() {
        int length = blockLocations.length;
        if(length >= 1) {
            HashMap<Vector, BlockData> dataList = new HashMap<>();

            EnumColour currentColour = getClosestColour(red, green, blue, -1, -1, -1);
            dataList.put(blockLocations[0], currentColour.getBlockData());

            if(length > 1) {
                for(int i = 1; i < length; i++) {
                    currentColour = getClosestColour(red, green, blue, currentColour.getRed(), currentColour.getGreen(), currentColour.getBlue());
                    dataList.put(blockLocations[i], currentColour.getBlockData());
                }
            }
            for(Map.Entry<Vector, BlockData> entry : dataList.entrySet()) {
                packetHandler.sendBlockChange(entry.getKey(), entry.getValue());
            }
        }
    }

    private EnumColour getClosestColour(int red, int green, int blue, int fromRed, int fromGreen, int fromBlue) {
        Vector targetVector = new Vector(red, green, blue);

        double minDistance = (fromRed == -1 ? new Vector(255, 255, 255) : new Vector(fromRed, fromGreen, fromBlue)).distance(targetVector);
        EnumColour bestEnumColor = null;
        for(EnumColour value : EnumColour.values()) {
            int r = value.getRed();
            int g = value.getGreen();
            int b = value.getBlue();

            Vector vector = fromRed == -1 ? new Vector(r, g, b) : new Vector((r + fromRed) / 2, (g + fromGreen) / 2, (b + fromBlue) / 2);
            double distance = vector.distance(targetVector);
            if(bestEnumColor == null || distance < minDistance) {
                minDistance = distance;
                bestEnumColor = value;
            }
        }
        return bestEnumColor;
    }

    private enum EnumColour {
        WHITE(249, 255, 254, Material.WHITE_STAINED_GLASS.createBlockData()),
        ORANGE(249, 128, 29, Material.ORANGE_STAINED_GLASS.createBlockData()),
        MAGENTA(199, 78, 189, Material.MAGENTA_STAINED_GLASS.createBlockData()),
        LIGHT_BLUE(58, 179, 218, Material.LIGHT_BLUE_STAINED_GLASS.createBlockData()),
        YELLOW(254, 216, 61, Material.YELLOW_STAINED_GLASS.createBlockData()),
        LIME(128, 199, 31, Material.LIME_STAINED_GLASS.createBlockData()),
        PINK(243, 139, 170, Material.PINK_STAINED_GLASS.createBlockData()),
        GRAY(71, 79, 82, Material.GRAY_STAINED_GLASS.createBlockData()),
        LIGHT_GRAY(157, 157, 151, Material.LIGHT_GRAY_STAINED_GLASS.createBlockData()),
        CYAN(22, 156, 156, Material.CYAN_STAINED_GLASS.createBlockData()),
        PURPLE(137, 50, 184, Material.PURPLE_STAINED_GLASS.createBlockData()),
        BLUE(60, 68, 170, Material.BLUE_STAINED_GLASS.createBlockData()),
        BROWN(131, 84, 50, Material.BROWN_STAINED_GLASS.createBlockData()),
        GREEN(94, 124, 22, Material.GREEN_STAINED_GLASS.createBlockData()),
        RED(176, 46, 38, Material.RED_STAINED_GLASS.createBlockData()),
        BLACK(29, 29, 33, Material.BLACK_STAINED_GLASS.createBlockData());

        private final int red;
        private final int green;
        private final int blue;
        private final BlockData blockData;
        EnumColour(int red, int green, int blue, BlockData blockData) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.blockData = blockData;
        }
        public int getRed() {
            return red;
        }
        public int getGreen() {
            return green;
        }
        public int getBlue() {
            return blue;
        }
        public BlockData getBlockData() {
            return blockData;
        }
    }
}