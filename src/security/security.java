package security;

import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;

public class security {

    private static PrivateKey privKey;
    private static PublicKey pubKey;
    private static String MyPubKeyName = "KCHAT_public.key";
    private static String MyPrivKeyName = "KCHAT_private.key";

    public static void main(String[] blah) throws Exception {
        //init();
        buildKeys();
    }

    public static void init() throws Exception {
        File pubFile = new File(MyPubKeyName);
        File privFile = new File(MyPrivKeyName);

        // if the keys already exist, then use them...
        if(pubFile.exists() && privFile.exists()) {

        }
        // build new keys, but we need to make sure we dont
        // inherit any old keys...
        else {
            if(pubFile.exists()) {
                // nuke existing pub key
            }
            if(privFile.exists()) {
                // nuke existing priv key
            }
            // build the keys
            buildKeys();
        }
    }

    public static void buildKeys()
            throws Exception {

        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA");
        kg.initialize(2048);

        KeyPair pair = kg.genKeyPair();
        privKey = pair.getPrivate();
        pubKey = pair.getPublic();

        KeyFactory fact = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec pub = fact.getKeySpec(pubKey, RSAPublicKeySpec.class);
        RSAPrivateKeySpec priv = fact.getKeySpec(privKey, RSAPrivateKeySpec.class);

        // save off public and private keys....
        saveKey(MyPubKeyName, pub.getModulus(), pub.getPublicExponent());
        saveKey(MyPrivKeyName, priv.getModulus(), priv.getPrivateExponent());

//      byte[] test = "KCHAT is the best chat app built!".getBytes();
//      
//      byte[] encBytes = encrypt(test, pubKey);
//      byte[] decBytes = decrypt(encBytes, privKey);
//      
//      boolean expected = java.util.Arrays.equals(test, decBytes);
//      System.out.println("Test " + (expected ? "SUCCEEDED!" : "FAILED!"));
    }

    protected static void saveKey(String fileName, BigInteger mod, BigInteger exp)
            throws IOException {
        ObjectOutputStream oout = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(fileName)));
        try {
            oout.writeObject(mod);
            oout.writeObject(exp);
        } catch (Exception e) {
            throw new IOException("Unexpected error", e);
        } finally {
            oout.close();
        }
    }

    private static byte[] encrypt(byte[] inpBytes, PublicKey key)
            throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(inpBytes);
    }

    private static byte[] decrypt(byte[] inpBytes, PrivateKey key)
            throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(inpBytes);
    }

}
