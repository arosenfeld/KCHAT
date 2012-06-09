package packets.messages;

import java.io.IOException;

import core.PersistenceManager.Range;

import packets.ChatPayload;
import packets.ChatPacket.PacketType;
import packing.PacketReader;
import packing.PacketWriter;
import util.LengthCalculator;
import util.LongInteger;
import util.PacketField;

public class ManifestMessage implements ChatPayload {
    @PacketField(size = 16)
    private LongInteger src;
    @PacketField(additional = 2)
    private Range[] ranges;

    public ManifestMessage(byte[] data) {
        unPack(data);
    }

    public ManifestMessage(LongInteger src, Range[] ranges) {
        this.src = src;
        this.ranges = ranges;
    }

    public LongInteger getSrc() {
        return src;
    }

    public Range[] getRanges() {
        return ranges;
    }

    @Override
    public PacketType getType() {
        return PacketType.MANIFEST;
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this) + ranges.length * 5;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeLongInteger(src);
        pw.writeShort((short) ranges.length);
        for (Range r : ranges) {
            pw.writeInt(r.start);
            pw.writeShort(r.size);
        }
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        src = pr.readLongInteger();
        ranges = new Range[pr.readShort()];
        for (int i = 0; i < ranges.length; i++) {
            ranges[i] = new Range(pr.readInt(), pr.readShort());
        }
    }
}
