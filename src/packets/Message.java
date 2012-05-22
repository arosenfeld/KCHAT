package packets;

import java.io.IOException;

import util.BitField;
import util.LongInteger;
import util.PacketReader;
import util.PacketWriter;
import core.ChatHeader;
import core.ChatPacket;
import core.PacketType;

public class Message extends ChatPacket {
    public enum ChatFields {
        TO_ROOM((byte) 0), PERSIST((byte) 1);

        private byte index;
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
    
    public Message(byte[] data) {
        super(new ChatHeader(data));
        this.params = new BitField();
        unPack(data);
    }

    public Message(ChatHeader header, int messageId, int persistenceId,
            LongInteger dest, byte[] message) {
        super(header);
        this.params = new BitField();
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
        return header.getLength() + 1 + 4 + 4 + 16 + 2 + message.length;
    }
    
    @Override
    protected PacketType getPacketType() {
        return PacketType.CHAT_MESSAGE;
    }
    
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Params: ");
        buf.append(params.toString());
        buf.append("\nMessageId: ");
        buf.append(messageId);
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
