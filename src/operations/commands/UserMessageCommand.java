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
        sentMessageId = socket.getNextSeq();

        // if (socket.getSecurityManager().UserHasPublicKey(dest)) {
        // try {
        // message = socket.getSecurityManager().encrypt(dest, message);
        // } catch (Exception e1) {
        // throw new InvalidCommandException("Unable to encrypt message.");
        // }
        // } else {
        // throw new InvalidCommandException("Unable to send to " + dest +
        // ".  No know public key.");
        // }

        synchronized (socket) {
            try {
                final ChatMessage msg = new ChatMessage(socket.getNextMessageId(), dest, message);
                // Send the message
                socket.sendPacket(socket.wrapPayload(msg));

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
            } catch (IOException e) {
                Logging.getLogger().warning("Unable to send user message");
            }
        }
    }

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
            socket.sendPacket(socket.wrapPayload(msg));
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
