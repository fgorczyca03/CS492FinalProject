import javax.crypto.SecretKey;
import javax.swing.*;

public class Main {
    private static final String VAULT_CHECK_VALUE = "vault-unlocked";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FileHandler fileHandler = new FileHandler();
                byte[] salt = fileHandler.loadSalt();

                String prompt = (salt == null) ? "Create a master password:" : "Enter master password:";
                String masterPassword = promptForPassword(prompt);
                if (masterPassword == null || masterPassword.isBlank()) {
                    return;
                }

                if (salt == null) {
                    salt = Encryption.generateSalt();
                    fileHandler.saveSalt(salt);
                }

                SecretKey key = Encryption.deriveKey(masterPassword, salt);
                Encryption encryption = new Encryption(key);

                Encryption.EncryptedData verifier = fileHandler.loadVerifier();
                if (verifier == null) {
                    fileHandler.saveVerifier(encryption.encrypt(VAULT_CHECK_VALUE));
                } else {
                    String check = encryption.decrypt(verifier);
                    if (!VAULT_CHECK_VALUE.equals(check)) {
                        JOptionPane.showMessageDialog(null, "Invalid master password.", "Access denied", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                PasswordManager manager = new PasswordManager(encryption, fileHandler);
                PasswordManagerUI ui = new PasswordManagerUI(manager);
                ui.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Failed to open vault: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static String promptForPassword(String message) {
        JPasswordField field = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(null, field, message, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option != JOptionPane.OK_OPTION) {
            return null;
        }
        return new String(field.getPassword());
    }
}
