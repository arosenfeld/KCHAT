package util;

public class BitField {
    private byte value;

    public BitField() {
        clear();
    }
    
    public BitField(byte value) {
        this.value = value;
    }
    
    public byte getValue() {
        return value;
    }

    public void setBit(byte index, boolean set) {
        value = (byte)(set ? value | (1 << index) : value & ~(1 << index));
    }
    
    public boolean isSet(byte index) {
        return (value & (1 << index)) > 0;
    }
    
    public void clear() {
        value = 0;
    }
}
