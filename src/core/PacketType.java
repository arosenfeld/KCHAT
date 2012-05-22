package core;

public enum PacketType {
    CHAT_MESSAGE(1), USER_PRESENCE(2), ROOM_COMPARISON(3), ROOM_STATUS(4), USER_STATUS(
            5), PURGE(6), MANIFEST(7), PUSH(8);

    int type;

    PacketType(int type) {
        this.type = type;
    }
};