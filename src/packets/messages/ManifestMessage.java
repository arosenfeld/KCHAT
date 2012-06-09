package packets.messages;

import java.io.IOException;

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
    @PacketField(size = 4)
    private int max;
    @PacketField(additional = 2)
    private int[] seqs;

    public ManifestMessage(byte[] data) {
        unPack(data);
    }

    public ManifestMessage(LongInteger src, int max, int[] seqs) {
        this.src = src;
        this.max = max;
        this.seqs = seqs;
    }

    public LongInteger getSrc() {
        return src;
    }

    public int getMax() {
        return max;
    }

    public int[] getSeqs() {
        return seqs;
    }

    @Override
    public PacketType getType() {
        return PacketType.MANIFEST;
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this) + seqs.length * 4;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeLongInteger(src);
        pw.writeShort((short) seqs.length);
        for(int s : seqs) {
            pw.writeInt(s);
        }
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        src = pr.readLongInteger();
        max = pr.readInt();
        seqs = new int[pr.readShort()];
        for (int i = 0; i < seqs.length; i++) {
            seqs[i] = pr.readInt();
        }
    }
}
