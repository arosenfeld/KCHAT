package operations.handlers;

import java.io.IOException;
import java.util.Random;
import java.util.Set;

import operations.PacketMatcher;
import operations.PacketMatcher.TypeMatcher;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.RoomComparisonMessage;
import packets.messages.RoomStatusMessage;
import packets.messages.UserPresenceMessage;
import packets.messages.UserPresenceMessage.PresenceStatus;
import packets.messages.UserStatusMessage.StatusType;
import packets.messages.UserStatusMessage;
import util.Configuration;
import util.Logging;
import util.LongInteger;
import core.ChatSocket;

public class PresenceHandler extends Handler {
    private PacketMatcher pm;
    private Random rand;

    public PresenceHandler() {
        this.pm = new TypeMatcher(PacketType.USER_PRESENCE, PacketType.ROOM_COMPARISON, PacketType.ROOM_STATUS,
                PacketType.USER_STATUS);
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
        case USER_STATUS:
            handleUserStatus(sock, packet);
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

        // Check if the hash in the message is equal to the local version
        if (!sock.getPresenceManager().hashMembers(rcm.getRoomName()).equals(rcm.getMembersHash())) {
            // Randomly pick wait period in [0, RMQI)
            int wait = rand.nextInt(1000 * Configuration.getInstance().getValueAsInt("timer.rmqi"));
            // Wait to see if another instance broadcasts a ROOM_STATUS
            // indicating a discrepancy
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
                // No other broadcast heard, send our own ROOM_STATUS message
                Set<LongInteger> members = sock.getPresenceManager().membersOf(rcm.getRoomName());
                try {
                    sock.sendPacket(sock.wrapPayload(new RoomStatusMessage(rcm.getRoomName(), members)));
                } catch (IOException e) {
                    Logging.getLogger().warning("Unable to send RoomStatusMessage");
                }
            }
        }
    }

    private void handleRoomStatus(ChatSocket sock, ChatPacket packet) {
        RoomStatusMessage rsm = (RoomStatusMessage) packet.getPayload();
        for (LongInteger m : sock.getPresenceManager().membersOf(rsm.getRoomName())) {
            if (!rsm.getMembers().contains(m)) {
                try {
                    if (m.equals(sock.getUUID())) {
                        StatusType pres = sock.getPresenceManager().isPresent(rsm.getRoomName(), m) ? StatusType.PRESENT
                                : StatusType.NOT_PRESENT;
                        sock.sendPacket(sock.wrapPayload(new UserStatusMessage(pres, rsm.getRoomName(), m)));
                    } else {
                        sock
                                .sendPacket(sock.wrapPayload(new UserStatusMessage(StatusType.QUERY, rsm.getRoomName(),
                                        m)));
                    }
                } catch (IOException e) {
                    Logging.getLogger().warning("Unable to send UserStatus");
                }
            }
        }
    }

    private void handleUserStatus(ChatSocket sock, ChatPacket packet) {
        UserStatusMessage status = (UserStatusMessage) packet.getPayload();
        if (status.getStatusType() == StatusType.QUERY && status.getUser().equals(sock.getUUID())) {
            StatusType pres = sock.getPresenceManager().isPresent(status.getRoom(), status.getUser()) ? StatusType.PRESENT
                    : StatusType.NOT_PRESENT;
            try {
                sock.sendPacket(sock.wrapPayload(new UserStatusMessage(pres, status.getRoom(), status.getUser())));
            } catch (IOException e) {
                Logging.getLogger().warning("Unable to send UserStatusMessage");
            }
        } else if (status.getStatusType() == StatusType.PRESENT) {
            sock.getPresenceManager().setPresence(status.getRoom(), status.getUser(), true);
        } else if (status.getStatusType() == StatusType.NOT_PRESENT) {
            sock.getPresenceManager().setPresence(status.getRoom(), status.getUser(), false);
        }
    }
}
