package operations.handlers;

import operations.PacketMatcher.TypeMatcher;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ManifestMessage;
import core.ChatSocket;

public class PersistenceHandler extends Handler {
    @Override
    public boolean accepts(ChatPacket packet) {
        return new TypeMatcher(PacketType.MANIFEST).matches(packet);
    }

    @Override
    public void process(ChatSocket sock, ChatPacket packet) {
        ManifestMessage mfst = (ManifestMessage) packet.getPayload();
    }
}
