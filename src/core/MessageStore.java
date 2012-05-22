package core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import packets.Message;

import util.LongInteger;

public class MessageStore {
    private Map<LongInteger, TreeMap<Integer, Message>> messages;

    public MessageStore() {
        messages = Collections
                .synchronizedMap(new HashMap<LongInteger, TreeMap<Integer, Message>>());
    }

    public void addMessage(Message m) {
        if (!messages.containsKey(m.getHeader().getSrc())) {
            messages.put(m.getHeader().getSrc(),
                    (TreeMap<Integer, Message>) Collections
                            .synchronizedMap(new TreeMap<Integer, Message>()));
        }
        messages.get(m.getHeader().getSrc()).put(m.getPersistenceId(), m);
    }
}
