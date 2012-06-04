package packets;

import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.PurgeMessage;
import packets.messages.RoomComparisonMessage;
import packets.messages.UserPresenceMessage;

public class ChatPayloadCreator {
    public static ChatPayload createPayload(PacketType type, byte[] packedPayload) {
        // TODO: There may be a better way to do this...
        switch (type) {
        case CHAT_MESSAGE:
            return new ChatMessage(packedPayload);
        case PURGE:
            return new PurgeMessage(packedPayload);
        case USER_PRESENCE:
            return new UserPresenceMessage(packedPayload);
        case ROOM_COMPARISON:
            return new RoomComparisonMessage(packedPayload);
        default:
            return null;
        }
    }
}
