import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

public class PasswordManagerUI extends JFrame {
    private final PasswordManager manager;

    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField searchField;
    private final JTextField titleField;
    private final JTextField usernameField;
    private final JTextField websiteField;
    private final JPasswordField passwordField;
    private final JTextArea notesArea;

    private List<VaultEntry> currentResults;
    private String selectedEntryId;

    public PasswordManagerUI(PasswordManager manager) {
        this.manager = manager;

        setTitle("Secure Password Manager");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tableModel = new DefaultTableModel(new String[]{"Title", "Username", "Website", "Updated"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        searchField = new JTextField();
        titleField = new JTextField();
        usernameField = new JTextField();
        websiteField = new JTextField();
        passwordField = new JPasswordField();
        notesArea = new JTextArea(6, 30);

        buildLayout();
        wireEvents();
        refreshTable("");
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel left = new JPanel(new BorderLayout(5, 5));
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        left.add(searchPanel, BorderLayout.NORTH);
        left.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel right = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        right.add(new JLabel("Title"), c);
        c.gridx = 1;
        right.add(titleField, c);

        c.gridx = 0;
        c.gridy++;
        right.add(new JLabel("Username / Email"), c);
        c.gridx = 1;
        right.add(usernameField, c);

        c.gridx = 0;
        c.gridy++;
        right.add(new JLabel("Website"), c);
        c.gridx = 1;
        right.add(websiteField, c);

        c.gridx = 0;
        c.gridy++;
        right.add(new JLabel("Password"), c);
        c.gridx = 1;
        right.add(passwordField, c);

        JButton generateButton = new JButton("Generate");
        c.gridx = 2;
        right.add(generateButton, c);

        c.gridx = 0;
        c.gridy++;
        c.anchor = GridBagConstraints.NORTHWEST;
        right.add(new JLabel("Notes"), c);

        c.gridx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        right.add(new JScrollPane(notesArea), c);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton newButton = new JButton("New");
        JButton saveButton = new JButton("Save");
        JButton deleteButton = new JButton("Delete");
        JButton copyButton = new JButton("Copy Password");
        actions.add(newButton);
        actions.add(saveButton);
        actions.add(deleteButton);
        actions.add(copyButton);

        c.gridy++;
        c.gridx = 1;
        c.gridwidth = 2;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        right.add(actions, c);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setDividerLocation(460);
        root.add(splitPane, BorderLayout.CENTER);

        setContentPane(root);

        newButton.addActionListener(e -> clearForm());
        generateButton.addActionListener(e -> passwordField.setText(PasswordManager.generatePassword(20)));
        saveButton.addActionListener(e -> onSave());
        deleteButton.addActionListener(e -> onDelete());
        copyButton.addActionListener(e -> onCopy());
    }

    private void wireEvents() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { refreshTable(searchField.getText()); }
            @Override
            public void removeUpdate(DocumentEvent e) { refreshTable(searchField.getText()); }
            @Override
            public void changedUpdate(DocumentEvent e) { refreshTable(searchField.getText()); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < currentResults.size()) {
                    loadEntry(currentResults.get(row));
                }
            }
        });
    }

    private void refreshTable(String query) {
        currentResults = manager.search(query);
        tableModel.setRowCount(0);
        for (VaultEntry entry : currentResults) {
            tableModel.addRow(new Object[]{entry.getTitle(), entry.getUsername(), entry.getWebsite(), entry.getUpdatedAt()});
        }
    }

    private void loadEntry(VaultEntry entry) {
        selectedEntryId = entry.getId();
        titleField.setText(entry.getTitle());
        usernameField.setText(entry.getUsername());
        websiteField.setText(entry.getWebsite());
        notesArea.setText(entry.getNotes());

        try {
            passwordField.setText(manager.decryptPassword(entry));
        } catch (Exception ex) {
            showError("Unable to decrypt this entry: " + ex.getMessage());
        }
    }

    private void onSave() {
        try {
            String title = titleField.getText().trim();
            String username = usernameField.getText().trim();
            String website = websiteField.getText().trim();
            String notes = notesArea.getText();
            String password = new String(passwordField.getPassword());

            if (title.isEmpty() || password.isEmpty()) {
                showError("Title and password are required.");
                return;
            }

            if (selectedEntryId == null || selectedEntryId.isBlank()) {
                manager.createEntry(title, username, website, notes, password);
                showInfo("Entry created.");
            } else {
                manager.updateEntry(selectedEntryId, title, username, website, notes, password);
                showInfo("Entry updated.");
            }

            refreshTable(searchField.getText());
            clearForm();
        } catch (Exception ex) {
            showError("Could not save entry: " + ex.getMessage());
        }
    }

    private void onDelete() {
        if (selectedEntryId == null) {
            showError("Select an entry first.");
            return;
        }

        int response = JOptionPane.showConfirmDialog(this, "Delete this entry?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            manager.deleteEntry(selectedEntryId);
            showInfo("Entry deleted.");
            refreshTable(searchField.getText());
            clearForm();
        } catch (Exception ex) {
            showError("Could not delete entry: " + ex.getMessage());
        }
    }

    private void onCopy() {
        String password = new String(passwordField.getPassword());
        if (password.isEmpty()) {
            showError("No password to copy.");
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(password), null);
        showInfo("Password copied to clipboard.");
    }

    private void clearForm() {
        selectedEntryId = null;
        table.clearSelection();
        titleField.setText("");
        usernameField.setText("");
        websiteField.setText("");
        passwordField.setText("");
        notesArea.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
