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

    public boolean isPresent(LongInteger room, LongInteger user) {
        return presences.containsKey(room) && presences.get(room).contains(user);
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
