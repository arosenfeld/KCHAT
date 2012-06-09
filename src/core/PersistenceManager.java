package core;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

        public Range[] getMissing(LongInteger src) {
            if (!messages.containsKey(src)) {
                return null;
            }
            List<Range> ranges = new LinkedList<Range>();
            int last = 0;
            Integer[] seqs = messages.get(src).keySet().toArray(new Integer[messages.get(src).size()]);
            for (int i = 0; i < seqs.length; i++) {
                if (last + 1 != seqs[i]) {
                    ranges.add(new Range(last + 1, seqs[i] - (last + 1)));
                }
                last = seqs[i];
            }
            
            return ranges.toArray(new Range[ranges.size()]);
        }
    }

    private class Range {
        public int start;
        public int size;

        public Range(int start, int size) {
            this.start = start;
            this.size = size;
        }
    }
}