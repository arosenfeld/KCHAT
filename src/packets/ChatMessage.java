package packets;

import java.io.IOException;

import packets.ChatPacket.PacketType;
import packing.PacketReader;
import packing.PacketWriter;

import util.BitField;
import util.LengthCalculator;
import util.LongInteger;
import util.PacketField;

public class ChatMessage implements ChatPayload {
    public enum MessageField {
        TO_ROOM((byte) 0), PERSIST((byte) 1);

        private byte index;

        MessageField(byte index) {
            this.index = index;
        }

        public byte getValue() {
            return index;
        }
    }

    @PacketField(size = 1)
    private BitField params;
    @PacketField(size = 4)
    private int messageId;
    @PacketField(size = 4)
    private int persistenceId;
    @PacketField(size = 16)
    private LongInteger dest;
    @PacketField(additional = 2)
    private byte[] message;

    public ChatMessage(byte[] data) {
        this.params = new BitField();
        unPack(data);
    }

    public ChatMessage(int messageId, int persistenceId, LongInteger dest, byte[] message) {
        this.messageId = messageId;
        this.params = new BitField();
        this.persistenceId = persistenceId;
        this.dest = dest;
        this.message = message;
    }

    public ChatMessage(int messageId, LongInteger dest, byte[] message) {
        this(messageId, 0, dest, message);
    }
    
    public int getMessageId() {
        return messageId;
    }

    public int getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceId(int persistenceId) {
        this.persistenceId = persistenceId;
    }

    public LongInteger getDest() {
        return dest;
    }

    public byte[] getMessage() {
        return message;
    }

    public boolean getParam(MessageField field) {
        return params.isSet(field.getValue());
    }

    public void setParam(MessageField field, boolean set) {
        params.setBit(field.getValue(), set);
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();

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
        params = pr.readBitField();
        messageId = pr.readInt();
        persistenceId = pr.readInt();
        dest = pr.readLongInteger();
        message = pr.readBytes(pr.readShort());
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this) + message.length;
    }

    @Override
    public PacketType getType() {
        return PacketType.CHAT_MESSAGE;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Params: ");
        buf.append(params.toString());
        buf.append("\nPersistenceId: ");
        buf.append(persistenceId);
        buf.append("\nDest: ");
        buf.append(dest.toString());
        buf.append("\nMessage: ");
        buf.append(new String(message));
        buf.append("\n");

        return buf.toString();
    }
}
