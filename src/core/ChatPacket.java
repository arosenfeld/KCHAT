package core;

import util.Packable;

public abstract class ChatPacket implements Packable {    
    protected ChatHeader header;

    public ChatPacket(ChatHeader header) {
        this.header = header;
    }
    
    protected abstract PacketType getPacketType();
}
