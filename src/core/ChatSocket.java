package core;

import operations.PacketMatcher;
import operations.commands.Command;
import operations.commands.InvalidCommandException;
import operations.handlers.Handler;
import operations.handlers.ChatMessageHandler;
import operations.handlers.PersistenceHandler;
import operations.handlers.PresenceHandler;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import packets.ChatPacket;
import packets.ChatPayload;
import packets.messages.ChatMessage;
import security.Security;
import transport.PacketCallback;
import transport.TransportProtocol;
import util.Configuration;
import util.Logging;
import util.LongInteger;

public class ChatSocket implements PacketCallback {
    private TransportProtocol protocol;
    private PersistenceManager persistenceManager;
    private PresenceManager presenceManager;
    private Security securityManager;

    private ChatPacketCallback clientCallback;
    private List<Handler> handlers;

    private LongInteger uuid;
    private byte version;

    private volatile int nextSeqId;
    private volatile int nextMessageId;
    private volatile int nextPersistId;
    private volatile int grtt;

    private ChatPacket last;
    private DuplicateFilter duplicates;
    private Map<LongInteger, Set<Integer>> passedToClient;

    public ChatSocket(TransportProtocol protocol, ChatPacketCallback callback, LongInteger uuid, byte version) {
        this.uuid = uuid;
        this.version = version;
        this.nextSeqId = 0;
        this.nextPersistId = 0;
        this.grtt = 1000 * Configuration.getInstance().getValueAsInt("timer.grtt_init");

        this.presenceManager = new PresenceManager(this);
        this.persistenceManager = new PersistenceManager(this);

        try {
            this.securityManager = new Security();
        } catch (Exception e) {
            Logging.getLogger().warning("Unable to build security object");
        }

        this.protocol = protocol;
        this.protocol.setCallback(this);

        this.clientCallback = callback;
        this.duplicates = new DuplicateFilter(500);
        this.handlers = new LinkedList<Handler>();
        passedToClient = Collections.synchronizedMap(new HashMap<LongInteger, Set<Integer>>());
    }

    public ChatSocket(TransportProtocol protocol, ChatPacketCallback callback, LongInteger uuid) {
        this(protocol, callback, uuid, (byte) 1);
    }

    public ChatSocket(TransportProtocol protocol, ChatPacketCallback callback) {
        this(protocol, callback, new LongInteger(UUID.randomUUID()), (byte) 1);
    }

    public void start() {
        this.handlers.add(new ChatMessageHandler());
        this.handlers.add(new PresenceHandler());
        this.handlers.add(new PersistenceHandler());

        this.presenceManager.start();
        this.persistenceManager.start();

        this.protocol.start();

        // Public key exchange
        ChatPacket packet = wrapPayload(new ChatMessage(getNextMessageId(), getNextPersistId(), new LongInteger(),
                securityManager.getMyPublicKey()));
        try {
            sendPacket(packet);
            getPersistenceManager().persistPacket(packet);
        } catch (IOException e) {
            Logging.getLogger().warning("Unable to send Public Key");
        }
    }

    public void stop() {
        this.protocol.close();
    }

    public PresenceManager getPresenceManager() {
        return presenceManager;
    }

    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public Security getSecurityManager() {
        return securityManager;
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

    public int getGRTT() {
        return grtt;
    }

    public void addSampledGRTT(int time) {
        grtt = Math.min((int) (.8 * grtt + .2 * time), Configuration.getInstance().getValueAsInt("timer.grtt_max"));
    }

    public void doubleGRTT() {
        grtt *= 2;
    }

    public void pushToClient(ChatPacket packet, boolean forcePush) {
        if (!passedToClient.containsKey(packet.getSrc())) {
            passedToClient.put(packet.getSrc(), new HashSet<Integer>());
        }

        int msgId = ((ChatMessage) packet.getPayload()).getMessageId();
        if (!passedToClient.get(packet.getSrc()).contains(msgId) || forcePush) {
            passedToClient.get(packet.getSrc()).add(msgId);
            clientCallback.receivePacket(packet);
        }
    }

    public void pushToClient(ChatPacket packet) {
        pushToClient(packet, false);
    }

    public ChatPacket wrapPayload(ChatPayload pld) {
        return new ChatPacket(version, getNextSeq(), uuid, pld);
    }

    public void executeCommand(final Command cmd) throws InvalidCommandException {
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
                wait(waitTime);
            } while (last == null || !matcher.matches(last));
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

            if (packet.getPayload() != null && packet.getPayload().getType() == packet.getType()) {
                duplicates.add(packet);
                last = packet;
                notifyAll();

                for (Handler h : handlers) {
                    if (h.accepts(packet)) {
                        h.process(this, packet);
                    }
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
