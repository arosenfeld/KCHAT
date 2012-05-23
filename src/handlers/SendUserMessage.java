package handlers;

import packets.ChatPacket;
import packets.Message;
import packets.ChatPacket.PacketType;
import packets.Message.MessageFields;
import util.LongInteger;
import core.ChatSocket;

public class SendUserMessage extends Handler {
    private LongInteger dest;
    private byte[] message;
    private boolean persist;

    public SendUserMessage(LongInteger dest, byte[] message, boolean persist) {
        this.dest = dest;
        this.message = message;
        this.persist = persist;
    }

    @Override
    public void invoke(ChatSocket socket) throws InvalidCommandException {
        Message m = new Message(socket.getNextId(), persist ? socket.getNextPersistId() : 0, dest, message);
        ChatPacket packet = socket.wrapPayload(m);
        if (persist) {
            // TODO: Use GRTT below
            if (socket.getIncomingPacketHandler().waitFor(1000, PacketType.PURGE) == null) {
                m.setParam(MessageFields.PERSIST, true);
                // socket.sendPacket(packet);
            }
        }
    }
}
