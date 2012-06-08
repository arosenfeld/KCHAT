package operations.commands;

import util.Logging;
import core.ChatSocket;

public abstract class Command {
    public abstract void invoke(ChatSocket socket) throws InvalidCommandException;
}
