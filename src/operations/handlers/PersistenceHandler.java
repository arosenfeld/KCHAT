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

/**
 * Handles all persistence related packets.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public class PersistenceHandler extends Handler {
    @Override
    public boolean accepts(ChatPacket packet) {
        return new TypeMatcher(PacketType.MANIFEST, PacketType.PURGE).matches(packet);
    }

    @Override
    public void process(ChatSocket sock, ChatPacket packet) {
        if (packet.getType() == PacketType.MANIFEST) {
            ManifestMessage mfst = (ManifestMessage) packet.getPayload();
            // For every persistence sequence number locally known...
            for (int seq : sock.getPersistenceManager().getHeard(mfst.getSrc())) {
                // If the remote instance hasn't received it....
                if (!mfst.getSeqs().contains(seq)) {
                    // Push the message out
                    try {
                        PushMessage msg = new PushMessage(mfst.getSrc(), sock.getPersistenceManager().getPacket(
                                mfst.getSrc(), seq));
                        sock.sendPacket(sock.wrapPayload(msg));
                    } catch (IOException e) {
                        Logging.getLogger().warning("Unable to send PushMessage");
                    }
                }
            }

            // For every persistence sequence number remotely known..
            for (int seq : mfst.getSeqs()) {
                Set<Integer> heard = sock.getPersistenceManager().getHeard(mfst.getSrc());
                // If the local instance hasn't received it...
                if (!heard.contains(seq)) {
                    // Push a manifest out
                    try {
                        sock.sendPacket(sock.wrapPayload(new ManifestMessage(mfst.getSrc(), heard)));
                        return;
                    } catch (IOException e) {
                        Logging.getLogger().warning("Unable to send ManifestMessage");
                    }
                }
            }
        }
    }
}
