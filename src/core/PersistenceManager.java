package core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import packets.messages.ManifestMessage;
import packets.messages.PushMessage;

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
        store.addPacket(packet);
    }

    public void purgePacket(ChatPacket packet) {
        store.removePacket(packet);
    }

    public ChatPacket getPacket(LongInteger src, int persistenceId) {
        return store.getPacket(src, persistenceId);
    }

    public Set<Integer> getHeard(LongInteger src) {
        return store.getHeard(src);
    }

    @Override
    public void run() {
        while (true) {
            try {
                for (LongInteger src : store.getSrcs()) {
                    ManifestMessage manifest = new ManifestMessage(src, store.getHeard(src));
                    ChatPacket packet = sock.wrapPayload(manifest);
                    sock.sendPacket(packet);
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
        private Map<LongInteger, HashMap<Integer, ChatPacket>> messages;

        public MessageStore() {
            messages = Collections.synchronizedMap(new HashMap<LongInteger, HashMap<Integer, ChatPacket>>());
        }

        public synchronized void addPacket(ChatPacket p) {
            if (!messages.containsKey(p.getSrc())) {
                messages.put(p.getSrc(), new HashMap<Integer, ChatPacket>());
            }

            if (p.getType() == PacketType.PUSH) {
                p = ((PushMessage) p.getPayload()).getPacket();
            }

            if (p.getType() == PacketType.CHAT_MESSAGE) {
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

        public Set<Integer> getHeard(LongInteger src) {
            if (!messages.containsKey(src)) {
                return new HashSet<Integer>();
            }

            return messages.get(src).keySet();
        }
    }
}