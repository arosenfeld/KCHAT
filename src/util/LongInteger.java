package util;

public class LongInteger {
    private byte[] value;

    public LongInteger(byte[] setValue) {
        setValue(setValue);
    }
    
    public LongInteger() {
        value = new byte[16];
    }

    public void setValue(byte[] setValue) {
        if (setValue.length > 16) {
            throw new ArrayStoreException("Invalid length of "
                    + setValue.length);
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
}
