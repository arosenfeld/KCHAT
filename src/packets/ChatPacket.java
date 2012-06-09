package packets;

import java.io.IOException;

import packing.Packable;
import packing.PacketReader;
import packing.PacketWriter;

import util.LengthCalculator;
import util.LongInteger;
import util.PacketField;

/**
 * Handles encoding and decoding of all KCHAT packets. The payload varies
 * depending on the PacketType, but all header information is constant as
 * described in the design document.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public class ChatPacket implements Packable {
    // Enum for each packet type
    public enum PacketType {
        UNDEFINED(0), CHAT_MESSAGE(1), USER_PRESENCE(2), ROOM_COMPARISON(3), ROOM_STATUS(4), USER_STATUS(5), PURGE(6), MANIFEST(
                7), PUSH(8);

        private byte type;

        PacketType(int type) {
            this.type = (byte) type;
        }

        public byte getValue() {
            return type;
        }

        public static PacketType fromValue(byte value) {
            for (PacketType t : PacketType.values()) {
                if (t.getValue() == value) {
                    return t;
                }
            }
            return null;
        }
    }

    // Header fields and associated sizes
    @PacketField(size = 1)
    private byte version;
    @PacketField(size = 16)
    private LongInteger src;
    @PacketField(size = 1)
    private PacketType type;
    @PacketField(size = 4)
    private int sequence;
    @PacketField(additional = 1)
    private byte[][] extensions;
    private ChatPayload payload;

    /**
     * Unpacks a byte array into a ChatPacket instance.
     * 
     * @param data
     *            The raw byte array.
     */
    public ChatPacket(byte[] data) {
        unPack(data);
    }

    /**
     * Creates a ChatPacket from local information.
     * 
     * @param version
     *            The protocol version.
     * @param sequence
     *            The sequence number of the packet.
     * @param src
     *            The source of the packet
     * @param payload
     *            The payload of the packet.
     * @param extensions
     *            Any extensions to the packet.
     */
    public ChatPacket(byte version, int sequence, LongInteger src, ChatPayload payload, byte[][] extensions) {
        this.version = version;
        this.sequence = sequence;
        this.src = src;
        this.type = payload.getType();
        this.extensions = extensions;
        this.payload = payload;
    }

    public ChatPacket(byte version, int sequence, LongInteger src, ChatPayload payload) {
        this(version, sequence, src, payload, null);
    }

    public byte getVersion() {
        return version;
    }

    public int getSequence() {
        return sequence;
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

    public ChatPayload getPayload() {
        return payload;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeByte(version);
        pw.writeLongInteger(src);
        pw.writeByte(type.getValue());
        pw.writeInt(sequence);
        if (extensions != null) {
            pw.writeByte((byte) extensions.length);

            for (byte[] e : extensions) {
                pw.writeShort((short) e.length);
                pw.writeBytes(e);
            }
        } else {
            pw.writeByte((byte) 0);
        }
        pw.writeBytes(payload.pack());
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        version = pr.readByte();
        src = pr.readLongInteger();
        type = PacketType.fromValue(pr.readByte());
        sequence = pr.readInt();

        int numExt = pr.readByte();
        if (numExt > 0) {
            extensions = new byte[numExt][];
            for (int i = 0; i < extensions.length; i++) {
                extensions[i] = pr.readBytes(pr.readShort());
            }
        }
        payload = ChatPayloadCreator.createPayload(type, pr.getRemainder());
    }

    @Override
    public int getLength() {
        int len = LengthCalculator.getLength(this) + payload.getLength();
        if (extensions != null) {
            for (byte[] e : extensions) {
                len += e.length + 2;
            }
        }
        return len;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChatPacket) {
            ChatPacket other = (ChatPacket) obj;
            if (other.getSrc().equals(getSrc()) && other.getSequence() == getSequence()
                    && other.getType().equals(getType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.src != null ? this.src.hashCode() : 0);
        hash = 71 * hash + this.sequence;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nVersion: " + version + "\n");
        sb.append("Src: " + src.toString() + "\n");
        sb.append("Type: " + type.toString() + "\n");
        sb.append("Seq: " + sequence + "\n");
        sb.append("Payload: \n\n" + payload.toString() + "\n");

        return sb.toString();
    }
}