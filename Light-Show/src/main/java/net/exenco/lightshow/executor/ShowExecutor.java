package net.exenco.lightshow.executor;

import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.executor.commands.*;
import net.exenco.lightshow.show.song.ShowSong;
import net.exenco.lightshow.show.song.SongManager;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ProximitySensor;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ShowExecutor implements CommandExecutor, TabCompleter {

    private final ArrayList<ShowCommand> commandList = new ArrayList<>();

    private final ShowSettings showSettings;
    private final SongManager songManager;

    public ShowExecutor(LightShow lightShow, ShowSettings showSettings, StageManager stageManager, SongManager songManager, ProximitySensor proximitySensor) {
        this.showSettings = showSettings;
        this.songManager = songManager;

        commandList.add(new CheckCommand(lightShow, showSettings, stageManager));
        commandList.add(new ReloadCommand(lightShow, showSettings));
        commandList.add(new StartCommand(showSettings, stageManager));
        commandList.add(new StopCommand(showSettings, stageManager));
        commandList.add(new WarningCommand(showSettings));
        commandList.add(new ToggleCommand(showSettings, proximitySensor));
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length > 0) {
            for(ShowCommand showCommand : commandList) {
                if(!showCommand.getName().equalsIgnoreCase(args[0])) {
                    continue;
                }
                if(commandSender.hasPermission(showCommand.getPermission())) {
                    return showCommand.execute(commandSender, command, label, args);
                } else {
                    commandSender.sendMessage(this.showSettings.commands().noPermission());
                    return true;
                }
            }
        }

        ShowSong showSong = songManager.getCurrentSong();
        if(showSong == null) {
            commandSender.sendMessage(showSettings.stage().noCurrentSong());
            return true;
        }
        commandSender.sendMessage(buildInformation(showSong));
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender commandSender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(args.length > 1) {
            String next = args[0];
            for(ShowCommand showCommand : commandList) {
                if (!commandSender.hasPermission(showCommand.getPermission())) {
                    continue;
                }
                String name = showCommand.getName();
                if (name.equalsIgnoreCase(next) || (name.startsWith("%") && name.endsWith("%"))) {
                    return showCommand.tabComplete(commandSender, command, label, args);
                }
            }
        } else if(args.length == 1) {
            ArrayList<String> completionList = new ArrayList<>();
            String currentArg = args[args.length - 1].toLowerCase();
            for(ShowCommand showCommand : commandList) {
                if(!commandSender.hasPermission(showCommand.getPermission()))
                    continue;
                for(String s : showCommand.getCompletions())
                    if(s.toLowerCase().startsWith(currentArg))
                        completionList.add(s);
            }
            return completionList;
        }
        return new ArrayList<>();
    }

    private String buildInformation(ShowSong showSong) {
        long duration = showSong.getDuration();
        long seconds = duration % 60;
        long minutes = (duration - seconds) / 60;

        String information = showSong.getDescription();
        if(information.equals(""))
            information = showSettings.stage().information();
        information = information.replaceAll("%title%", showSong.getTitle());
        information = information.replaceAll("%artist%", showSong.getArtist());
        information = information.replaceAll("%album%", showSong.getAlbum());
        information = information.replaceAll("%year%", showSong.getYear() + "");
        information = information.replaceAll("%description%", showSong.getDescription());
        information = information.replaceAll("%duration%", minutes + ":" + seconds);

        return information;
    }
}
