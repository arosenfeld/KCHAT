package core;

import net.PacketHandler;
import net.TransportProtocol;

public class Broker {
    private static Broker instance;
    private TransportProtocol protocol;
    private PacketHandler packetHandler;

    private Broker() {
        this.packetHandler = new PacketHandler();
    }

    public void setProtocol(TransportProtocol protocol) {
        this.protocol = protocol;
        this.protocol.setCallback(packetHandler);
    }

    public void start() {
        this.protocol.start();
    }
    
    public void stop() {
        this.protocol.close();
    }

    public static Broker getInstance() {
        if (instance == null) {
            instance = new Broker();
        }
        return instance;
    }
}
