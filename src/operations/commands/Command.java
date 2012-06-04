package operations.commands;

import core.ChatSocket;

public abstract class Command {
    public abstract void invoke(ChatSocket socket) throws InvalidCommandException;
}
