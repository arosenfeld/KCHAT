package operations.commands;

import java.io.IOException;

import packets.messages.PurgeMessage;
import core.ChatSocket;

/**
 * A command the remove a packet from persistent storage.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public class PurgeCommand extends Command {
    private int messageId;

    public PurgeCommand(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public void invoke(ChatSocket socket) throws InvalidCommandException {
        try {
            socket.sendPacket(socket.wrapPayload(new PurgeMessage(messageId)));
        } catch (IOException e) {
            throw new InvalidCommandException("Could not send Purge packet");
        }
    }
}
