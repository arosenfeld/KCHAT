package packets;

import packing.Packable;
import net.ChatHeader;
import core.PacketType;

public abstract class ChatPacket implements Packable {
    protected ChatHeader header;

    public ChatPacket(ChatHeader header) {
        this.header = header;
    }

    public final ChatHeader getHeader() {
        return header;
    }

    protected abstract PacketType getPacketType();
}
