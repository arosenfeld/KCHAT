package net;

import java.io.IOException;

import packets.LengthCalculator;
import packets.PacketField;
import packets.PacketType;
import packing.Packable;
import packing.PacketReader;
import packing.PacketWriter;

import util.LongInteger;

public class ChatHeader implements Packable {
    @PacketField(size=1)
    private byte version;
    @PacketField(size=16)
    private LongInteger src;
    @PacketField(size=1)
    private PacketType type;
    @PacketField(additional=1)
    private byte[][] extensions;

    public ChatHeader(byte[] header) {
        unPack(header);
    }

    public ChatHeader(byte version, LongInteger src, PacketType type,
            byte[][] extensions) {
        this.version = version;
        this.src = src;
        this.type = type;
        this.extensions = extensions;
    }

    public ChatHeader(byte version, LongInteger src, PacketType type) {
        this(version, src, type, null);
    }

    public byte getVersion() {
        return version;
    }

    public LongInteger getSrc() {
        return src;
    }

    public PacketType getType() {
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
        pw.writeByte(type.getValue());
        if (extensions != null) {
            pw.writeByte((byte)extensions.length);
        
            for (byte[] e : extensions) {
                pw.writeShort((short) e.length);
                pw.writeBytes(e);
            }
        } else {
            pw.writeByte((byte)0);
        }
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        version = pr.readByte();
        src = pr.readLongInteger();
        type = PacketType.fromValue(pr.readByte());

        int numExt = pr.readByte();
        if(numExt > 0) {
            extensions = new byte[numExt][];
            for (int i = 0; i < extensions.length; i++) {
                extensions[i] = pr.readBytes(pr.readShort());
            }
        }
    }

    @Override
    public int getLength() {
        int len = LengthCalculator.getLength(this);
        if (extensions != null) {
            for (byte[] e : extensions) {
                len += e.length + 2;
            }
        }
        return len;
    }
}