package net.exenco.lightshow.executor.commands;

import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

/**
 * Extend this class if you want to build a tree-wise command.
 */
public abstract class ShowCommand {

    protected ShowSettings showSettings;
    public ShowCommand(ShowSettings showSettings) {
        this.showSettings = showSettings;
    }

    /**
     * Getter for name of command.
     * @return the name.
     */
    public abstract String getName();

    /**
     * Getter for permission of command.
     * @return the permission.
     */
    public abstract String getPermission();

    /**
     * Getter for position of command.
     * @return the position.
     */
    public abstract int getPosition();

    /**
     * Method for representation of self in tab-completion.
     * @return list of representations.
     */
    public List<String> getCompletions() {
        List<String> nextList = new ArrayList<>();
        nextList.add(getName());
        return nextList;
    }

    /**
     * Method for all following arguments.
     * @return list of following arguments.
     */
    public abstract List<ShowCommand> getNext();

    /**
     * Default algorithm to work executor.
     * @param commandSender pass through of {@link CommandSender}
     * @param command pass through of {@link Command}
     * @param label pass through of {@link String}
     * @param args pass through of {@link String[]}
     * @return whether the executor was successful finding the command.
     */
    public boolean execute(CommandSender commandSender, Command command, String label, String[] args) {
        if(args.length > getPosition()) {
            for(ShowCommand showCommand : getNext()) {
                String name = showCommand.getName();
                if(name.equalsIgnoreCase(args[getPosition()]) || (name.startsWith("%") && name.endsWith("%"))) {
                    if(commandSender.hasPermission(showCommand.getPermission())) {
                        return showCommand.execute(commandSender, command, label, args);
                    } else {
                        commandSender.sendMessage(this.showSettings.commands().noPermission());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Default algorithm to determine next arguments.
     * @param commandSender pass through of {@link CommandSender}
     * @param command pass through of {@link Command}
     * @param label pass through of {@link String}
     * @param args pass through of {@link String[]}
     * @return a list containing arguments to be displayed.
     */
    public List<String> tabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        int position = getPosition();
        if (args.length > position + 1) {
            String next = args[position - 1];
            for (ShowCommand showCommand : getNext()) {
                if (!commandSender.hasPermission(showCommand.getPermission())) {
                    continue;
                }
                String name = showCommand.getName();
                if (name.equalsIgnoreCase(next) || (name.startsWith("%") && name.endsWith("%"))) {
                    return showCommand.tabComplete(commandSender, command, label, args);
                }
            }
        } else if (args.length == position + 1) {
            ArrayList<String> completionList = new ArrayList<>();
            String arg = args[position].toLowerCase();
            if (getNext() != null) {
                for (ShowCommand showCommand : getNext()) {
                    if (!commandSender.hasPermission(showCommand.getPermission())) {
                        continue;
                    }
                    for (String s : showCommand.getCompletions())
                        if (s.toLowerCase().startsWith(arg))
                            completionList.add(s);
                }
            }
            return completionList;
        }
        return null;
    }
}
