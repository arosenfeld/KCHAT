package packets;

import packets.ChatPacket.PacketType;
import util.Logging;
import core.ChatSocket;
import net.PacketCallback;

public class IncomingPacketHandler implements PacketCallback {
    private ChatSocket socket;
    private ChatPacket last;

    public IncomingPacketHandler(ChatSocket socket) {
        this.socket = socket;
    }

    public synchronized ChatPacket waitFor(int timeout, PacketType... types) {
        try {
            do {
                wait(timeout);
            } while (correctType(last.getType(), types));
            return last;
        } catch (InterruptedException e) {
            Logging.getLogger().warning("Wait was interrupted.");
        }
        return null;
    }

    @Override
    public synchronized void processPacket(byte[] data) {
    }

    private boolean correctType(PacketType received, PacketType[] waiting) {
        for (PacketType t : waiting) {
            if (t == received) {
                return true;
            }
        }
        return false;
    }
}
