package operations.handlers;

import core.ChatSocket;
import packets.ChatPacket;

public abstract class Handler {
    public abstract boolean accepts(ChatPacket packet);
    public abstract void process(ChatSocket sock, ChatPacket packet);
}
