package core;

import packets.Message;

public interface MessageCallback {
    public void receiveMessage(Message message);
}
