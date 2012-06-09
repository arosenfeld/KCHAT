package operations.handlers;

import operations.commands.InvalidCommandException;
import operations.commands.PurgeCommand;

import core.ChatSocket;
import operations.PacketMatcher.TypeMatcher;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.PushMessage;
import packets.messages.ChatMessage.MessageField;
import util.Logging;

public class ChatMessageHandler extends Handler {

    @Override
    public boolean accepts(ChatPacket packet) {
        return new TypeMatcher(PacketType.CHAT_MESSAGE, PacketType.PUSH).matches(packet);
    }

    @Override
    public void process(ChatSocket sock, ChatPacket packet) {
        Logging.getLogger().info("Got " + packet.getType());
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
        if (msg.getParam(MessageField.TO_ROOM)) {
            if (msg.getDest().equals(socket.getUUID())) {
                try {
                    socket.executeCommand(new PurgeCommand(msg.getMessageId()));
                } catch (InvalidCommandException e) {
                    Logging.getLogger().warning("Unable to execute Purge after receiving message.");
                }
            } else {
                if (!socket.getPresenceManager().isPresent(msg.getDest(), socket.getUUID())) {
                    return;
                }
            }
            socket.pushToClient(packet);
        } else if (!msg.getParam(MessageField.TO_ROOM) && msg.getDest().equals(socket.getUUID())) {
            // Decrypt packet
            socket.pushToClient(packet);
        }
    }

    public void processPushMessage(ChatSocket socket, ChatPacket packet) {
        Logging.getLogger().info("Got PushMessage");
        PushMessage pushed = (PushMessage) packet.getPayload();
        socket.pushToClient(pushed.getPacket());
    }
}
