package handlers;

import java.util.Set;

import packets.ChatPacket;
import packets.ChatPacket.PacketType;
import core.ChatSocket;

public abstract class Handler {
    public abstract void invoke(ChatSocket socket) throws InvalidCommandException;
}
