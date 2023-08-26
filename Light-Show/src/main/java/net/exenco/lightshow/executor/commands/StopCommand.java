package net.exenco.lightshow.executor.commands;

import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StopCommand extends ShowCommand {

    private final StageManager stageManager;
    public StopCommand(ShowSettings showSettings, StageManager stageManager) {
        super(showSettings);
        this.stageManager = stageManager;
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getPermission() {
        return "lightshow.stop";
    }

    @Override
    public int getPosition() {
        return 1;
    }

    @Override
    public List<ShowCommand> getNext() {
        return null;
    }

    @Override
    public boolean execute(CommandSender commandSender, Command command, String label, String[] args) {
        ShowSettings.ArtNet stopMessages = showSettings.artNet();
        commandSender.sendMessage(stageManager.stop() ? stopMessages.stopping() : stopMessages.cannotStop());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return null;
    }
}
