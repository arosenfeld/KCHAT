package packets;

import packets.ChatPacket.PacketType;
import packing.Packable;

public interface ChatPayload extends Packable {
    public PacketType getType();
}
