package net.exenco.lightshow.show.artnet;

import java.nio.ByteBuffer;

class ArtNetPacket {
    public static final byte[] PACKET_HEADER = "Art-Net\0".getBytes();

    private final int universeID;
    private final byte[] dmxData = new byte[520];

    public ArtNetPacket(byte[] data) {
        universeID = ByteBuffer.wrap(data, 14, 8).get();
        int numChannels = ByteBuffer.wrap(data, 16, 2).getShort();
        System.arraycopy(data, 18, dmxData, 0, numChannels);
    }

    public byte[] getDmx() {
        return dmxData;
    }

    public int getUniverseID() {
        return universeID;
    }

}