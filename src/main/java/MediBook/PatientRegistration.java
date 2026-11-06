package MediBook;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * PatientRegistration provides the complete registration interface for patients.
 * Includes validation for required fields, age, phone, email, username, and password confirmation.
 */
public class PatientRegistration extends JFrame {

    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> genderBox;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public PatientRegistration() {
        initComponents();
    }

    private void initComponents() {
        setTitle("MediBook - Patient Registration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 680);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("MediBook", "Patient Registration"), BorderLayout.NORTH);

        // Content Scroll Pane
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(UITheme.BG_LIGHT);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel card = UITheme.createCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Fields
        nameField = new JTextField(20);
        ageField = new JTextField(20);
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        addressField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        styleInputs(nameField, ageField, phoneField, emailField, addressField, usernameField, passwordField, confirmPasswordField);
        genderBox.setFont(UITheme.FONT_REGULAR);
        genderBox.setBackground(Color.WHITE);

        int row = 0;
        addRow(card, gbc, row++, "Full Name *:", nameField);
        addRow(card, gbc, row++, "Age *:", ageField);
        addRow(card, gbc, row++, "Gender *:", genderBox);
        addRow(card, gbc, row++, "Phone Number *:", phoneField);
        addRow(card, gbc, row++, "Email Address *:", emailField);
        addRow(card, gbc, row++, "Address *:", addressField);
        addRow(card, gbc, row++, "Username *:", usernameField);
        addRow(card, gbc, row++, "Password *:", passwordField);
        addRow(card, gbc, row++, "Confirm Password *:", confirmPasswordField);

        contentPanel.add(card);
        contentPanel.add(Box.createVerticalStrut(15));

        // Buttons Panel
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);

        JButton registerBtn = new JButton("Register");
        UITheme.stylePrimaryButton(registerBtn);
        registerBtn.addActionListener(e -> registerPatient());

        JButton backBtn = new JButton("Back to Login");
        UITheme.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> {
            new PatientLogin().setVisible(true);
            dispose();
        });

        btnPanel.add(registerBtn);
        btnPanel.add(backBtn);

        contentPanel.add(btnPanel);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleInputs(JComponent... components) {
        for (JComponent c : components) {
            c.setFont(UITheme.FONT_REGULAR);
            c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(6, 8, 6, 8)
            ));
        }
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent input) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.FONT_BOLD);
        label.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(input, gbc);
    }

    private void registerPatient() {
        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // 1. Required fields check
        if (name.isEmpty() || ageText.isEmpty() || phone.isEmpty() || email.isEmpty()
                || address.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter all required fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Age validation
        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0 || age > 125) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid age between 1 and 125.",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Age must be a valid number.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Phone validation (10 digits)
        if (!phone.matches("^[0-9]{10}$")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid 10-digit phone number.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 4. Email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 5. Username validation
        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "Username must be at least 3 characters long.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 6. Password validation & confirmation
        if (password.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 3 characters long.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match. Please re-enter.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 7. Check username uniqueness and insert
        String checkSql = "SELECT COUNT(*) FROM patients WHERE username = ?";
        String insertSql = "INSERT INTO patients (name, age, gender, phone, email, address, username, password) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                checkPs.setString(1, username);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this,
                                "This username is already taken. Please choose another username.",
                                "Duplicate Username",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            try (PreparedStatement insertPs = con.prepareStatement(insertSql)) {
                insertPs.setString(1, name);
                insertPs.setInt(2, age);
                insertPs.setString(3, gender);
                insertPs.setString(4, phone);
                insertPs.setString(5, email);
                insertPs.setString(6, address);
                insertPs.setString(7, username);
                insertPs.setString(8, password);

                int rows = insertPs.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Registration successful! You can now log in.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    new PatientLogin().setVisible(true);
                    dispose();
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error while registering: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new PatientRegistration().setVisible(true));
    }
}
