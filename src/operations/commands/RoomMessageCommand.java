package operations.commands;

import java.io.IOException;

import packets.ChatPacket;
import packets.messages.ChatMessage;
import packets.messages.ChatMessage.MessageField;
import util.LongInteger;
import core.ChatSocket;

/**
 * A command to send a message to a specific room.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public class RoomMessageCommand extends Command {
    private LongInteger dest;
    private byte[] message;

    public RoomMessageCommand(LongInteger dest, byte[] message) {
        this.dest = dest;
        this.message = message;
    }

    @Override
    public void invoke(ChatSocket socket) throws InvalidCommandException {
        // Room with address zero is reserved
        if (dest.equals(new LongInteger())) {
            throw new InvalidCommandException("Cannot send to room with address zero");
        }

        try {
            // Setup the packet
            ChatMessage msg = new ChatMessage(socket.getNextMessageId(), socket.getNextPersistId(), dest, message);
            msg.setParam(MessageField.TO_ROOM, true);
            msg.setParam(MessageField.PERSIST, true);

            ChatPacket packet = socket.wrapPayload(msg);
            // Persist the packet
            socket.getPersistenceManager().persistPacket(packet);

            // Push the message to the local client
            if (socket.getPresenceManager().isPresent(socket.getUUID(), dest)) {
                socket.pushToClient(packet);
            }

            // Send the packet
            socket.sendPacket(packet);
        } catch (IOException e) {
            throw new InvalidCommandException("Unable to send ChatMessage to room");
        }
    }
}
