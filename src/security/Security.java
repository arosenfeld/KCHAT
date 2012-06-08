package security;

import java.security.spec.*;
import java.util.*;
import java.security.*;
import javax.crypto.*;
import util.LongInteger;

public class Security {

    private static PrivateKey myPrivKey;
    private static PublicKey myPubKey;
    private static Hashtable<LongInteger, PublicKey> pubKeys;

    public Security() throws Exception {
        buildKeys();
        pubKeys = new Hashtable<LongInteger, PublicKey>();
    }

    private static void buildKeys() throws Exception {
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
        kg.initialize(2048);

        KeyPair pair = kg.genKeyPair();
        myPrivKey = pair.getPrivate();
        myPubKey = pair.getPublic();
    }

    public byte[] encrypt(LongInteger userId, byte[] msg) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, pubKeys.get(userId));
        return cipher.doFinal(msg);
    }

    public byte[] decrypt(byte[] msg) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, myPrivKey);
        return cipher.doFinal(msg);
    }

    public void RemovePublicKey(LongInteger userId) {
        if (pubKeys.containsKey(userId)) {
            pubKeys.remove(userId);
        }
    }

    public byte[] SendMyPublicKey() {
        return myPubKey.getEncoded();
    }

    public void SaveUserPublicKey(LongInteger userId, byte[] bytes) throws NoSuchAlgorithmException,
            InvalidKeySpecException {
        PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(bytes));
        pubKeys.put(userId, pubKey);
    }

    public boolean UserHasPublicKey(LongInteger userId) {
        return pubKeys.containsKey(userId);
    }
}
