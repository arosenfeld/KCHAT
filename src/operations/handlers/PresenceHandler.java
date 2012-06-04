package operations.handlers;

import packets.ChatPacket;
import packets.UserPresenceMessage;
import packets.ChatPacket.PacketType;
import packets.UserPresenceMessage.PresenceStatus;
import util.Logging;
import core.ChatSocket;

public class PresenceHandler extends Handler {
    @Override
    public boolean accepts(ChatPacket packet) {
        // TODO: Other presence packets
        return packet.getType() == PacketType.USER_PRESENCE;
    }

    @Override
    public void process(ChatSocket sock, ChatPacket packet) {
        switch (packet.getType()) {
        case USER_PRESENCE:
            handleUserPresence(sock, packet);
            break;
        }
    }

    private void handleUserPresence(ChatSocket sock, ChatPacket packet) {
        UserPresenceMessage up = (UserPresenceMessage) packet.getPayload();
        sock.getPresenceManager().setPresence(up.getRoomName(), packet.getSrc(),
                up.getPresenceStatus() == PresenceStatus.JOIN);
    }
}
