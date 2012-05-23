package util;

import java.nio.ByteBuffer;
import java.util.UUID;

public class LongInteger {
    private byte[] value;

    public LongInteger(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        setValue(bb.array());
    }

    public LongInteger(byte[] setValue) {
        setValue(setValue);
    }

    public LongInteger() {
        value = new byte[16];
    }

    public void setValue(byte[] setValue) {
        if (setValue.length > 16) {
            throw new ArrayStoreException("Invalid length of " + setValue.length);
        }

        value = new byte[16];
        for (int i = 0; i < 16; i++) {
            if (i < setValue.length) {
                value[i] = setValue[i];
            } else {
                value[i] = 0;
            }
        }
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (byte b : value) {
            buf.append(b);
            buf.append(" ");
        }
        return buf.toString();
    }
}
