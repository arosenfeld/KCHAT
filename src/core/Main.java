package core;

import java.io.IOException;

import packets.Message;
import packets.Message.ChatFields;
import util.LongInteger;

public class Main {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        ChatHeader hdr = new ChatHeader((byte) 123, new LongInteger("SRC123".getBytes()), PacketType.CHAT_MESSAGE);
        Message m = new Message(hdr, 123, 456, new LongInteger("DEST098".getBytes()), "This is a test".getBytes());
        m.setParam(ChatFields.PERSIST, true);
        System.out.println(m.toString());
        
        byte[] packed = m.pack();

        Message m2 = new Message(packed);
        System.out.println(m2.toString());
    }
}