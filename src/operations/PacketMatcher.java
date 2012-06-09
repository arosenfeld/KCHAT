package operations;

import packets.ChatPacket;
import packets.ChatPacket.PacketType;

/**
 * An interface for matching packets.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public interface PacketMatcher {
    public boolean matches(ChatPacket packet);

    /**
     * Simple matcher for basing selection on PacketType
     * 
     * @author Aaron Rosenfeld <ar374@drexel.edu>
     * 
     */
    public static class TypeMatcher implements PacketMatcher {
        private PacketType[] types;

        public TypeMatcher(PacketType... types) {
            this.types = types;
        }

        @Override
        public boolean matches(ChatPacket packet) {
            for (PacketType t : types) {
                if (packet.getType() == t) {
                    return true;
                }
            }
            return false;
        }
    }
}
