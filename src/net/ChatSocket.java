package net;

import java.util.List;

import core.PacketType;

import listeners.Listener;

public class ChatSocket implements PacketCallback {
    private TransportProtocol protocol;
    private List<Listener> listeners;

    public ChatSocket(TransportProtocol protocol) {
        this.protocol = protocol;
        this.protocol.setCallback(this);
    }

    public void start() {
        this.protocol.start();
    }

    public void stop() {
        this.protocol.close();
    }

    public TransportProtocol getTransport() {
        return protocol;
    }

    @Override
    public void processPacket(byte[] data) {
        /*PacketType type = new ChatHeader(data).getType();
        for (Listener l : listeners) {
            if (l.interested()) {
            }
        }*/
    }
}
