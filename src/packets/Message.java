package packets;

import java.io.IOException;

import util.BitField;
import util.LongInteger;
import util.PacketReader;
import util.PacketWriter;
import core.ChatHeader;
import core.ChatPacket;

public class Message extends ChatPacket {
    public enum ChatFields {
        TO_ROOM((byte) 0), PERSIST((byte) 1);

        byte index;

        ChatFields(byte index) {
            this.index = index;
        }

        public byte getValue() {
            return index;
        }
    }

    private BitField params;
    private int messageId;
    private int persistenceId;
    private LongInteger dest;
    private byte[] message;

    public Message(ChatHeader header, int messageId, int persistenceId,
            LongInteger dest, byte[] message) {
        super(header);
        this.messageId = messageId;
        this.persistenceId = persistenceId;
        this.dest = dest;
        this.message = message;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getPersistenceId() {
        return persistenceId;
    }

    public LongInteger getDest() {
        return dest;
    }

    public byte[] getMessage() {
        return message;
    }

    public boolean getParam(ChatFields field) {
        return params.isSet(field.getValue());
    }

    public void setParam(ChatFields field, boolean set) {
        params.setBit(field.getValue(), set);
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeBytes(header.pack());
        pw.writeByte(params.getValue());
        pw.writeInt(messageId);
        pw.writeInt(persistenceId);
        pw.writeLongInteger(dest);
        pw.writeShort((short) message.length);
        pw.writeBytes(message);

        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        header = new ChatHeader(data);

        pr.position(header.getLength());
        params = pr.readBitField();
        messageId = pr.readInt();
        persistenceId = pr.readInt();
        dest = pr.readLongInteger();
        message = pr.readBytes(pr.readShort());
    }

    @Override
    public int getLength() {
        return header.getLength() + 8 + 32 + 32 + 128 + 16 + message.length;
    }
}
