package packing;

import java.nio.ByteBuffer;

import util.BitField;
import util.LongInteger;

public class PacketReader {
    private ByteBuffer bb;

    public PacketReader(byte[] buf) {
        bb = ByteBuffer.wrap(buf);
    }

    public byte readByte() {
        return bb.get();
    }

    public byte[] readBytes(int len) {
        byte[] buf = new byte[len];
        bb.get(buf, 0, len);
        return buf;
    }

    public BitField readBitField() {
        return new BitField(bb.get());
    }

    public short readShort() {
        return bb.getShort();
    }

    public int readInt() {
        return bb.getInt();
    }

    public LongInteger readLongInteger() {
        return new LongInteger(readBytes(16));
    }

    public void position(int i) {
        bb.position(i);
    }

    public byte[] getRemainder() {
        return readBytes(bb.capacity() - bb.position());
    }
}