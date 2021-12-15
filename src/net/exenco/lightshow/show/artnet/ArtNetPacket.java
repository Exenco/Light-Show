package net.exenco.lightshow.show.artnet;

import java.nio.ByteBuffer;

class ArtNetPacket {
    public static final byte[] PACKET_HEADER = "Art-Net\0".getBytes();

    private int universeID;
    private final byte[] dmxData = new byte[520];

    public byte[] getDmx() {
        return dmxData;
    }

    public int getUniverseID() {
        return universeID;
    }

    public void parse(byte[] data) {
        int subnetUniverse = ByteBuffer.wrap(data, 14, 8).get();
        universeID = subnetUniverse & 0x0f;
        int numChannels = ByteBuffer.wrap(data, 16, 2).getShort();
        System.arraycopy(data, 18, dmxData, 0, numChannels);
    }
}