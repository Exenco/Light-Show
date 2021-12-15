package net.exenco.lightshow;

import net.exenco.lightshow.executor.ShowExecutor;
import net.exenco.lightshow.listener.PlayerMoveListener;
import net.exenco.lightshow.show.song.SongManager;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.util.*;
import net.exenco.lightshow.util.file.ConfigHandler;
import net.exenco.lightshow.util.registries.FireworkRegistry;
import net.exenco.lightshow.util.registries.LogoRegistry;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Main Class of the Light-Show Plugin for Spigot 1.17.1.
 */
public class LightShow extends JavaPlugin {
    private ConfigHandler configHandler;
    private ShowSettings showSettings;
    private ProximitySensor proximitySensor;
    private PacketHandler packetHandler;
    private SongManager songManager;
    private LogoRegistry logoRegistry;
    private FireworkRegistry fireworkRegistry;
    private StageManager stageManager;

    /**
     * Called when the plugin is being enabled.
     * Used to initialise everything.
     */
    @Override
    public void onEnable() {
        /* Initialise Attributes */

        /* World uninterested */
        this.configHandler = new ConfigHandler(this);
        this.showSettings = new ShowSettings(configHandler);

        /* World specific */
        this.proximitySensor = new ProximitySensor(showSettings);
        this.packetHandler = new PacketHandler(this, proximitySensor, showSettings);
        this.proximitySensor.setPacketHandler(packetHandler);
        this.songManager = new SongManager(configHandler, showSettings, packetHandler);
        this.logoRegistry = new LogoRegistry(configHandler, showSettings);
        this.fireworkRegistry = new FireworkRegistry(configHandler, showSettings);
        this.stageManager = new StageManager(this, configHandler, showSettings, songManager, packetHandler, fireworkRegistry, logoRegistry);

        /* Initialise listener */
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerMoveListener(proximitySensor), this);

        /* Initialise executor */
        PluginCommand pluginCommand = Objects.requireNonNull(getCommand("show"));
        ShowExecutor showExecutor = new ShowExecutor(this, showSettings, stageManager, songManager);
        pluginCommand.setExecutor(showExecutor);
        pluginCommand.setTabCompleter(showExecutor);
    }

    /**
     * Called when the plugin is being disabled.
     * Used to reset changes or stop still running threads.
     */
    @Override
    public void onDisable() {
        if(stageManager != null)
            stageManager.stop();
        if(packetHandler != null)
            packetHandler.resetEverything();
    }

    /**
     * Reloads the plugin and all its contents.
     * Does stop but not restart Art-Net if it was running.
     */
    public void reload() {
        this.getLogger().info("Reloading...");

        this.stageManager.stop();
        this.songManager.stop();
        this.packetHandler.resetEverything();

        this.configHandler.load();
        this.showSettings.load();
        this.proximitySensor.load();
        this.logoRegistry.load();
        this.fireworkRegistry.load();
        this.songManager.loadSongs();
        this.stageManager.load();

        this.getLogger().info("Successfully reloaded!");
    }
}
