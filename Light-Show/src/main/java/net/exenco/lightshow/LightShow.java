package net.exenco.lightshow;

import net.exenco.lightshow.executor.ShowExecutor;
import net.exenco.lightshow.listener.PlayerMoveListener;
import net.exenco.lightshow.show.song.SongManager;
import net.exenco.lightshow.show.stage.StageManager;
import net.exenco.lightshow.show.stage.fixtures.*;
import net.exenco.lightshow.util.*;
import net.exenco.lightshow.util.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * Main Class of the Light-Show Plugin for Spigot 1.19.4
 */
public class LightShow extends JavaPlugin {
    private ConfigHandler configHandler;
    private ShowSettings showSettings;
    private ProximitySensor proximitySensor;
    private PacketHandler packetHandler;
    private SongManager songManager;
    private StageManager stageManager;

    /**
     * Called when the plugin is being enabled.
     * Used to initialise everything.
     */
    @Override
    public void onEnable() {
        /* Initialise Attributes */

        /* World unspecific */
        this.configHandler = new ConfigHandler(this);
        this.showSettings = new ShowSettings(configHandler);

        /* World specific */
        this.proximitySensor = new ProximitySensor(showSettings);
        this.packetHandler = new PacketHandler(this, proximitySensor, showSettings);
        this.proximitySensor.setPacketHandler(packetHandler);
        this.songManager = new SongManager(configHandler, showSettings, packetHandler);
        this.stageManager = new StageManager(this, configHandler, showSettings, songManager, packetHandler);

        /* Register Fixtures */
        this.stageManager.registerFixture("Command", CommandFixture.class);
        this.stageManager.registerFixture("Beacon", BeaconFixture.class);
        this.stageManager.registerFixture("BlockChanger", BlockChangerFixture.class);
        this.stageManager.registerFixture("BlockUpdater", BlockUpdaterFixture.class);
        this.stageManager.registerFixture("Crystal", CrystalFixture.class);
        this.stageManager.registerFixture("FireworkLauncher", FireworkFixture.class);
        this.stageManager.registerFixture("FogMachine", FogMachineFixture.class);
        this.stageManager.registerFixture("LogoDisplay", LogoFixture.class);
        this.stageManager.registerFixture("MovingHead", MovingHeadFixture.class);
        this.stageManager.registerFixture("ParticleFlare", ParticleFlareFixture.class);
        this.stageManager.registerFixture("SongSelector", SongSelectorFixture.class);
        this.stageManager.load();

        /* Initialise listener */
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerMoveListener(proximitySensor), this);

        /* Initialise executor */
        PluginCommand pluginCommand = Objects.requireNonNull(getCommand("show"));
        ShowExecutor showExecutor = new ShowExecutor(this, showSettings, stageManager, songManager, proximitySensor);
        pluginCommand.setExecutor(showExecutor);
        pluginCommand.setTabCompleter(showExecutor);

    }

    /**
     * Called when the plugin is being disabled.
     * Used to reset changes or stop still running threads.
     */
    @Override
    public void onDisable() {
        if(this.stageManager != null) {
            this.stageManager.stop();
        }
        if(this.packetHandler != null) {
            this.packetHandler.resetEverything();
        }
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
        this.songManager.loadSongs();
        this.stageManager.load();

        this.getLogger().info("Successfully reloaded!");
    }
}
