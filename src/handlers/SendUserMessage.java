package handlers;

import java.io.IOException;

import packets.ChatPacket;
import packets.Message;
import packets.ChatPacket.PacketType;
import packets.Message.MessageFields;
import util.Logging;
import util.LongInteger;
import core.ChatSocket;

public class SendUserMessage extends Handler {
    private LongInteger dest;
    private byte[] message;
    private boolean persist;

    public SendUserMessage(LongInteger dest, byte[] message, boolean persist) {
        this.dest = dest;
        this.message = message;
        this.persist = persist;
    }

    @Override
    public void invoke(ChatSocket socket) throws InvalidCommandException {
        Message m = new Message(socket.getNextId(), persist ? socket.getNextPersistId() : 0, dest, message);
        ChatPacket outgoing = socket.wrapPayload(m);
        ChatPacket received;
        synchronized (socket.getIncomingPacketHandler()) {
            try {
                // Send the message
                socket.sendPacket(outgoing);
                // Wait for a purge message
                // TODO: Use GRTT in this wait
                received = socket.getIncomingPacketHandler().waitFor(1000, PacketType.PURGE);

                // If no purge received and message is persistent, resend as persistent
                if (persist && received == null) {
                    m.setParam(MessageFields.PERSIST, true);
                    socket.sendPacket(outgoing);
                }
            } catch (IOException e) {
                Logging.getLogger().warning("Unable to send user message");
            }
        }
    }
}
