package packets.messages;

import java.io.IOException;

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
    private LongInteger[] members;

    public RoomStatusMessage(byte[] data) {
        unPack(data);
    }

    public RoomStatusMessage(LongInteger roomName, LongInteger[] members) {
        this.roomName = roomName;
        this.members = members;
    }
    
    public LongInteger getRoomName() {
        return roomName;
    }
    
    public LongInteger[] getMembers() {
        return members;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeLongInteger(roomName);
        pw.writeShort((short) members.length);
        for (LongInteger member : members) {
            pw.writeLongInteger(member);
        }
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        roomName = pr.readLongInteger();
        members = new LongInteger[pr.readShort()];
        for (int i = 0; i < members.length; i++) {
            members[i] = pr.readLongInteger();
        }
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this) + 16 * members.length;
    }

    @Override
    public PacketType getType() {
        return PacketType.ROOM_STATUS;
    }
}
