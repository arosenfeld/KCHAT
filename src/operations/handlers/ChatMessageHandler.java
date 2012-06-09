package operations.handlers;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import core.ChatSocket;
import operations.PacketMatcher.TypeMatcher;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.PushMessage;
import packets.messages.ChatMessage.MessageField;
import util.Logging;
import util.LongInteger;

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

    public void processChatMessage(ChatSocket socket, ChatPacket packet) {
        ChatMessage msg = (ChatMessage) packet.getPayload();
        if (msg.getDest().equals(new LongInteger())) {
            try {
                socket.getSecurityManager().saveUserPublicKey(packet.getSrc(), msg.getMessage());
                return;
            } catch (Exception e) {
                Logging.getLogger().warning("Unable to save Public Key");
            }
        } else {
            if (msg.getParam(MessageField.PERSIST)) {
                socket.getPersistenceManager().persistPacket(packet);
            }
            if (shouldPass(socket, msg)) {
                if (!msg.getParam(MessageField.TO_ROOM)) {
                    try {
                        msg.setMessage(socket.getSecurityManager().decrypt(msg.getMessage()));
                    } catch (Exception e) {
                        Logging.getLogger().warning("Unable to decrypt message");
                        return;
                    }
                }
                socket.pushToClient(packet);
            }
        }
    }

    public void processPushMessage(ChatSocket socket, ChatPacket packet) {
        PushMessage pushed = (PushMessage) packet.getPayload();
        ChatMessage original = (ChatMessage) pushed.getPacket().getPayload();
        socket.getPersistenceManager().persistPacket(packet);
        if (original.getDest().equals(new LongInteger())) {
            try {
                socket.getSecurityManager().saveUserPublicKey(packet.getSrc(), original.getMessage());
                return;
            } catch (Exception e) {
                Logging.getLogger().warning("Unable to save Public Key");
            }
        }

        if (shouldPass(socket, original)) {

            if (!original.getParam(MessageField.TO_ROOM)) {
                try {
                    original.setMessage(socket.getSecurityManager().decrypt(original.getMessage()));
                } catch (Exception e1) {
                    Logging.getLogger().warning("Unable to decrypt message");
                    return;
                }
            }

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
