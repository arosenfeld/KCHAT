package operations.handlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import operations.commands.InvalidCommandException;
import operations.commands.PurgeCommand;

import core.ChatSocket;
import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.ChatMessage.MessageField;
import util.Logging;
import util.LongInteger;

public class ChatMessageHandler extends Handler {
    private Map<LongInteger, Set<Integer>> passedToClient;

    public ChatMessageHandler() {
        passedToClient = Collections.synchronizedMap(new HashMap<LongInteger, Set<Integer>>());
    }

    @Override
    public boolean accepts(ChatPacket packet) { // TODO: Also handle push?
        return packet.getType() == PacketType.CHAT_MESSAGE;
    }

    @Override
    public void process(ChatSocket sock, ChatPacket packet) {
        ChatMessage msg = (ChatMessage) packet.getPayload();
        if (msg.getParam(MessageField.TO_ROOM) || msg.getDest().equals(sock.getUUID())) {
            if (msg.getDest().equals(sock.getUUID())) {
                try {
                    sock.executeCommand(new PurgeCommand(msg.getMessageId()));
                } catch (InvalidCommandException e) {
                    Logging.getLogger().warning("Unable to execute Purge after receiving message.");
                }
            } else {
                if (!sock.getPresenceManager().isPresent(msg.getDest(), sock.getUUID())) {
                    return;
                }
            }

            if (!alreadyPassedToClient(packet)) {
                sock.getClientCallback().receivePacket(packet);
            }
        }
    }

    private boolean alreadyPassedToClient(ChatPacket packet) {
        int msgId = ((ChatMessage) packet.getPayload()).getMessageId();
        if (!passedToClient.containsKey(packet.getSrc())) {
            passedToClient.put(packet.getSrc(), new HashSet<Integer>());
        }

        if (!passedToClient.get(packet.getSrc()).contains(msgId)) {
            passedToClient.get(packet.getSrc()).add(msgId);
            return false;
        }
        return true;
    }
}
