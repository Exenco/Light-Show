package net.exenco.lightshow.executor.commands;

import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReloadCommand extends ShowCommand {

    private final LightShow lightShow;
    public ReloadCommand(LightShow lightShow, ShowSettings showSettings) {
        super(showSettings);
        this.lightShow = lightShow;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getPermission() {
        return "lightshow.reload";
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
        lightShow.reload();
        commandSender.sendMessage(this.showSettings.commands().reload());
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        return null;
    }
}
