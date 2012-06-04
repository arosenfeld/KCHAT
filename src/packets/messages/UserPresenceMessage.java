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

public class UserPresenceMessage implements ChatPayload {
    @PacketField(size = 16)
    private LongInteger roomName;
    @PacketField(size = 1)
    private PresenceStatus status;

    public UserPresenceMessage(byte[] data) {
        unPack(data);
    }

    public UserPresenceMessage(LongInteger roomName, PresenceStatus status) {
        this.roomName = roomName;
        this.status = status;
    }

    public LongInteger getRoomName() {
        return roomName;
    }

    public PresenceStatus getPresenceStatus() {
        return status;
    }

    @Override
    public byte[] pack() throws IOException {
        PacketWriter pw = new PacketWriter();
        pw.writeLongInteger(roomName);
        pw.writeByte(status.getValue());
        return pw.getArray();
    }

    @Override
    public void unPack(byte[] data) {
        PacketReader pr = new PacketReader(data);
        roomName = pr.readLongInteger();
        status = PresenceStatus.fromValue(pr.readByte());
    }

    @Override
    public int getLength() {
        return LengthCalculator.getLength(this);
    }

    @Override
    public PacketType getType() {
        return PacketType.USER_PRESENCE;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Room Name: " + roomName + "\n");
        sb.append("Status: " + status + "\n");
        return sb.toString();
    }

    public enum PresenceStatus {
        JOIN((byte) 1), LEAVE((byte) 0);

        private byte value;

        PresenceStatus(byte value) {
            this.value = value;
        }

        public byte getValue() {
            return value;
        }

        public static PresenceStatus fromValue(byte value) {
            for (PresenceStatus t : PresenceStatus.values()) {
                if (t.getValue() == value) {
                    return t;
                }
            }
            return null;
        }
    }
}
