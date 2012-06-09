package packing;

import java.io.IOException;

/**
 * Interface for any Java object that can be packed/unpacked to/from a byte
 * array.
 * 
 * @author Aaron Rosenfeld <ar374@drexel.edu>
 * 
 */
public interface Packable {

    public byte[] pack() throws IOException;

    public void unPack(byte[] data);

    public int getLength();
}
