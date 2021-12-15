package net.exenco.lightshow.show.song;

import com.google.gson.JsonObject;
import net.exenco.lightshow.util.PacketHandler;
import net.exenco.lightshow.util.file.ConfigHandler;
import net.exenco.lightshow.util.ShowSettings;
import org.bukkit.SoundCategory;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

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
        for(Map.Entry<Integer, String> entry : showSettings.songMap().entrySet()) {
            int id = entry.getKey();
            JsonObject songJson = configHandler.getSongJson(entry.getValue());
            ShowSong showSong = new ShowSong(id, songJson);
            songList.put(id, showSong);
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
