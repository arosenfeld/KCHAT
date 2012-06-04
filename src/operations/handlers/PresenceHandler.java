package operations.handlers;

import java.io.IOException;
import java.util.Random;

import operations.PacketMatcher;
import operations.PacketMatcher.TypeMatcher;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.RoomComparisonMessage;
import packets.messages.RoomStatusMessage;
import packets.messages.UserPresenceMessage;
import packets.messages.UserPresenceMessage.PresenceStatus;
import util.Configuration;
import util.Logging;
import util.LongInteger;
import core.ChatSocket;

public class PresenceHandler extends Handler {
    private PacketMatcher pm;
    private Random rand;

    public PresenceHandler() {
        // TODO: Other presence packets
        this.pm = new TypeMatcher(PacketType.USER_PRESENCE, PacketType.ROOM_COMPARISON);
        this.rand = new Random();
    }

    @Override
    public boolean accepts(ChatPacket packet) {
        return pm.matches(packet);
    }

    @Override
    public void process(ChatSocket sock, ChatPacket packet) {
        switch (packet.getType()) {
        case USER_PRESENCE:
            handleUserPresence(sock, packet);
            break;
        case ROOM_COMPARISON:
            handleRoomComparison(sock, packet);
            break;
        case ROOM_STATUS:
            handleRoomStatus(sock, packet);
            break;
        }
    }

    private void handleUserPresence(ChatSocket sock, ChatPacket packet) {
        UserPresenceMessage up = (UserPresenceMessage) packet.getPayload();
        sock.getPresenceManager().setPresence(up.getRoomName(), packet.getSrc(),
                up.getPresenceStatus() == PresenceStatus.JOIN);
    }

    private void handleRoomComparison(ChatSocket sock, ChatPacket packet) {
        final RoomComparisonMessage rcm = (RoomComparisonMessage) packet.getPayload();
        if (!sock.getPresenceManager().hashMembers(rcm.getRoomName()).equals(rcm.getMembersHash())) {
            int wait = rand.nextInt(1000 * Configuration.getInstance().getValueAsInt("timer.rmqi"));
            ChatPacket recv = sock.waitFor(wait, new PacketMatcher() {

                @Override
                public boolean matches(ChatPacket packet) {
                    if (packet.getType() == PacketType.ROOM_STATUS) {
                        RoomStatusMessage rsm = (RoomStatusMessage) packet.getPayload();
                        return rsm.equals(rcm.getRoomName());
                    }
                    return false;
                }
            });

            if (recv == null) {
                LongInteger[] members = sock.getPresenceManager().membersOf(rcm.getRoomName());
                try {
                    sock.sendPacket(sock.wrapPayload(new RoomStatusMessage(rcm.getRoomName(), members)));
                } catch (IOException e) {
                    Logging.getLogger().warning("Unable to send RoomStatusMessage");
                }
            }
        }
    }

    private void handleRoomStatus(ChatSocket sock, ChatPacket packet) {
        // TODO: Handle this...
    }
}
