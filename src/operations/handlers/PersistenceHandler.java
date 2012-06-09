package operations.handlers;

import java.io.IOException;

import operations.PacketMatcher.TypeMatcher;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.ManifestMessage;
import packets.messages.PushMessage;
import util.Logging;
import core.ChatSocket;
import core.PersistenceManager.Range;

public class PersistenceHandler extends Handler {
    @Override
    public boolean accepts(ChatPacket packet) {
        return new TypeMatcher(PacketType.MANIFEST).matches(packet);
    }

    @Override
    public void process(ChatSocket sock, ChatPacket packet) {
        ManifestMessage mfst = (ManifestMessage) packet.getPayload();
        for (Range missing : mfst.getRanges()) {
            for (int seq = 0; seq < missing.size; seq++) {
                ChatPacket local = sock.getPersistenceManager().getPacket(mfst.getSrc(), missing.start + seq);
                if (local != null) {
                    try {
                        sock.sendPacket(sock.wrapPayload(new PushMessage(mfst.getSrc(), (ChatMessage) local
                                .getPayload())));
                    } catch (IOException e) {
                        Logging.getLogger().warning("Unable to send PushMessage");
                    }
                }
            }
        }
    }
}
