package listeners;

import packets.Message;
import net.ChatSocket;

public abstract class Listener {
    private ChatSocket socket;

    public Listener(ChatSocket socket) {
        this.socket = socket;
    }

    public abstract boolean interested();

    public abstract void process(Message m);
}
