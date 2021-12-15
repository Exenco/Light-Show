package net.exenco.lightshow.show.stage.effects;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.song.SongManager;
import net.exenco.lightshow.util.ShowSettings;

import java.util.logging.Logger;

public class SongSelector extends ShowEffect {

    private final int range;
    private final SongManager songManager;
    public SongSelector(ShowSettings showSettings, SongManager songManager) {
        super(new JsonObject());
        this.songManager = songManager;
        this.range = showSettings.showEffects().selector().maxValue();
    }

    @Override
    public int getDmxSize() {
        return 1;
    }

    @Override
    public void applyState(int[] data) {
        int id = Math.round((float) valueOfMax(range, data[0]));
        if(id == 0) {
            songManager.stop();
        } else {
            if(songManager.getCurrentSong() == null || songManager.getCurrentSong().getId() != id) {
                songManager.play(id);
            }
        }
    }
}
