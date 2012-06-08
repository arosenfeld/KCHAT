package core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;

import util.LongInteger;

public class PersistenceManager extends Thread {
    private MessageStore store;
    private ChatSocket sock;

    public PersistenceManager(ChatSocket sock) {
        store = new MessageStore();
        this.sock = sock;
    }

    public void persistPacket(ChatPacket packet) {
        if (packet.getType() == PacketType.CHAT_MESSAGE) {
            store.addPacket(packet);
        }
    }
    
    public void purgePacket(ChatPacket packet) {
        store.removePacket(packet);
    }
    
    @Override
    public void run() {
        
    }

    private class MessageStore {
        private Map<LongInteger, TreeMap<Integer, ChatPacket>> messages;

        public MessageStore() {
            messages = Collections.synchronizedMap(new HashMap<LongInteger, TreeMap<Integer, ChatPacket>>());
        }

        public void addPacket(ChatPacket p) {
            if (p.getType() == PacketType.CHAT_MESSAGE) {
                if (!messages.containsKey(p.getSrc())) {
                    messages.put(p.getSrc(), (TreeMap<Integer, ChatPacket>) Collections
                            .synchronizedMap(new TreeMap<Integer, ChatPacket>()));
                }
                messages.get(p.getSrc()).put(((ChatMessage) p.getPayload()).getPersistenceId(), p);
            }
        }

        public void removePacket(ChatPacket p) {
            if (messages.containsKey(p.getSrc())) {
                messages.get(p.getSrc()).remove(p);
            }
        }
    }
}