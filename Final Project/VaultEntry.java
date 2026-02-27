import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class VaultEntry implements Serializable {
    private final String id;
    private String title;
    private String username;
    private String website;
    private String notes;
    private Encryption.EncryptedData encryptedPassword;
    private final Instant createdAt;
    private Instant updatedAt;

    public VaultEntry(String title, String username, String website, String notes, Encryption.EncryptedData encryptedPassword) {
        this.id = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        update(title, username, website, notes, encryptedPassword);
    }

    public void update(String title, String username, String website, String notes, Encryption.EncryptedData encryptedPassword) {
        this.title = title;
        this.username = username;
        this.website = website;
        this.notes = notes;
        this.encryptedPassword = encryptedPassword;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getUsername() { return username; }
    public String getWebsite() { return website; }
    public String getNotes() { return notes; }
    public Encryption.EncryptedData getEncryptedPassword() { return encryptedPassword; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
