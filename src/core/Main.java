package core;

import java.io.IOException;
import java.util.TreeMap;

import packets.Message;
import packets.Message.MessageFields;

import net.ChatHeader;
import net.ChatSocket;
import net.UdpMulticast;

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

        ChatHeader hdr = new ChatHeader((byte) 123, new LongInteger(
                "SRC123".getBytes()), PacketType.CHAT_MESSAGE);
        Message m = new Message(hdr, 123, 456, new LongInteger(
                "DEST098".getBytes()), "This is a test".getBytes());
        m.setParam(MessageFields.PERSIST, true);
        //System.out.println(m.toString());
        //byte[] packed = m.pack();
        Logging.getLogger().info("H Length: " + hdr.getLength());
        Logging.getLogger().info("Length: " + m.getLength());

        /*
         * String propertiesPath = System.getProperty("kchat.propertiesPath");
         * if (propertiesPath == null) { propertiesPath = "kchat.properties"; }
         * Configuration.getInstance().setFile(propertiesPath);
         * 
         * ChatSocket sock = new ChatSocket(new UdpMulticast(Configuration
         * .getInstance().getValueAsString("udp.iface"), Configuration
         * .getInstance().getValueAsString("udp.host"), Configuration
         * .getInstance().getValueAsInt("udp.port")));
         * 
         * sock.start();
         */
    }
}