package packets.messages;

import java.io.IOException;

import packets.ChatPayload;
import packets.ChatPacket.PacketType;
import packing.PacketReader;
import packing.PacketWriter;
import util.LengthCalculator;
import util.LongInteger;
import util.PacketField;

public class PushMessage implements ChatPayload {
    @PacketField(size = 16)
    private LongInteger originalSrc;
    private ChatMessage msg;

    public PushMessage(LongInteger originalSrc, ChatMessage msg) {
        this.originalSrc = originalSrc;
        this.msg = msg;
    }

    @Override
    public PacketType getType() {
        return PacketType.PUSH;
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this) + msg.getLength();
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeLongInteger(originalSrc);
        pw.writeBytes(msg.pack());
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        originalSrc = pr.readLongInteger();
        msg = new ChatMessage(pr.getRemainder());
    }
}
