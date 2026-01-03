package net.exenco.lightshow.show.artnet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ArtNetPacket {
    public static final byte[] PACKET_HEADER = "Art-Net\0".getBytes();

    private final int universeID;
    private final byte[] dmxData = new byte[530];

    public ArtNetPacket(byte[] data) {
        universeID = ByteBuffer.wrap(data, 14, 8).get();
        int numChannels = ByteBuffer.wrap(data, 16, 2).getShort();
        System.arraycopy(data, 18, dmxData, 0, Math.min(numChannels, data.length - 18));
    }

    public byte[] getDmx() {
        return dmxData;
    }

    public int getUniverseID() {
        return universeID;
    }

    private static boolean isValidHeader(byte[] data) {
        boolean equal = true;
        for (int i = 0; i < ArtNetPacket.PACKET_HEADER.length && equal; i++)
            equal = data[i] == ArtNetPacket.PACKET_HEADER[i];
        return equal;
    }

    public static ArtNetPacket valueOf(byte[] raw) {
        if (raw.length < 20)
            return null;
        int opCode = ByteBuffer.wrap(raw, 8, 16).order(ByteOrder.LITTLE_ENDIAN).getShort();
        if (opCode == 0x5000 && isValidHeader(raw))
            return new ArtNetPacket(raw);
        return null;
    }
}