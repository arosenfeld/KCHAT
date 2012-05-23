package packing;

import java.io.IOException;

public interface Packable {

    public byte[] pack() throws IOException;

    public void unPack(byte[] data);

    public int getLength();
}
