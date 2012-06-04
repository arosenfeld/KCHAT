package core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

import util.LongInteger;

public class PresenceManager {
    private Map<LongInteger, Set<LongInteger>> presences;

    public PresenceManager() {
        presences = new HashMap<LongInteger, Set<LongInteger>>();
    }

    public void setPresence(LongInteger room, LongInteger user, boolean present) {
        if (!presences.containsKey(room)) {
            presences.put(room, new HashSet<LongInteger>());
        }
        if (present) {
            presences.get(room).add(user);
        } else {
            presences.get(room).remove(user);
            if (presences.get(room).size() == 0) {
                presences.remove(room);
            }
        }
    }

    public LongInteger[] membersOf(LongInteger room) {
        if (!presences.containsKey(room) || presences.get(room).size() == 0) {
            return new LongInteger[0];
        }

        return presences.get(room).toArray(new LongInteger[presences.get(room).size()]);
    }

    public boolean isPresent(LongInteger room, LongInteger user) {
        return presences.containsKey(room) && presences.get(room).contains(user);
    }

    public LongInteger hashMembers(LongInteger roomName) {
        LongInteger hash = new LongInteger();
        if (!presences.containsKey(roomName)) {
            return hash;
        }

        for (LongInteger m : presences.get(roomName)) {
            hash.xorWith(m);
        }

        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (LongInteger r : presences.keySet()) {
            sb.append(r);
            sb.append(" members: \n");
            for (LongInteger m : presences.get(r)) {
                sb.append("    ");
                sb.append(m);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
