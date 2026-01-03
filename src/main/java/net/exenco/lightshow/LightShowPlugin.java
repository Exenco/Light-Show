package net.exenco.lightshow;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.exenco.lightshow.commands.LsCommand;
import net.exenco.lightshow.listener.PlayerMoveListener;
import net.exenco.lightshow.show.ShowStage;
import net.exenco.lightshow.util.PluginFiles;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class LightShowPlugin extends JavaPlugin {

    private PluginFiles pluginFiles;

    private ShowStage stage;

    @Override
    public void onEnable() {
        this.pluginFiles = new PluginFiles(this);

        this.stage = new ShowStage(this);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerMoveListener(stage), this);


        LifecycleEventManager<@NotNull Plugin> manager = getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(new LsCommand(this).build(), "Access light show system");
        });
    }

    @Override
    public void onDisable() {
        stage.getArtNetReceiver().stop();
    }

    public void reload() {
        getLogger().info("Reloading...");

        this.reloadConfig();
        pluginFiles.load();
        stage.load();
    }

    public ShowStage getStage() {
        return stage;
    }

    public PluginFiles getPluginFiles() {
        return pluginFiles;
    }
}
