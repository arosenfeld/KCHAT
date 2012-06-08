package operations.commands;

import java.io.IOException;

import operations.PacketMatcher;

import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.PurgeMessage;
import packets.messages.ChatMessage.MessageField;
import security.security;
import util.Logging;
import util.LongInteger;
import core.ChatSocket;

public class UserMessageCommand extends Command {
    private LongInteger dest;
    private byte[] message;
    private boolean persist;
    private int sentMessageId;

    public UserMessageCommand(LongInteger dest, byte[] message, boolean persist) {
        this.dest = dest;
        this.message = message;
        this.persist = persist;
    }

    @Override
    public void invoke(ChatSocket socket) throws InvalidCommandException {
        sentMessageId = socket.getNextSeq();
        ChatPacket received;

//        if (socket.getSecurityManager().UserHasPublicKey(dest)) {
//            try {
//                message = socket.getSecurityManager().encrypt(dest, message);
//            } catch (Exception e1) {
//                throw new InvalidCommandException("Unable to encrypt message.");
//            }
//        } else {
//            throw new InvalidCommandException("Unable to send to " + dest + ".  No know public key.");
//        }
        
        synchronized (socket) {
            try {
                ChatMessage msg = new ChatMessage(socket.getNextMessageId(), dest, message);
                // Send the message
                socket.sendPacket(socket.wrapPayload(msg));
                Logging.getLogger().info("Sent message");

                // Wait for a purge message
                // TODO: Use GRTT in this wait
                received = socket.waitFor(1000, new PurgeMatcher());

                // If no purge received and message is persistent, resend as
                // persistent
                if (persist && received == null) {
                    Logging.getLogger().info("Persist set and no response received.");
                    msg.setParam(MessageField.PERSIST, true);
                    msg.setPersistenceId(socket.getNextPersistId());
                    socket.sendPacket(socket.wrapPayload(msg));
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
