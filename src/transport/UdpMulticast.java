package transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;

/**
 * Wrapper for MulticastSocket. Provides simple multicast send/recv
 * functionality.
 * 
 * @author arosenfeld
 */
public class UdpMulticast extends TransportProtocol {

    public static final int MAX_LENGTH = 2048;
    private MulticastSocket socket;
    private boolean running;

    /**
     * Instantiates a new UdpMulticast instance.
     * 
     * @param iface
     *            Interface on which to multicast.
     * @param host
     *            IP or hostname on which to multicast.
     * @param port
     *            Port on which to multicast.
     * @param callback
     *            Callback to invoke when a packet is received on the socket
     * @throws IOException
     */
    public UdpMulticast(String iface, String host, int port) throws IOException {
        super(iface, host, port);
        this.running = true;

        socket = new MulticastSocket(port);
        socket.setNetworkInterface(NetworkInterface.getByName(iface));
        socket.setLoopbackMode(false);
        socket.joinGroup(address);
    }

    /**
     * Stops the socket.
     */
    public void close() {
        running = false;
        socket.close();
    }

    /**
     * Sends a packet.
     * 
     * @param data
     *            Packet data.
     */
    public void send(byte[] data) {
        DatagramPacket p = new DatagramPacket(data, data.length, address, port);
        try {
            socket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the socket's listener.
     */
    @Override
    public void run() {
        while (running) {
            try {
                byte[] buf = new byte[MAX_LENGTH];
                DatagramPacket p = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(p);
                    cb.processPacket(p.getData());
                } catch(SocketException e) {
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
