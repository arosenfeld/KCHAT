package core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.ManifestMessage;

import util.Logging;
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

    public ChatPacket getPacket(LongInteger src, int persistenceId) {
        return store.getPacket(src, persistenceId);
    }

    @Override
    public void run() {
        while (true) {
            try {
                for (LongInteger src : store.getSrcs()) {
                    ManifestMessage manifest = new ManifestMessage(src, store.getMissing(src));
                    sock.sendPacket(sock.wrapPayload(manifest));
                }
                Thread.sleep(sock.getGRTT());
            } catch (InterruptedException e) {
                Logging.getLogger().warning("Sleep was interrupted");
            } catch (IOException e) {
                Logging.getLogger().warning("Unable to send ManifestMessage");
            }
        }
    }

    private class MessageStore {
        private Map<LongInteger, TreeMap<Integer, ChatPacket>> messages;

        public MessageStore() {
            messages = Collections.synchronizedMap(new HashMap<LongInteger, TreeMap<Integer, ChatPacket>>());
        }

        public synchronized void addPacket(ChatPacket p) {
            if (p.getType() == PacketType.CHAT_MESSAGE) {
                if (!messages.containsKey(p.getSrc())) {
                    messages.put(p.getSrc(), new TreeMap<Integer, ChatPacket>());
                }
                messages.get(p.getSrc()).put(((ChatMessage) p.getPayload()).getPersistenceId(), p);
            }
        }

        public synchronized void removePacket(ChatPacket p) {
            if (messages.containsKey(p.getSrc())) {
                messages.get(p.getSrc()).remove(p);
            }
        }

        public synchronized ChatPacket getPacket(LongInteger src, int persistenceId) {
            if (messages.containsKey(src) && messages.get(src).containsKey(persistenceId)) {
                return messages.get(src).get(persistenceId);
            }
            return null;
        }

        public synchronized LongInteger[] getSrcs() {
            return messages.keySet().toArray(new LongInteger[messages.keySet().size()]);
        }

        public synchronized Range[] getMissing(LongInteger src) {
            if (!messages.containsKey(src)) {
                return new Range[0];
            }
            List<Range> ranges = new LinkedList<Range>();
            int last = 0;
            Integer[] seqs = messages.get(src).keySet().toArray(new Integer[messages.get(src).size()]);
            for (int i = 0; i < seqs.length; i++) {
                if (last + 1 != seqs[i]) {
                    ranges.add(new Range(last + 1, (short) (seqs[i] - (last + 1))));
                }
                last = seqs[i];
            }
            return ranges.toArray(new Range[ranges.size()]);
        }
    }

    public static class Range {
        public int start;
        public short size;

        public Range(int start, short size) {
            this.start = start;
            this.size = size;
        }
    }
}