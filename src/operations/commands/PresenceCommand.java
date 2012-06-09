package operations.commands;

import java.io.IOException;

import packets.ChatPacket;
import packets.messages.UserPresenceMessage;
import packets.messages.UserPresenceMessage.PresenceStatus;
import util.Logging;
import util.LongInteger;
import core.ChatSocket;

/**
 * A command to join or leave a chat room.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public class PresenceCommand extends Command {
    private LongInteger roomName;
    private boolean present;

    public PresenceCommand(LongInteger roomName, boolean present) {
        this.roomName = roomName;
        this.present = present;
    }

    @Override
    public void invoke(ChatSocket socket) throws InvalidCommandException {
        // Sanity check the command
        if (present && socket.getPresenceManager().isPresent(roomName, socket.getUUID())) {
            throw new InvalidCommandException("Could not join " + roomName + ": Already present.");
        }
        if (!present && !socket.getPresenceManager().isPresent(roomName, socket.getUUID())) {
            throw new InvalidCommandException("Could not leave " + roomName + ": Not present.");
        }

        // Setup the UserPresenceMessage
        socket.getPresenceManager().setPresence(roomName, socket.getUUID(), present);
        PresenceStatus status = present ? PresenceStatus.JOIN : PresenceStatus.LEAVE;
        try {
            socket.sendPacket(socket.wrapPayload(new UserPresenceMessage(roomName, status)));

            // If the client is joining a room, bring the them up to date by
            // pushing previous messages
            if (status == PresenceStatus.JOIN) {
                Logging.getLogger().info(socket.getPersistenceManager().toString());
                for (ChatPacket p : socket.getPersistenceManager().getRoomMessages(roomName)) {
                    Logging.getLogger().info("Pushing " + p.getSequence());
                    socket.pushToClient(p, true);
                }
            }
        } catch (IOException e) {
            throw new InvalidCommandException("Unable to send presence packet.");
        }
    }
}
