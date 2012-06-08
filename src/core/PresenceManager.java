package core;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Map;

import packets.messages.RoomComparisonMessage;

import util.Configuration;
import util.Logging;
import util.LongInteger;

public class PresenceManager {
    private Map<LongInteger, Room> presences;

    public PresenceManager() {
        presences = Collections.synchronizedMap(new HashMap<LongInteger, Room>());
    }

    public LongInteger[] getRooms() {
        return presences.keySet().toArray(new LongInteger[presences.size()]);
    }

    public synchronized void setPresence(LongInteger room, LongInteger user, boolean present) {
        if (!presences.containsKey(room)) {
            presences.put(room, new Room(room));
            notifyAll();
        }

        if (present) {
            presences.get(room).members.add(user);
        } else {
            presences.get(room).members.remove(user);
            if (presences.get(room).members.size() == 0) {
                presences.remove(room);
            }
        }
    }

    public synchronized Set<LongInteger> membersOf(LongInteger room) {
        if (!presences.containsKey(room) || presences.get(room).members.size() == 0) {
            return new HashSet<LongInteger>();
        }

        return presences.get(room).members;
    }

    public synchronized boolean isPresent(LongInteger room, LongInteger user) {
        return presences.containsKey(room) && presences.get(room).members.contains(user);
    }

    public synchronized LongInteger hashMembers(LongInteger roomName) {
        LongInteger hash = new LongInteger();
        if (!presences.containsKey(roomName)) {
            return hash;
        }

        for (LongInteger m : presences.get(roomName).members) {
            hash.xorWith(m);
        }

        return hash;
    }

    public void startQueries(final ChatSocket sock) {
        final PresenceManager lcl = this;
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    Room nextRoom;
                    while ((nextRoom = nextTimer()) == null) {
                        Logging.getLogger().info("Waiting...");
                        try {
                            synchronized (lcl) {
                                lcl.wait();
                            }
                        } catch (InterruptedException e) {
                            Logging.getLogger().warning("wait was interrupted");
                        }
                    }

                    if (nextRoom.timerEndTime - Calendar.getInstance().getTimeInMillis() > 0) {
                        try {
                            Thread.sleep(nextRoom.timerEndTime - Calendar.getInstance().getTimeInMillis());
                        } catch (InterruptedException e1) {
                            Logging.getLogger().warning("Thread sleep interrupted.");
                        }
                    }

                    Logging.getLogger().info("Sending room comparison.");
                    try {
                        sock.sendPacket(sock.wrapPayload(new RoomComparisonMessage(nextRoom.name,
                                hashMembers(nextRoom.name))));
                        nextRoom.generateTimer();
                    } catch (IOException e) {
                        Logging.getLogger().warning("Unable to send room comparison");
                    }
                }
            }
        }).start();
    }

    private Room nextTimer() {
        Room min = null;
        for (LongInteger r : presences.keySet()) {
            if (min == null || presences.get(r).timerEndTime < min.timerEndTime) {
                min = presences.get(r);
            }
        }
        return min;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LongInteger r : presences.keySet()) {
            sb.append(r);
            sb.append(" members: \n");
            for (LongInteger m : presences.get(r).members) {
                sb.append("    ");
                sb.append(m);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private class Room {
        public LongInteger name;
        public Set<LongInteger> members;
        public long timerEndTime;
        private Random rand;

        public Room(LongInteger name) {
            this.name = name;
            this.members = Collections.synchronizedSet(new HashSet<LongInteger>());
            this.rand = new Random();
            this.generateTimer();
        }

        public void generateTimer() {
            timerEndTime = Calendar.getInstance().getTimeInMillis()
                    + rand.nextInt(1000 * Configuration.getInstance().getValueAsInt("timer.rmqi"));
        }
    }
}
