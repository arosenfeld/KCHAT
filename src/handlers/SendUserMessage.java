package handlers;

import java.io.IOException;

import packets.ChatPacket;
import packets.ChatMessage;
import packets.ChatPacket.PacketType;
import packets.ChatMessage.MessageField;
import packets.PurgeMessage;
import util.Logging;
import util.LongInteger;
import core.ChatSocket;

public class SendUserMessage extends Handler {
    private LongInteger dest;
    private byte[] message;
    private boolean persist;
    private int sentMessageId;

    public SendUserMessage(LongInteger dest, byte[] message, boolean persist) {
        this.dest = dest;
        this.message = message;
        this.persist = persist;
    }

    @Override
    public void invoke(ChatSocket socket) throws InvalidCommandException {
        sentMessageId = socket.getNextId();
        ChatPacket received;
        synchronized (socket.getIncomingPacketHandler()) {
            try {
                // Send the message
                socket.sendPacket(socket.wrapPayload(new ChatMessage(dest, message)));
                Logging.getLogger().info("Sent message");

                // Wait for a purge message
                // TODO: Use GRTT in this wait
                received = socket.getIncomingPacketHandler().waitFor(1000, new PurgeMatcher());

                // If no purge received and message is persistent, resend as
                // persistent
                if (persist && received == null) {
                    Logging.getLogger().info("Persist set and no response received.");
                    ChatMessage resendPersistent = new ChatMessage(socket.getNextPersistId(), dest, message);
                    resendPersistent.setParam(MessageField.PERSIST, true);
                    socket.sendPacket(socket.wrapPayload(resendPersistent));
                }
            } catch (IOException e) {
                Logging.getLogger().warning("Unable to send user message");
            }
        }
    }

    private class PurgeMatcher implements PacketMatcher {
        @Override
        public boolean matches(ChatPacket packet) {
            if (packet.getType() == PacketType.PURGE && packet.getSrc().equals(dest)) {
                PurgeMessage p = (PurgeMessage) packet.getPayload();
                return p.getMessageId() == sentMessageId;
            }
            return false;
        }
    }
}
