package packets.messages;

import java.io.IOException;

import packets.ChatPacket.PacketType;
import packets.ChatPayload;
import packing.PacketReader;
import packing.PacketWriter;
import util.LengthCalculator;
import util.LongInteger;
import util.PacketField;

public class UserStatusMessage implements ChatPayload {
    @PacketField(size = 1)
    private StatusType type;
    @PacketField(size = 4)
    private LongInteger room;
    @PacketField(size = 4)
    private LongInteger user;

    public UserStatusMessage(byte[] data) {
        unPack(data);
    }

    public UserStatusMessage(StatusType type, LongInteger room, LongInteger user) {
        this.type = type;
        this.room = room;
        this.user = user;
    }

    public StatusType getStatusType() {
        return type;
    }

    public LongInteger getRoom() {
        return room;
    }

    public LongInteger getUser() {
        return user;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeByte(type.value);
        pw.writeLongInteger(room);
        pw.writeLongInteger(user);
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        type = StatusType.fromValue(pr.readByte());
        room = pr.readLongInteger();
        user = pr.readLongInteger();
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this);
    }

    @Override
    public PacketType getType() {
        return PacketType.USER_STATUS;
    }

    public enum StatusType {
        QUERY((byte) 0), PRESENT((byte) 1), NOT_PRESENT((byte) 2);

        private byte value;

        StatusType(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        public static StatusType fromValue(byte value) {
            for (StatusType t : StatusType.values()) {
                if (t.getValue() == value) {
                    return t;
                }
            }
            return null;
        }
    }
}
