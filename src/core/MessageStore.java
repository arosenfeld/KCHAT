package core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import packets.ChatPacket;
import packets.ChatMessage;
import packets.ChatPacket.PacketType;

import util.LongInteger;

public class MessageStore {
    private Map<LongInteger, TreeMap<Integer, ChatPacket>> messages;

    public MessageStore() {
        messages = Collections.synchronizedMap(new HashMap<LongInteger, TreeMap<Integer, ChatPacket>>());
    }

    public void addMessage(ChatPacket m) {
        if (m.getType() == PacketType.CHAT_MESSAGE) {
            if (!messages.containsKey(m.getSrc())) {
                messages.put(m.getSrc(), (TreeMap<Integer, ChatPacket>) Collections
                        .synchronizedMap(new TreeMap<Integer, ChatPacket>()));
            }
            messages.get(m.getSrc()).put(((ChatMessage) m.getPayload()).getPersistenceId(), m);
        }
    }
}
