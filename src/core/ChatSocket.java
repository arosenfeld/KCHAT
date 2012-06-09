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
import packets.ChatPacket.PacketType;
import packets.messages.ChatMessage;
import security.Security;
import transport.PacketCallback;
import transport.TransportProtocol;
import util.Configuration;
import util.Logging;
import util.LongInteger;

/**
 * The primary KCHAT class, which provides socket functionality to upper-layer
 * applications.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
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

    /**
     * Creates a KCHAT socket.
     * 
     * @param protocol
     *            An underlying multicast transport protocol.
     * @param callback
     *            A callback which will be invoked when messages are received.
     * @param uuid
     *            A globally unique ID for this socket.
     * @param version
     *            The version of the protocol
     */
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

    /**
     * Starts the socket.
     */
    public void start() {
        // Start all the handlers of incoming packets
        this.handlers.add(new ChatMessageHandler());
        this.handlers.add(new PresenceHandler());
        this.handlers.add(new PersistenceHandler());

        // Start the managers
        this.presenceManager.start();
        this.persistenceManager.start();

        // Start the MC socket
        this.protocol.start();

        // Publish the local public key to room 0
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

    /**
     * Adds a sample of the group-round-trip-time.
     * 
     * @param time
     *            Time between send and receive in milliseconds.
     */
    public void addSampledGRTT(int time) {
        grtt = Math.min((int) (.8 * grtt + .2 * time), Configuration.getInstance().getValueAsInt("timer.grtt_max"));
    }

    /**
     * Doubles the GRTT. Used when a response to a message is not received.
     */
    public void doubleGRTT() {
        grtt *= 2;
    }

    /**
     * Pushes a message to the instantiating application.
     * 
     * @param packet
     *            The packet to push.
     * @param forcePush
     *            If the packet should be forced to the application, ignoring
     *            duplicates.
     */
    public void pushToClient(ChatPacket packet, boolean forcePush) {
        if (!passedToClient.containsKey(packet.getSrc())) {
            passedToClient.put(packet.getSrc(), new HashSet<Integer>());
        }

        if (packet.getType() == PacketType.CHAT_MESSAGE) {
            int msgId = ((ChatMessage) packet.getPayload()).getMessageId();
            if (!passedToClient.get(packet.getSrc()).contains(msgId) || forcePush) {
                passedToClient.get(packet.getSrc()).add(msgId);
                clientCallback.receivePacket(packet);
            }
        } else if (packet.getType() == PacketType.USER_PRESENCE) {
            clientCallback.receivePacket(packet);
        }
    }

    public void pushToClient(ChatPacket packet) {
        pushToClient(packet, false);
    }

    public ChatPacket wrapPayload(ChatPayload pld) {
        return new ChatPacket(version, getNextSeq(), uuid, pld);
    }

    /**
     * Executes an application command on the socket (e.g. join a room, send a
     * message)
     * 
     * @param cmd
     *            The command to execute.
     * @throws InvalidCommandException
     */
    public void executeCommand(final Command cmd) throws InvalidCommandException {
        cmd.invoke(this);
    }

    public void sendPacket(ChatPacket packet) throws IOException {
        protocol.send(packet.pack());
    }

    /**
     * Waits at most a specific amount of time for a packet.
     * 
     * @param timeout
     *            The time to wait.
     * @param matcher
     *            A matcher to select a packet.
     * @return
     */
    public synchronized ChatPacket waitFor(int timeout, PacketMatcher matcher) {
        try {
            // Determine the raw time to end
            long endTime = Calendar.getInstance().getTimeInMillis() + timeout;
            long waitTime;
            // While no matching packet has been received
            do {
                // If the time has expired, return null
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

    /**
     * Processes an incoming raw packet.
     */
    @Override
    public synchronized void processPacket(byte[] data) {
        ChatPacket packet = new ChatPacket(data);
        // If its a duplicate, or from the local instance, drop the packet
        if (!duplicates.heard(packet) && !packet.getSrc().equals(uuid)) {

            // Basic packet checking
            if (packet.getPayload() != null && packet.getPayload().getType() == packet.getType()) {
                duplicates.add(packet);
                last = packet;
                // Notify anything that is blocked on waitFor()
                notifyAll();

                // Pass it to all the handlers
                for (Handler h : handlers) {
                    if (h.accepts(packet)) {
                        h.process(this, packet);
                    }
                }
            }
        }
    }

    /**
     * Class providing duplicate filtering.
     * 
     * @author Aaron Rosenfeld <ar374@drexel.edu>
     * 
     */
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
