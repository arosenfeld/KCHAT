package operations.commands;

import java.io.IOException;
import java.util.Calendar;

import operations.PacketMatcher;

import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.PurgeMessage;
import packets.messages.ChatMessage.MessageField;
import util.Logging;
import util.LongInteger;
import core.ChatSocket;

/**
 * A command to send a message to a specific instance
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
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
    public void invoke(final ChatSocket socket) throws InvalidCommandException {
        // The user with address zero is reserved
        if (dest.equals(new LongInteger())) {
            throw new InvalidCommandException("Cannot send to user with address zero");
        }
        // Bump the seq number
        sentMessageId = socket.getNextSeq();

        // Assure the local instance has a public key for the destination user
        if (socket.getSecurityManager().userHasPublicKey(dest)) {
            try {
                message = socket.getSecurityManager().encrypt(dest, message);
            } catch (Exception e1) {
                throw new InvalidCommandException("Unable to encrypt message.");
            }
        } else {
            throw new InvalidCommandException("Unable to send to " + dest + ".  No known public key.");
        }

        synchronized (socket) {
            try {
                // Create the message
                final ChatMessage msg = new ChatMessage(socket.getNextMessageId(), dest, message);
                if (dest.equals(socket.getUUID())) {
                    // If the user is sending to themself, don't involve the
                    // network
                    socket.pushToClient(socket.wrapPayload(msg));
                } else {
                    // Send the message
                    socket.sendPacket(socket.wrapPayload(msg));

                    // If the message is persistent, we must wait for a PURGE or
                    // otherwise store the message for later delivery.
                    if (persist) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    checkPersistence(socket, msg);
                                } catch (IOException e) {
                                    Logging.getLogger().warning("Unable to maintain persistence");
                                }
                            }
                        }).start();
                    }
                }
            } catch (IOException e) {
                Logging.getLogger().warning("Unable to send user message");
            }
        }
    }

    /**
     * After sending a persistent message, waits for a PURGE message. If one
     * isn't received, mark the message as persistent, store it locally, and
     * resend it with the PERSIST flag set to true.
     * 
     * @param socket
     *            The KCHAT socket.
     * @param msg
     *            The message that was sent.
     * @throws IOException
     */
    private void checkPersistence(ChatSocket socket, ChatMessage msg) throws IOException {
        // Wait for a purge message
        long start = Calendar.getInstance().getTimeInMillis();
        ChatPacket received = socket.waitFor(socket.getGRTT(), new PurgeMatcher());
        if (received != null) {
            socket.addSampledGRTT((int) (1000 * (Calendar.getInstance().getTimeInMillis() - start)));
        } else {
            socket.doubleGRTT();
        }

        // If no purge received and message is persistent, resend as
        // persistent
        if (received == null) {
            Logging.getLogger().info("Persist set and no response received.");
            msg.setParam(MessageField.PERSIST, true);
            msg.setPersistenceId(socket.getNextPersistId());
            ChatPacket packet = socket.wrapPayload(msg);
            socket.getPersistenceManager().persistPacket(packet);
            socket.sendPacket(packet);
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
