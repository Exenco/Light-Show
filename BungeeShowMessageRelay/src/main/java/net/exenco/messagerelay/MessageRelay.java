package net.exenco.messagerelay;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.UUID;

public class MessageRelay extends Plugin {

    private UUID allowedUUID;

    public void onEnable() {
        PluginManager pluginManager = this.getProxy().getPluginManager();
        pluginManager.registerListener(this, new MessageListener(this));
        try {
            createConfig();
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
            this.allowedUUID = UUID.fromString(configuration.get("SenderUuid").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDisable() {

    }

    public UUID getAllowedUUID() {
        return allowedUUID;
    }

    private void createConfig() {
        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        File file = new File(this.getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
