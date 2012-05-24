package core;

import handlers.Handler;
import handlers.InvalidCommandException;

import java.io.IOException;
import java.util.UUID;

import packets.ChatPacket;
import packets.ChatPayload;
import packets.ChatMessage;
import packets.ChatMessage.MessageField;
import packets.ChatPacket.PacketType;
import transport.TransportProtocol;
import util.LongInteger;

public class ChatSocket {
    private TransportProtocol protocol;
    private MessageStore messageStore;
    private IncomingPacketHandler incomingPacketHandler;

    private LongInteger uuid;
    private byte version;
    private volatile int nextId;
    private volatile int nextPersistId;

    public ChatSocket(TransportProtocol protocol, MessageCallback callback, LongInteger uuid, byte version) {
        this.protocol = protocol;
        this.messageStore = new MessageStore();
        this.incomingPacketHandler = new IncomingPacketHandler(this);
        this.protocol.setCallback(incomingPacketHandler);
        this.uuid = uuid;
        this.version = version;
        this.nextId = 0;
        this.nextPersistId = 0;
    }

    public ChatSocket(TransportProtocol protocol, MessageCallback callback) {
        this(protocol, callback, new LongInteger(UUID.randomUUID()), (byte) 1);
    }

    public void start() {
        this.protocol.start();
    }

    public void stop() {
        this.protocol.close();
    }

    public IncomingPacketHandler getIncomingPacketHandler() {
        return incomingPacketHandler;
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

    public int getNextId() {
        return ++nextId;
    }

    public int getNextPersistId() {
        return ++nextPersistId;
    }

    public ChatPacket wrapPayload(ChatPayload pld) {
        return new ChatPacket(version, getNextId(), uuid, pld);
    }

    public void executeCommand(Handler cmd) throws InvalidCommandException {
        cmd.invoke(this);
    }

    public void sendPacket(ChatPacket packet) throws IOException {
        protocol.send(packet.pack());
    }
}
