package handlers;

import core.ChatSocket;

public abstract class Handler {
    public abstract void invoke(ChatSocket socket) throws InvalidCommandException;
}
