package packets;

import java.io.IOException;

import packets.ChatPacket.PacketType;
import packing.PacketReader;
import packing.PacketWriter;
import util.LengthCalculator;
import util.Logging;
import util.PacketField;

public class PurgeMessage implements ChatPayload {
    @PacketField(size = 4)
    private int messageId;

    public PurgeMessage(byte[] data) {
        unPack(data);
    }

    public PurgeMessage(int messageId) {
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeInt(messageId);
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);

        messageId = pr.readInt();
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this);
    }

    @Override
    public PacketType getType() {
        return PacketType.PURGE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Message ID: " + messageId);

        return sb.toString();
    }
}
