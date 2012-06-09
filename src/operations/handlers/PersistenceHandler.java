package operations.handlers;

import java.io.IOException;
import java.util.Set;

import operations.PacketMatcher.TypeMatcher;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ManifestMessage;
import packets.messages.PushMessage;
import util.Logging;
import core.ChatSocket;

public class PersistenceHandler extends Handler {
    @Override
    public boolean accepts(ChatPacket packet) {
        return new TypeMatcher(PacketType.MANIFEST, PacketType.PURGE).matches(packet);
    }

    @Override
    public void process(ChatSocket sock, ChatPacket packet) {
        if (packet.getType() == PacketType.MANIFEST) {
            ManifestMessage mfst = (ManifestMessage) packet.getPayload();
            // Push those that are local
            for (int seq : sock.getPersistenceManager().getHeard(mfst.getSrc())) {
                if (!mfst.getSeqs().contains(seq)) {
                    try {
                        PushMessage msg = new PushMessage(mfst.getSrc(), sock.getPersistenceManager().getPacket(
                                mfst.getSrc(), seq));
                        sock.sendPacket(sock.wrapPayload(msg));
                    } catch (IOException e) {
                        Logging.getLogger().warning("Unable to send PushMessage");
                    }
                }
            }

            // Request those that are remote
            for (int seq : mfst.getSeqs()) {
                Set<Integer> heard = sock.getPersistenceManager().getHeard(mfst.getSrc());
                if (!heard.contains(seq)) {
                    try {
                        sock.sendPacket(sock.wrapPayload(new ManifestMessage(mfst.getSrc(), heard)));
                        return;
                    } catch (IOException e) {
                        Logging.getLogger().warning("Unable to send ManifestMessage");
                    }
                }
            }
        } else if (packet.getType() == PacketType.PURGE) {
            // TODO
        }
    }
}
