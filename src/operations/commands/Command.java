package operations.commands;

import core.ChatSocket;

/**
 * Abstract class for commands that applications can send to the socket.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public abstract class Command {
    public abstract void invoke(ChatSocket socket) throws InvalidCommandException;
}
