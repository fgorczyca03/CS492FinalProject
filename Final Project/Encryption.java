import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Base64;


public class Encryption {
    private final SecretKey key;

    //Constructor take prederived key
    public Encryption(SecretKey key) {
        this.key = key;
    }

    //Derive AES key
    public static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    //Generate salt
    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    //Computes Hash of input byte array
    public static byte[] sha256(byte[] input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input);
    }

    //Encrypts plaintext string. Returns an array
    public String[] encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        //Geberate random UV
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        //Init cipher with key and IV
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        String hash = Base64.getEncoder().encodeToString(sha256(encrypted));

        //Return IV, CT, and hash
        return new String[] {
            Base64.getEncoder().encodeToString(iv),
            Base64.getEncoder().encodeToString(encrypted),
            hash
        };
    }

    //Decrypts
    public String decrypt(String ivBase64, String encryptedBase64, String storedHash) throws Exception {
        //Decode inputs
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        byte[] encrypted = Base64.getDecoder().decode(encryptedBase64);

        //rehash the data and compare to stored hash
        String newHash = Base64.getEncoder().encodeToString(sha256(encrypted));
        if (!newHash.equals(storedHash)) {
            //If hashes dont match throw error bc data tampered
            throw new SecurityException("Integrity check failed!");
        }

        //Decrypt the CT
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        //Convert byte array to string
        return new String(decrypted);
    }
}
