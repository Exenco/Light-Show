package net.exenco.lightshow.show.artnet;

import java.util.HashMap;

/**
 * Class to buffer all input. Necessary for flawless communication between two runnables.
 */
class ArtNetBuffer {
    private final HashMap<Integer, byte[]> data;

    public ArtNetBuffer() {
        this.data = new HashMap<>();
    }

    public void setDmxData(int universeId, byte[] dmx) {
        data.put(universeId, dmx);
    }

    public byte[] getDmxData(int universeId) {
        if(!data.containsKey(universeId))
            data.put(universeId, new byte[512]);

        return data.get(universeId);
    }

    public void clear() {
        data.clear();
    }
}
