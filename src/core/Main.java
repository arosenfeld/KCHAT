package core;

import packets.ChatPacket;
import packets.PurgeMessage;
import transport.UdpMulticast;
import util.Configuration;
import util.Logging;

public class Main {
    public static void main(String[] args) throws Exception {
        String propertiesPath = System.getProperty("kchat.propertiesPath");
        if (propertiesPath == null) {
            propertiesPath = "kchat.properties";
        }
        Configuration.getInstance().setFile(propertiesPath);

        ChatSocket sock = new ChatSocket(new UdpMulticast(Configuration.getInstance().getValueAsString("udp.iface"),
                Configuration.getInstance().getValueAsString("udp.host"), Configuration.getInstance().getValueAsInt(
                        "udp.port")), null);
        //sock.start();
        
        PurgeMessage pm = new PurgeMessage(12345);
        ChatPacket packet = sock.wrapPayload(pm);
        Logging.getLogger().info(packet.toString());
        byte[] packed = packet.pack();
        ChatPacket packet2 = new ChatPacket(packed);
        Logging.getLogger().info("----------------------------\n" + packet2.toString());
    }
}