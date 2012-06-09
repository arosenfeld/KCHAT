package operations.handlers;

import core.ChatSocket;
import packets.ChatPacket;

/**
 * Abstract class for a handler which deals with packets incoming off the wire.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public abstract class Handler {
    public abstract boolean accepts(ChatPacket packet);

    public abstract void process(ChatSocket sock, ChatPacket packet);
}
