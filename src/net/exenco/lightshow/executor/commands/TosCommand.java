package net.exenco.lightshow.executor.commands;

import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class TosCommand extends ShowCommand {

    public TosCommand(ShowSettings showSettings) {
        super(showSettings);
    }

    @Override
    public String getName() {
        return "tos";
    }

    @Override
    public String getPermission() {
        return "lightshow.tos";
    }

    @Override
    public int getPosition() {
        return 1;
    }

    @Override
    public ArrayList<ShowCommand> getNext() {
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
