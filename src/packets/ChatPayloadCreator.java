package packets;

import packets.ChatPacket.PacketType;

public class ChatPayloadCreator {
    public static ChatPayload createPayload(PacketType type, byte[] packedPayload) {
        // TODO: There may be a better way to do this...
        switch (type) {
        case CHAT_MESSAGE:
            return new ChatMessage(packedPayload);
        case PURGE:
            return new PurgeMessage(packedPayload);
        default:
            return null;
        }
    }
}
