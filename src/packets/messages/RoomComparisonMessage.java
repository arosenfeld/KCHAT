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

public class RoomComparisonMessage implements ChatPayload {
    @PacketField(size = 16)
    private LongInteger roomName;

    @PacketField(size = 16)
    private LongInteger membersHash;

    public RoomComparisonMessage(byte[] data) {
        unPack(data);
    }

    public RoomComparisonMessage(LongInteger roomName, LongInteger membersHash) {
        this.roomName = roomName;
        this.membersHash = membersHash;
    }

    public LongInteger getRoomName() {
        return roomName;
    }

    public LongInteger getMembersHash() {
        return membersHash;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeLongInteger(roomName);
        pw.writeLongInteger(membersHash);
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        roomName = pr.readLongInteger();
        membersHash = pr.readLongInteger();
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this);
    }

    @Override
    public PacketType getType() {
        return PacketType.ROOM_COMPARISON;
    }
}
