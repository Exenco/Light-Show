package net.exenco.lightshow.show.song;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.ConfigHandler;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.SoundCategory;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class SongManager {
    private final ConfigHandler configHandler;
    private final ShowSettings showSettings;
    private final PacketHandler packetHandler;
    private final HashMap<Integer, ShowSong> songList = new HashMap<>();
    private ShowSong currentSong;
    public SongManager(ConfigHandler configHandler, ShowSettings showSettings, PacketHandler packetHandler) {
        this.configHandler = configHandler;
        this.showSettings = showSettings;
        this.packetHandler = packetHandler;
        loadSongs();
    }

    public void loadSongs() {
        songList.clear();

        File directory = new File("plugins//Light-Show//Songs");
        configHandler.createDirectory(directory);

        for(File file : Objects.requireNonNull(directory.listFiles())) {
            if(!file.getName().endsWith(".json"))
                continue;
            try {
                JsonObject jsonObject = configHandler.getJsonFromFile(file).getAsJsonObject();
                int id = jsonObject.get("Id").getAsInt();
                ShowSong showSong = new ShowSong(id, jsonObject);
                songList.put(id, showSong);
            } catch(Exception ignored) {}
        }
    }

    public void play(int id) {
        if(songList.get(id) == null)
            return;
        this.currentSong = songList.get(id);
        Vector location = showSettings.stage().location().toVector();
        packetHandler.playSound(location, currentSong.getSound(), currentSong.getSoundCategory(), 1.0F, 1.0F);
    }

    public void stop() {
        if(currentSong == null)
            return;
        packetHandler.stopSound(currentSong.getSound(), SoundCategory.RECORDS);
        this.currentSong = null;
    }

    public ShowSong getCurrentSong() {
        return currentSong;
    }
}
