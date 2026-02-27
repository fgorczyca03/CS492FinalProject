import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Encryption {
    private final SecretKey key;

    public Encryption(SecretKey key) {
        this.key = key;
    }

    public static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static byte[] sha256(byte[] input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(input);
    }

    public EncryptedData encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        return new EncryptedData(
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(encrypted),
                Base64.getEncoder().encodeToString(sha256(encrypted))
        );
    }

    public String decrypt(EncryptedData data) throws Exception {
        byte[] iv = Base64.getDecoder().decode(data.iv());
        byte[] encrypted = Base64.getDecoder().decode(data.cipherText());

        String newHash = Base64.getEncoder().encodeToString(sha256(encrypted));
        if (!newHash.equals(data.hash())) {
            throw new SecurityException("Integrity check failed. Vault data may be corrupted.");
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    public record EncryptedData(String iv, String cipherText, String hash) implements Serializable {}
}
