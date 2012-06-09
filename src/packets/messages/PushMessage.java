package packets.messages;

import java.io.IOException;

import packets.ChatPacket;
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
    private ChatPacket packet;

    public PushMessage(byte[] data) {
        unPack(data);
    }

    public PushMessage(LongInteger originalSrc, ChatPacket packet) {
        this.originalSrc = originalSrc;
        this.packet = packet;
    }

    public LongInteger getOriginalSrc() {
        return originalSrc;
    }

    public ChatPacket getPacket() {
        return packet;
    }

    @Override
    public PacketType getType() {
        return PacketType.PUSH;
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this) + packet.getLength();
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeLongInteger(originalSrc);
        pw.writeBytes(packet.pack());
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        originalSrc = pr.readLongInteger();
        packet = new ChatPacket(pr.getRemainder());
    }
}
