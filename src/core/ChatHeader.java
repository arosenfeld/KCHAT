package core;

import java.io.IOException;
import util.LongInteger;
import util.Packable;
import util.PacketReader;
import util.PacketWriter;

public class ChatHeader implements Packable {
    private byte version;
    private LongInteger src;
    private byte type;
    private byte numExtensions;
    private byte[][] extensions;

    public ChatHeader(byte[] header) {
        unPack(header);
    }

    public ChatHeader(byte version, LongInteger src, byte type,
            byte[][] extensions) {
        this.extensions = extensions;

        if (extensions == null) {
            this.numExtensions = 0;
        } else {
            this.numExtensions = (byte) extensions.length;
        }
    }

    public byte getVersion() {
        return version;
    }

    public LongInteger getSrc() {
        return src;
    }

    public byte getType() {
        return type;
    }

    public byte[][] getExtensions() {
        return extensions;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeByte(version);
        pw.writeLongInteger(src);
        pw.writeByte(type);
        pw.writeByte(numExtensions);
        if (numExtensions > 0) {
            for (byte[] e : extensions) {
                pw.writeShort((short) e.length);
                pw.writeBytes(e);
            }
        }
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        version = pr.readByte();
        src = pr.readLongInteger();
        type = pr.readByte();

        numExtensions = pr.readByte();
        extensions = new byte[numExtensions][];
        for (byte[] b : extensions) {
            b = pr.readBytes(pr.readShort());
        }
    }

    @Override
    public int getLength() {
        int len = 8 + 128 + 8 + 8;
        for (byte[] e : extensions) {
            len += e.length + 2;
        }
        return len;
    }
}