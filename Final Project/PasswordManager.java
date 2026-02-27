import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PasswordManager {
    private final Encryption encryption;
    private final FileHandler fileHandler;
    private final List<VaultEntry> entries;

    public PasswordManager(Encryption encryption, FileHandler fileHandler) {
        this.encryption = encryption;
        this.fileHandler = fileHandler;
        this.entries = fileHandler.loadEntries();
    }

    public List<VaultEntry> getAllEntries() {
        List<VaultEntry> copy = new ArrayList<>(entries);
        copy.sort(Comparator.comparing(VaultEntry::getTitle, String.CASE_INSENSITIVE_ORDER));
        return copy;
    }

    public List<VaultEntry> search(String query) {
        if (query == null || query.isBlank()) {
            return getAllEntries();
        }

        String needle = query.trim().toLowerCase();
        List<VaultEntry> results = new ArrayList<>();
        for (VaultEntry entry : entries) {
            if (containsIgnoreCase(entry.getTitle(), needle)
                    || containsIgnoreCase(entry.getUsername(), needle)
                    || containsIgnoreCase(entry.getWebsite(), needle)
                    || containsIgnoreCase(entry.getNotes(), needle)) {
                results.add(entry);
            }
        }
        results.sort(Comparator.comparing(VaultEntry::getTitle, String.CASE_INSENSITIVE_ORDER));
        return results;
    }

    public VaultEntry createEntry(String title, String username, String website, String notes, String plainPassword) throws Exception {
        VaultEntry entry = new VaultEntry(title, username, website, notes, encryption.encrypt(plainPassword));
        entries.add(entry);
        persist();
        return entry;
    }

    public void updateEntry(String id, String title, String username, String website, String notes, String plainPassword) throws Exception {
        VaultEntry existing = findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Entry no longer exists.");
        }
        existing.update(title, username, website, notes, encryption.encrypt(plainPassword));
        persist();
    }

    public void deleteEntry(String id) throws Exception {
        VaultEntry existing = findById(id);
        if (existing != null) {
            entries.remove(existing);
            persist();
        }
    }

    public String decryptPassword(VaultEntry entry) throws Exception {
        return encryption.decrypt(entry.getEncryptedPassword());
    }

    public static String generatePassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-=[]{}";
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }

    private VaultEntry findById(String id) {
        for (VaultEntry entry : entries) {
            if (entry.getId().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    private void persist() throws Exception {
        fileHandler.saveEntries(entries);
    }

    private boolean containsIgnoreCase(String text, String needle) {
        return text != null && text.toLowerCase().contains(needle);
    }
}
