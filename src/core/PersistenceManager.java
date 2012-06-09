package core;

import java.io.IOException;
import java.util.ArrayList;
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
import packets.messages.ChatMessage.MessageField;

import util.Logging;
import util.LongInteger;

/**
 * Maintains messages persistently, and handles periodic manifest transmissions.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
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

    /**
     * Gets the packets from a specific room.
     * 
     * @param room
     *            The room name.
     * @return Packets bound for the room
     */
    public ChatPacket[] getRoomMessages(LongInteger room) {
        // TODO: Fix this
        if (!store.messages.containsKey(room)) {
            return new ChatPacket[0];
        }

        ArrayList<Integer> sorted = new ArrayList<Integer>();
        for (LongInteger src : store.messages.keySet()) {
            for (ChatPacket p : store.messages.get(src).values()) {
                if (p.getType() == PacketType.CHAT_MESSAGE) {
                    ChatMessage msg = (ChatMessage) p.getPayload();
                    if (msg.getParam(MessageField.TO_ROOM) && msg.getDest().equals(room)) {
                        sorted.add(msg.getPersistenceId());
                    }
                }
            }
        }

        Collections.sort(sorted);
        ChatPacket[] ret = new ChatPacket[sorted.size()];
        for (int i = 0; i < sorted.size(); i++) {
            ret[i] = store.messages.get(room).get(sorted.get(i));
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LongInteger room : store.messages.keySet()) {
            sb.append("Name: " + room + "\n");
            for (Integer s : store.messages.get(room).keySet()) {
                sb.append("      " + s + "\n");
            }
        }
        return sb.toString();
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