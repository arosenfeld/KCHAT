package net;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 
 * @author arosenfeld
 */
public abstract class TransportProtocol extends Thread {

    protected String iface;
    protected InetAddress address;
    protected int port;
    protected PacketCallback cb;

    public TransportProtocol(String iface, String host, int port)
            throws UnknownHostException {
        this.iface = iface;
        this.address = InetAddress.getByName(host);
        this.port = port;
    }

    public void setCallback(PacketCallback cb) {
        this.cb = cb;
    }

    public abstract void close();

    public abstract void send(byte[] data);
}
