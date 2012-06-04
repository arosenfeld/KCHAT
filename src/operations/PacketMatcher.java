package operations;

import packets.ChatPacket;
import packets.ChatPacket.PacketType;

public interface PacketMatcher {
    public boolean matches(ChatPacket packet);

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
