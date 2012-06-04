package core;

import packets.ChatPacket;

public interface ChatPacketCallback {
    public void receivePacket(ChatPacket message);
}
