package operations.commands;

import java.io.IOException;

import packets.messages.ChatMessage;
import packets.messages.ChatMessage.MessageField;
import util.LongInteger;
import core.ChatSocket;

public class RoomMessageCommand extends Command {
    private LongInteger dest;
    private byte[] message;

    public RoomMessageCommand(LongInteger dest, byte[] message) {
        this.dest = dest;
        this.message = message;
    }

    @Override
    public void invoke(ChatSocket socket) throws InvalidCommandException {
        try {
            ChatMessage msg = new ChatMessage(socket.getNextMessageId(), socket.getNextPersistId(), dest, message);
            msg.setParam(MessageField.TO_ROOM, true);
            msg.setParam(MessageField.PERSIST, true);
            socket.sendPacket(socket.wrapPayload(msg));
        } catch (IOException e) {
            throw new InvalidCommandException("Unable to send ChatMessage to room");
        }
    }
}
