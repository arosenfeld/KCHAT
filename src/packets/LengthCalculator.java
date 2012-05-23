package packets;

import java.lang.reflect.Field;

import packing.Packable;


public class LengthCalculator {
    public static final int getLength(Packable p) {
        int size = 0;
        for (Field f : p.getClass().getDeclaredFields()) {
            PacketField header;
            if ((header = f.getAnnotation(PacketField.class)) != null) {
                size += header.size() + header.additional();
            }
        }
        return size;
    }
}
