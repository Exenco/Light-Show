package net.exenco.lightshow.util;

import java.util.HashMap;
import java.util.Map;

public class DmxBuffer {

    private final Map<Integer, byte[]> liveData = new HashMap<>();

    public synchronized void setDmxData(int universeId, byte[] dmx) {
        byte[] copy = dmx.clone();
        liveData.put(universeId, copy);
    }

    public synchronized byte[] getDmxData(int universeId) {
        return liveData.computeIfAbsent(universeId, id -> new byte[512]);
    }
}


