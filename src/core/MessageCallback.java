package core;

import packets.ChatMessage;

public interface MessageCallback {
    public void receiveMessage(ChatMessage message);
}
