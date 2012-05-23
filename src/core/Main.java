package core;

import transport.UdpMulticast;
import util.Configuration;

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

        sock.start();
    }
}