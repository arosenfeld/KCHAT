package packing;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import util.BitField;
import util.LongInteger;

public class PacketWriter {
    private DataOutputStream os;
    private ByteArrayOutputStream baos;

    public PacketWriter() {
        baos = new ByteArrayOutputStream();
        os = new DataOutputStream(baos);
    }

    public void writeByte(byte v) throws IOException {
        os.writeByte(v);
    }

    public void writeBytes(byte[] v) throws IOException {
        os.write(v);
    }

    public void writeBitField(BitField v) throws IOException {
        os.writeByte(v.getValue());
    }

    public void writeShort(short v) throws IOException {
        os.writeShort(v);
    }

    public void writeInt(int v) throws IOException {
        os.writeInt(v);
    }

    public void writeLongInteger(LongInteger v) throws IOException {
        os.write(v.getValue());
    }

    public byte[] getArray() {
        return baos.toByteArray();
    }
}