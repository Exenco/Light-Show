package net.exenco.lightshow.show.stage.effects;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.ShowSettings;
import net.minecraft.world.item.EnumColor;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class BeaconBeam extends ShowEffect {

    private boolean enabled;
    private int red;
    private int green;
    private int blue;
    private final Vector[] blockLocations;
    private final PacketHandler packetHandler;
    private final Material disabledBlockMaterial;
    public BeaconBeam(JsonObject jsonObject, ShowSettings showSettings, PacketHandler packetHandler) {
        super(jsonObject);

        this.disabledBlockMaterial = showSettings.showEffects().beacon().disabledBlock();
        int count = jsonObject.has("ColourBlocks") ? jsonObject.get("ColourBlocks").getAsInt() : 7;

        this.blockLocations = new Vector[count];
        for(int i = 0; i < count; i++) {
            this.blockLocations[i] = location.clone().add(new Vector(0, i, 0));
        }
        this.packetHandler = packetHandler;
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
            packetHandler.sendBlockChange(location, Material.AIR);
        } else if(!enabled && this.enabled) {
            this.enabled = false;
            packetHandler.sendBlockChange(location, disabledBlockMaterial);
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
            updateColours();
    }

    private void updateColours() {
        int length = blockLocations.length;
        if(length >= 1) {
            HashMap<Vector, Material> materialList = new HashMap<>();

            EnumColor currentColour = getStartColour(red, green, blue);
            materialList.put(blockLocations[0], colourToMaterial(currentColour));

            if(length > 1) {
                for(int i = 1; i < length; i++) {
                    float[] colours = currentColour.getColor();
                    int r = (int) (colours[0] * 255);
                    int g = (int) (colours[1] * 255);
                    int b = (int) (colours[2] * 255);

                    currentColour = getClosestColourToFrom(red, green, blue, r, g, b);
                    materialList.put(blockLocations[i], colourToMaterial(currentColour));
                }
            }

            for(Map.Entry<Vector, Material> entry : materialList.entrySet()) {
                packetHandler.sendBlockChange(entry.getKey(), entry.getValue());
            }
        }
    }
    private EnumColor getStartColour(int red, int green, int blue) {
        Vector targetVector = new Vector(red, green, blue);

        double minDistance = new Vector(255, 255, 255).distance(targetVector);
        EnumColor bestEnumColor = null;
        for(EnumColor value : EnumColor.values()) {
            float[] colours = value.getColor();
            int r = (int) (colours[0] * 255);
            int g = (int) (colours[1] * 255);
            int b = (int) (colours[2] * 255);
            Vector vector = new Vector(r, g, b);
            double distance = vector.distance(targetVector);
            if(bestEnumColor == null || distance < minDistance) {
                minDistance = distance;
                bestEnumColor = value;
            }
        }
        return bestEnumColor;
    }

    private Material colourToMaterial(EnumColor enumColor) {
        if(enumColor == null)
            return Material.AIR;
        String name = enumColor.getName().toUpperCase() + "_STAINED_GLASS";
        return Material.valueOf(name);
    }

    private EnumColor getClosestColourToFrom(int red, int green, int blue, int fromRed, int fromGreen, int fromBlue) {
        Vector targetVector = new Vector(red, green, blue);

        double minDistance = new Vector(fromRed, fromGreen, fromBlue).distance(targetVector);
        EnumColor bestEnumColor = null;
        for(EnumColor value : EnumColor.values()) {
            float[] colours = value.getColor();
            int r = (int) (colours[0] * 255);
            int g = (int) (colours[1] * 255);
            int b = (int) (colours[2] * 255);

            Vector vector = new Vector((r + fromRed) / 2, (g + fromGreen) / 2, (b + fromBlue) / 2);
            double distance = vector.distance(targetVector);
            if(bestEnumColor == null || distance < minDistance) {
                minDistance = distance;
                bestEnumColor = value;
            }
        }
        return bestEnumColor;
    }
}
