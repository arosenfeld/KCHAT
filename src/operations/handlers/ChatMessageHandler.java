package operations.handlers;

import core.ChatSocket;
import operations.PacketMatcher.TypeMatcher;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.PushMessage;
import packets.messages.ChatMessage.MessageField;
import util.Logging;
import util.LongInteger;

/**
 * Handles incoming chat messages. This includes PUSH messages which reconcile
 * missed messages.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public class ChatMessageHandler extends Handler {

    @Override
    public boolean accepts(ChatPacket packet) {
        return new TypeMatcher(PacketType.CHAT_MESSAGE, PacketType.PUSH).matches(packet);
    }

    @Override
    public void process(ChatSocket sock, ChatPacket packet) {
        switch (packet.getType()) {
        case CHAT_MESSAGE:
            processChatMessage(sock, packet);
            break;
        case PUSH:
            processPushMessage(sock, packet);
            break;
        }
    }

    /**
     * Process a CHAT_MESSAGE packet.
     * 
     * @param socket
     *            KCHAT socket.
     * @param packet
     *            The CHAT_MESSAGE packet.
     */
    public void processChatMessage(ChatSocket socket, ChatPacket packet) {
        ChatMessage msg = (ChatMessage) packet.getPayload();
        // If the message is bound for address zero, it is a public key. Save
        // it.
        if (msg.getDest().equals(new LongInteger())) {
            try {
                socket.getSecurityManager().saveUserPublicKey(packet.getSrc(), msg.getMessage());
                return;
            } catch (Exception e) {
                Logging.getLogger().warning("Unable to save Public Key");
            }
        } else {
            // Store the message if it is persistent
            if (msg.getParam(MessageField.PERSIST)) {
                socket.getPersistenceManager().persistPacket(packet);
            }
            // If the application has not yet received the packet, send it up
            // the stack.
            if (shouldPass(socket, msg)) {
                if (!msg.getParam(MessageField.TO_ROOM)) {
                    // The message is a user-to-user message. Decrypt it.
                    try {
                        msg.setMessage(socket.getSecurityManager().decrypt(msg.getMessage()));
                    } catch (Exception e) {
                        Logging.getLogger().warning("Unable to decrypt message");
                        return;
                    }
                }
                // Push the message to the application.
                socket.pushToClient(packet);
            }
        }
    }

    /**
     * Processes a PUSH message reconciling a missed message.
     * 
     * @param socket
     *            KCHAT socket.
     * @param packet
     *            The PUSH packet.
     */
    public void processPushMessage(ChatSocket socket, ChatPacket packet) {
        // The PUSH message
        PushMessage pushed = (PushMessage) packet.getPayload();
        // The CHAT_MESSAGE that was originally sent, and is now being
        // reconciled
        ChatMessage original = (ChatMessage) pushed.getPacket().getPayload();
        // Persist it
        socket.getPersistenceManager().persistPacket(packet);
        // If it's bound for address zero, it's a public key
        if (original.getDest().equals(new LongInteger())) {
            try {
                socket.getSecurityManager().saveUserPublicKey(packet.getSrc(), original.getMessage());
                return;
            } catch (Exception e) {
                Logging.getLogger().warning("Unable to save Public Key");
            }
        }

        // If the application has not yet received the packet, send it up the
        // stack.
        if (shouldPass(socket, original)) {
            if (!original.getParam(MessageField.TO_ROOM)) {
                // The message is a user-to-user message. Decrypt it.
                try {
                    original.setMessage(socket.getSecurityManager().decrypt(original.getMessage()));
                } catch (Exception e1) {
                    Logging.getLogger().warning("Unable to decrypt message");
                    return;
                }
            }
            // Push the message to the application.
            socket.pushToClient(pushed.getPacket());
        }
    }

    private boolean shouldPass(ChatSocket socket, ChatMessage msg) {
        if (msg.getParam(MessageField.TO_ROOM)
                && socket.getPresenceManager().isPresent(msg.getDest(), socket.getUUID())) {
            return true;
        } else if (!msg.getParam(MessageField.TO_ROOM) && msg.getDest().equals(socket.getUUID())) {
            return true;
        }
        return false;
    }
}
