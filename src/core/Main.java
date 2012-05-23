package core;

import java.io.IOException;
import java.util.TreeMap;

import packets.ChatPacket;
import packets.ChatPayload;
import packets.Message;
import packets.Message.MessageFields;

import net.UdpMulticast;

import util.BitField;
import util.Configuration;
import util.Logging;
import util.LongInteger;

public class Main {

    /**
     * @param args
     * @throws IOException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
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