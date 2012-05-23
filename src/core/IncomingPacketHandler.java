package core;

import java.util.Calendar;

import handlers.PacketMatcher;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import transport.PacketCallback;
import util.Logging;

public class IncomingPacketHandler implements PacketCallback {
    private ChatSocket socket;
    private ChatPacket last;

    public IncomingPacketHandler(ChatSocket socket) {
        this.socket = socket;
    }

    public synchronized ChatPacket waitFor(int timeout, PacketMatcher matcher) {
        try {
            long endTime = Calendar.getInstance().getTimeInMillis() + timeout;
            do {
                wait(endTime - Calendar.getInstance().getTimeInMillis());
            } while (matcher.matches(last));
            return last;
        } catch (InterruptedException e) {
            Logging.getLogger().warning("Wait was interrupted.");
        }
        return null;
    }

    @Override
    public synchronized void processPacket(byte[] data) {
    }
}
