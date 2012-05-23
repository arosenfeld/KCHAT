package transport;

/**
 * Callback interface for classes which process packets.
 * 
 * @author arosenfeld
 */
public interface PacketCallback {

    public void processPacket(byte[] data);
}
