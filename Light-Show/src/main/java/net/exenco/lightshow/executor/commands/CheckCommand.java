package net.exenco.lightshow.executor.commands;

import net.exenco.lightshow.LightShow;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;

public class CheckCommand extends ShowCommand {

    private final LightShow lightShow;
    private final StageManager stageManager;
    public CheckCommand(LightShow lightShow, ShowSettings showSettings, StageManager stageManager) {
        super(showSettings);
        this.lightShow = lightShow;
        this.stageManager = stageManager;
    }

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public String getPermission() {
        return "lightshow.check";
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
        ShowSettings.ArtNet artNet = showSettings.artNet();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (stageManager.confirmReceiving()) {
                    commandSender.sendMessage(artNet.connected());
                } else {
                    commandSender.sendMessage(artNet.notConnected());
                }
            }
        }.runTaskAsynchronously(lightShow);
        return true;
    }
}