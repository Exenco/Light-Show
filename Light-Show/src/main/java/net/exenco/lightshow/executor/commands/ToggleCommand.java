package net.exenco.lightshow.executor.commands;

import net.exenco.lightshow.util.ProximitySensor;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ToggleCommand extends ShowCommand {

    private final ProximitySensor proximitySensor;
    public ToggleCommand(ShowSettings showSettings, ProximitySensor proximitySensor) {
        super(showSettings);
        this.proximitySensor = proximitySensor;
    }

    @Override
    public String getName() {
        return "toggle";
    }

    @Override
    public String getPermission() {
        return "lightshow.toggle";
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
        if(commandSender instanceof Player player) {
            if(proximitySensor.containsTogglePlayer(player)) {
                proximitySensor.removeTogglePlayer(player);
                player.sendMessage(showSettings.commands().toggleOn());
            } else {
                proximitySensor.addTogglePlayer(player);
                player.sendMessage(showSettings.commands().toggleOff());
            }
        } else {
            commandSender.sendMessage(showSettings.commands().notAllowed());
        }
        return true;
    }
}
