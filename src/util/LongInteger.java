package util;

import java.nio.ByteBuffer;
import java.util.Arrays;
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

    public LongInteger(String value) {
        this(value.getBytes());
    }

    public LongInteger() {
        value = new byte[16];
        for (int i = 0; i < value.length; i++) {
            value[i] = 0;
        }
    }

    public void setValue(byte[] setValue) {
        if (setValue.length > 16) {
            throw new ArrayStoreException("Invalid length of " + setValue.length);
        }

        value = new byte[16];
        for (int i = 0; i < value.length; i++) {
            if (i < setValue.length) {
                value[i] = setValue[i];
            } else {
                value[i] = 0;
            }
        }
    }

    public void xorWith(LongInteger other) {
        for (int i = 0; i < value.length; i++) {
            value[i] ^= other.getValue()[i];
        }
    }

    public byte[] getValue() {
        return value;
    }

    public String asRaw() {
        StringBuffer buf = new StringBuffer();
        for (byte b : value) {
            buf.append(b);
            buf.append(" ");
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        return new String(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LongInteger) {
            byte[] other = ((LongInteger) obj).getValue();
            for (int i = 0; i < value.length; i++) {
                if (other[i] != value[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Arrays.hashCode(this.value);
        return hash;
    }
}
