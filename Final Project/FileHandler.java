import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final String VAULT_FILE = "passwords.db";
    private static final String SALT_FILE = "salt.bin";
    private static final String VERIFIER_FILE = "verifier.bin";

    public List<VaultEntry> loadEntries() {
        Path path = Path.of(VAULT_FILE);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(VAULT_FILE))) {
            Object data = in.readObject();
            if (data instanceof List<?>) {
                List<?> raw = (List<?>) data;
                List<VaultEntry> entries = new ArrayList<>();
                for (Object item : raw) {
                    if (item instanceof VaultEntry entry) {
                        entries.add(entry);
                    }
                }
                return entries;
            }
        } catch (Exception ignored) {
        }

        return new ArrayList<>();
    }

    public void saveEntries(List<VaultEntry> entries) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(VAULT_FILE))) {
            out.writeObject(entries);
        }
    }

    public void saveSalt(byte[] salt) throws IOException {
        try (FileOutputStream out = new FileOutputStream(SALT_FILE)) {
            out.write(salt);
        }
    }

    public byte[] loadSalt() {
        try (FileInputStream in = new FileInputStream(SALT_FILE)) {
            return in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    public void saveVerifier(Encryption.EncryptedData verifier) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(VERIFIER_FILE))) {
            out.writeObject(verifier);
        }
    }

    public Encryption.EncryptedData loadVerifier() {
        Path path = Path.of(VERIFIER_FILE);
        if (!Files.exists(path)) {
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(VERIFIER_FILE))) {
            Object data = in.readObject();
            if (data instanceof Encryption.EncryptedData encryptedData) {
                return encryptedData;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
