package core;

import operations.PacketMatcher;
import operations.commands.Command;
import operations.commands.InvalidCommandException;
import operations.handlers.Handler;
import operations.handlers.ChatMessageHandler;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import packets.ChatPacket;
import packets.ChatPayload;
import transport.PacketCallback;
import transport.TransportProtocol;
import util.Logging;
import util.LongInteger;

public class ChatSocket implements PacketCallback {
    private TransportProtocol protocol;
    private MessageStore messageStore;
    private ChatPacketCallback clientCallback;
    private boolean loopback;
    private List<Handler> handlers;

    private LongInteger uuid;
    private byte version;

    private volatile int nextSeqId;
    private volatile int nextMessageId;
    private volatile int nextPersistId;

    private ChatPacket last;
    private DuplicateFilter duplicates;

    public ChatSocket(TransportProtocol protocol, ChatPacketCallback callback, LongInteger uuid, byte version) {
        this.uuid = uuid;
        this.version = version;
        this.nextSeqId = 0;
        this.nextPersistId = 0;
        this.loopback = false;

        this.protocol = protocol;
        this.protocol.setCallback(this);

        this.messageStore = new MessageStore();

        this.clientCallback = callback;

        this.duplicates = new DuplicateFilter(500);

        this.handlers = new LinkedList<Handler>();
    }

    public ChatSocket(TransportProtocol protocol, ChatPacketCallback callback) {
        this(protocol, callback, new LongInteger(UUID.randomUUID()), (byte) 1);
    }

    public void start() {
        this.handlers.add(new ChatMessageHandler());
        this.protocol.start();
    }

    public void stop() {
        this.protocol.close();
    }

    public TransportProtocol getTransport() {
        return protocol;
    }

    public LongInteger getUUID() {
        return uuid;
    }

    public int getVersion() {
        return version;
    }

    public int getNextSeq() {
        return ++nextSeqId;
    }

    public int getNextMessageId() {
        return ++nextMessageId;
    }

    public int getNextPersistId() {
        return ++nextPersistId;
    }

    public ChatPacketCallback getClientCallback() {
        return clientCallback;
    }

    public ChatPacket wrapPayload(ChatPayload pld) {
        return new ChatPacket(version, getNextSeq(), uuid, pld);
    }

    public void executeCommand(Command cmd) throws InvalidCommandException {
        cmd.invoke(this);
    }

    public void sendPacket(ChatPacket packet) throws IOException {
        protocol.send(packet.pack());
    }

    public synchronized ChatPacket waitFor(int timeout, PacketMatcher matcher) {
        try {
            long endTime = Calendar.getInstance().getTimeInMillis() + timeout;
            long waitTime;
            do {
                if ((waitTime = endTime - Calendar.getInstance().getTimeInMillis()) <= 0) {
                    return null;
                }
                Logging.getLogger().info("waiting " + waitTime);
                wait(waitTime);
            } while (last == null || matcher.matches(last));
            return last;
        } catch (InterruptedException e) {
            Logging.getLogger().warning("Wait was interrupted.");
        }
        return null;
    }

    @Override
    public synchronized void processPacket(byte[] data) {
        ChatPacket packet = new ChatPacket(data);
        if (!duplicates.heard(packet) && !packet.getSrc().equals(uuid)) {
            duplicates.add(packet);
            last = packet;
            notifyAll();
            Logging.getLogger().info("Received packet of type " + packet.getType());

            for (Handler h : handlers) {
                if (h.accepts(packet)) {
                    h.process(this, packet);
                }
            }
        }
    }

    private class DuplicateFilter {
        private LinkedHashSet<ChatPacket> heard;
        private int maxSize;

        public DuplicateFilter(int size) {
            this.maxSize = size;
            this.heard = new LinkedHashSet<ChatPacket>();
        }

        public void add(ChatPacket packet) {
            if (!heard.contains(packet)) {
                heard.add(packet);
                if (heard.size() >= maxSize) {
                    heard.remove(heard.iterator().next());
                }
            }
        }

        public boolean heard(ChatPacket packet) {
            return heard.contains(packet);
        }
    }
}
