package packets.messages;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
    private Set<Integer> seqs;

    public ManifestMessage(byte[] data) {
        unPack(data);
    }

    public ManifestMessage(LongInteger src, Set<Integer> seqs) {
        this.src = src;
        this.seqs = seqs;
    }

    public LongInteger getSrc() {
        return src;
    }

    public Set<Integer> getSeqs() {
        return seqs;
    }

    @Override
    public PacketType getType() {
        return PacketType.MANIFEST;
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this) + seqs.size() * 4;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeLongInteger(src);
        short sz = (short) seqs.size();
        pw.writeShort(sz);
        for (int s : seqs) {
            pw.writeInt(s);
        }
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        this.src = pr.readLongInteger();
        this.seqs = new HashSet<Integer>();
        short sz = pr.readShort();
        for (int i = 0; i < sz; i++) {
            int s = pr.readInt();
            this.seqs.add(s);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Src: " + src + "\n");
        sb.append("Size: " + seqs.size() + "\n");
        for (int index : seqs) {
            sb.append("    " + index);
        }

        return sb.toString();
    }
}
