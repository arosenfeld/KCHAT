package packets;

import packets.ChatPacket.PacketType;
import packing.Packable;

/**
 * Interface for each type of KCHAT payload (e.g. ChatMessage, UserPresence)
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public interface ChatPayload extends Packable {
    public PacketType getType();
}
