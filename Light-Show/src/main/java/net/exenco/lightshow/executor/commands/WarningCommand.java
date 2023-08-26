package net.exenco.lightshow.executor.commands;

import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class WarningCommand extends ShowCommand {

    public WarningCommand(ShowSettings showSettings) {
        super(showSettings);
    }

    @Override
    public String getName() {
        return "warning";
    }

    @Override
    public String getPermission() {
        return "lightshow.warning";
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
        String termsOfService = showSettings.stage().termsOfService();
        if(termsOfService != null && !termsOfService.equals(""))
            commandSender.sendMessage(termsOfService);
        return true;
    }
}
