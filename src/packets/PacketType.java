package packets;

public enum PacketType {
    CHAT_MESSAGE(1), USER_PRESENCE(2), ROOM_COMPARISON(3), ROOM_STATUS(4), USER_STATUS(
            5), PURGE(6), MANIFEST(7), PUSH(8);

    private byte type;
    PacketType(int type) {
        this.type = (byte)type;
    }

    public byte getValue() {
        return type;
    }

    public static PacketType fromValue(byte value) {
        for(PacketType t : PacketType.values()) {
            if(t.getValue() == value) {
                return t;
            }
        }
        return null;
    }
};