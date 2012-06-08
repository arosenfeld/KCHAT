package packets;

import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.PurgeMessage;
import packets.messages.RoomComparisonMessage;
import packets.messages.RoomStatusMessage;
import packets.messages.UserPresenceMessage;
import packets.messages.UserStatusMessage;

public class ChatPayloadCreator {
    public static ChatPayload createPayload(PacketType type, byte[] packedPayload) {
        // TODO: There may be a better way to do this...
        switch (type) {
        case CHAT_MESSAGE:
            return new ChatMessage(packedPayload);
        case PURGE:
            return new PurgeMessage(packedPayload);
        case ROOM_COMPARISON:
            return new RoomComparisonMessage(packedPayload);
        case ROOM_STATUS:
            return new RoomStatusMessage(packedPayload);
        case USER_PRESENCE:
            return new UserPresenceMessage(packedPayload);
        case USER_STATUS:
            return new UserStatusMessage(packedPayload);
        default:
            return null;
        }
    }
}
