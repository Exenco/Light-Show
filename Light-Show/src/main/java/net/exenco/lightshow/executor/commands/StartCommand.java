package net.exenco.lightshow.executor.commands;

import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StartCommand extends ShowCommand {

    private final ArrayList<ShowCommand> nextList;
    private final StageManager stageManager;
    public StartCommand(ShowSettings showSettings, StageManager stageManager) {
        super(showSettings);
        this.stageManager = stageManager;
        this.nextList = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getPermission() {
        return "lightshow.start";
    }

    @Override
    public int getPosition() {
        return 1;
    }

    @Override
    public List<ShowCommand> getNext() {
        return nextList;
    }

    @Override
    public boolean execute(CommandSender commandSender, Command command, String label, String[] args) {
        ShowSettings.ArtNet startMessages = showSettings.artNet();
        if (stageManager.start()) {
            commandSender.sendMessage(startMessages.starting());
        } else {
            commandSender.sendMessage(startMessages.cannotStart());
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return null;
    }
}
