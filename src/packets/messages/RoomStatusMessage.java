package packets.messages;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import packets.ChatPacket.PacketType;
import packets.ChatPayload;
import packing.PacketReader;
import packing.PacketWriter;
import util.LengthCalculator;
import util.LongInteger;
import util.PacketField;

public class RoomStatusMessage implements ChatPayload {
    @PacketField(size = 16)
    private LongInteger roomName;
    @PacketField(additional = 2)
    private Set<LongInteger> members;

    public RoomStatusMessage(byte[] data) {
        unPack(data);
    }

    public RoomStatusMessage(LongInteger roomName, Set<LongInteger> members) {
        this.roomName = roomName;
        this.members = members;
    }

    public LongInteger getRoomName() {
        return roomName;
    }

    public Set<LongInteger> getMembers() {
        return members;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeLongInteger(roomName);
        pw.writeShort((short) members.size());
        for (LongInteger member : members) {
            pw.writeLongInteger(member);
        }
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        roomName = pr.readLongInteger();
        members = new HashSet<LongInteger>();
        for (int i = 0; i < members.size(); i++) {
            members.add(pr.readLongInteger());
        }
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this) + 16 * members.size();
    }

    @Override
    public PacketType getType() {
        return PacketType.ROOM_STATUS;
    }
}
