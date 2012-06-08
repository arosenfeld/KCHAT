package security;

import java.util.*;
import java.security.*;
import javax.crypto.*;
import util.LongInteger;

public class security {

    private static PrivateKey myPrivKey;
    private static PublicKey myPubKey;
    private static Hashtable pubKeys;

    public static void main(String[] blah) throws Exception {
        buildKeys();
    }

    public security() throws Exception
    {
        buildKeys();
        pubKeys = new Hashtable();
    }

    private static void buildKeys()
            throws Exception
    {
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
        kg.initialize(2048);

        KeyPair pair = kg.genKeyPair();
        myPrivKey = pair.getPrivate();
        myPubKey = pair.getPublic();
    }

    public byte[] encrypt(LongInteger userId, byte[] msg) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, myPubKey);
        return cipher.doFinal(msg);
    }

    public byte[] decrypt(byte[] msg) throws Exception
    {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, myPrivKey);
        return cipher.doFinal(msg);
    }

    public void SaveUserPublicKey(LongInteger userId, byte[] pubKey)
    {
        pubKeys.put(userId, pubKey);
    }

    public boolean UserHasPublicKey(LongInteger userId)
    {
        return pubKeys.containsKey(userId);
    }
}
