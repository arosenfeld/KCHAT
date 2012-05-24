package core;

import java.util.Calendar;
import java.util.LinkedHashSet;

import handlers.PacketMatcher;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import transport.PacketCallback;
import util.Logging;

public class IncomingPacketHandler implements PacketCallback {
    private ChatSocket socket;
    private ChatPacket last;
    private DuplicateFilter duplicates;

    public IncomingPacketHandler(ChatSocket socket) {
        this.socket = socket;
        this.duplicates = new DuplicateFilter(500); // TODO: Configurable size
    }

    public synchronized ChatPacket waitFor(int timeout, PacketMatcher matcher) {
        try {
            long endTime = Calendar.getInstance().getTimeInMillis() + timeout;
            long waitTime;
            do {
                if ((waitTime = endTime - Calendar.getInstance().getTimeInMillis()) <= 0) {
                    return null;
                }
                Logging.getLogger().info("waiting " + (endTime - Calendar.getInstance().getTimeInMillis()));
                wait(waitTime);
            } while (last == null || matcher.matches(last));
            return last;
        } catch (InterruptedException e) {
            Logging.getLogger().warning("Wait was interrupted.");
        }
        return null;
    }

    @Override
    public synchronized void processPacket(byte[] data) {
        ChatPacket packet = new ChatPacket(data);
        if (!duplicates.duplicate(packet)) {
            duplicates.add(packet);
            Logging.getLogger().info("Received packet of type " + packet.getType());
        }
    }

    private class DuplicateFilter {
        private LinkedHashSet<ChatPacket> heard;
        private int maxSize;

        public DuplicateFilter(int size) {
            this.maxSize = size;
            this.heard = new LinkedHashSet<ChatPacket>();
        }

        public void add(ChatPacket packet) {
            if (!heard.contains(packet)) {
                heard.add(packet);
                if (heard.size() >= maxSize) {
                    heard.remove(heard.iterator().next());
                }
            }
        }

        public boolean duplicate(ChatPacket packet) {
            return heard.contains(packet);
        }
    }
}
