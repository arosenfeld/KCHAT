package transport;

/**
 * Callback interface for classes which process packets.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 */
public interface PacketCallback {

    public void processPacket(byte[] data);
}
