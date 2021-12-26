package net.exenco.lightshow.show.song;

import com.google.gson.JsonObject;
import org.bukkit.SoundCategory;

public class ShowSong {
    private final int id;
    private final String title;
    private final String artist;
    private final String album;
    private final int year;
    private final String description;
    private final long duration;
    private final String sound;
    private final SoundCategory soundCategory;

    public ShowSong(int id, JsonObject songJson) {
        this.id = id;
        this.title = songJson.get("Title").getAsString();
        this.artist = songJson.get("Artist").getAsString();
        this.album = songJson.get("Album").getAsString();
        this.year = songJson.get("Year").getAsInt();
        this.description = songJson.has("Description") ? songJson.get("Description").getAsString() : "";
        this.duration = songJson.get("Duration").getAsLong();
        this.sound = songJson.get("Sound").getAsString();
        this.soundCategory = songJson.has("SoundCategory") ? SoundCategory.valueOf(songJson.get("SoundCategory").getAsString().toUpperCase()) : SoundCategory.RECORDS;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public int getYear() {
        return year;
    }

    public String getDescription() {
        return description;
    }

    public long getDuration() {
        return duration;
    }

    public String getSound() {
        return sound;
    }

    public SoundCategory getSoundCategory() {
        return soundCategory;
    }
}
