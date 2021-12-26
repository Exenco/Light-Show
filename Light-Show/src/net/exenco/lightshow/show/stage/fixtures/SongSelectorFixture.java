package net.exenco.lightshow.show.stage.fixtures;

import com.google.gson.JsonObject;
import net.exenco.lightshow.show.song.SongManager;
import net.exenco.lightshow.show.stage.StageManager;

public class SongSelectorFixture extends ShowFixture {

    private final int range;
    private final SongManager songManager;
    public SongSelectorFixture(JsonObject configJson, StageManager stageManager) {
        super(configJson, stageManager);
        this.songManager = stageManager.getSongManager();
        this.range = stageManager.getShowSettings().showEffects().selector().maxValue();
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
