package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.stage.StageManager;
import org.bukkit.Bukkit;

public class CommandFixture extends ShowFixture {

    private final String command;
    private boolean fire = true;

    public CommandFixture(JsonObject configJson, StageManager stageManager) {
        super(configJson, stageManager);
        this.command = configJson.has("Command") ? configJson.get("Command").getAsString() : "say test";
    }

    @Override
    public int getDmxSize() {
        return 1;
    }

    @Override
    public void applyState(int[] data) {
        boolean execute = data[0] > 0;
        if (!execute) {
            fire = true;
            return;
        }

        if (fire) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            fire = false;
        }
    }
}
