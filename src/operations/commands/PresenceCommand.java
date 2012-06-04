package operations.commands;

import java.io.IOException;

import packets.UserPresenceMessage;
import packets.UserPresenceMessage.PresenceStatus;
import util.LongInteger;
import core.ChatSocket;

public class PresenceCommand extends Command {
    private LongInteger roomName;
    private boolean present;

    public PresenceCommand(LongInteger roomName, boolean present) {
        this.roomName = roomName;
        this.present = present;
    }

    @Override
    public void invoke(ChatSocket socket) throws InvalidCommandException {
        if (present && socket.getPresenceManager().isPresent(roomName, socket.getUUID())) {
            throw new InvalidCommandException("Could not join " + roomName + ": Already present.");
        }
        if (!present && !socket.getPresenceManager().isPresent(roomName, socket.getUUID())) {
            throw new InvalidCommandException("Could not leave " + roomName + ": Not present.");
        }

        socket.getPresenceManager().setPresence(roomName, socket.getUUID(), present);
        PresenceStatus status = present ? PresenceStatus.JOIN : PresenceStatus.LEAVE;
        try {
            socket.sendPacket(socket.wrapPayload(new UserPresenceMessage(roomName, status)));
        } catch (IOException e) {
            throw new InvalidCommandException("Unable to send presence packet.");
        }
    }
}
