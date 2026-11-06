package MediBook;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 * Profile management dialog for patients.
 * Allows viewing/editing demographic information and changing username/password.
 */
public class Profile extends JDialog {

    private final int patientId;
    private final Runnable onProfileUpdate;

    private JTextField nameField;
    private JTextField ageField;
    private JComboBox<String> genderBox;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField addressField;

    private JTextField newUsernameField;
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    public Profile(Frame parent, int patientId, Runnable onProfileUpdate) {
        super(parent, "My Profile & Account Settings", true);
        this.patientId = patientId;
        this.onProfileUpdate = onProfileUpdate;

        initComponents();
        loadProfileData();
    }

    private void initComponents() {
        setSize(580, 680);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("Profile & Settings", "Manage Your Details"), BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(UITheme.BG_LIGHT);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Profile Details Card
        JPanel infoCard = UITheme.createCard();
        infoCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                "Personal Information",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UITheme.FONT_SECTION,
                UITheme.PRIMARY_COLOR
        ));
        infoCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);

        nameField = new JTextField(18);
        ageField = new JTextField(18);
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        phoneField = new JTextField(18);
        emailField = new JTextField(18);
        addressField = new JTextField(18);

        styleInputs(nameField, ageField, phoneField, emailField, addressField);
        genderBox.setFont(UITheme.FONT_REGULAR);
        genderBox.setBackground(Color.WHITE);

        int r = 0;
        addFormRow(infoCard, gbc, r++, "Full Name:", nameField);
        addFormRow(infoCard, gbc, r++, "Age:", ageField);
        addFormRow(infoCard, gbc, r++, "Gender:", genderBox);
        addFormRow(infoCard, gbc, r++, "Phone:", phoneField);
        addFormRow(infoCard, gbc, r++, "Email:", emailField);
        addFormRow(infoCard, gbc, r++, "Address:", addressField);

        JButton saveInfoBtn = new JButton("Save Profile Info");
        UITheme.stylePrimaryButton(saveInfoBtn);
        saveInfoBtn.addActionListener(e -> updatePersonalInfo());
        gbc.gridx = 1; gbc.gridy = r++;
        infoCard.add(saveInfoBtn, gbc);

        contentPanel.add(infoCard);
        contentPanel.add(Box.createVerticalStrut(14));

        // Security / Credentials Card
        JPanel credCard = UITheme.createCard();
        credCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                "Account Credentials",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UITheme.FONT_SECTION,
                UITheme.PRIMARY_COLOR
        ));
        credCard.setLayout(new GridBagLayout());
        GridBagConstraints gbcCred = new GridBagConstraints();
        gbcCred.fill = GridBagConstraints.HORIZONTAL;
        gbcCred.insets = new Insets(5, 6, 5, 6);

        newUsernameField = new JTextField(18);
        currentPasswordField = new JPasswordField(18);
        newPasswordField = new JPasswordField(18);
        confirmPasswordField = new JPasswordField(18);

        styleInputs(newUsernameField, currentPasswordField, newPasswordField, confirmPasswordField);

        int cr = 0;
        addFormRow(credCard, gbcCred, cr++, "Username:", newUsernameField);
        addFormRow(credCard, gbcCred, cr++, "Current Password *:", currentPasswordField);
        addFormRow(credCard, gbcCred, cr++, "New Password:", newPasswordField);
        addFormRow(credCard, gbcCred, cr++, "Confirm Password:", confirmPasswordField);

        JButton saveCredBtn = new JButton("Update Credentials");
        UITheme.styleTealButton(saveCredBtn);
        saveCredBtn.addActionListener(e -> updateCredentials());
        gbcCred.gridx = 1; gbcCred.gridy = cr++;
        credCard.add(saveCredBtn, gbcCred);

        contentPanel.add(credCard);
        contentPanel.add(Box.createVerticalStrut(12));

        // Close Button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);
        JButton closeBtn = new JButton("Close");
        UITheme.styleSecondaryButton(closeBtn);
        closeBtn.addActionListener(e -> dispose());
        bottomBar.add(closeBtn);
        contentPanel.add(bottomBar);

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
                new EmptyBorder(5, 7, 5, 7)
            ));
        }
    }

    private void addFormRow(JPanel p, GridBagConstraints gbc, int row, String lbl, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel label = new JLabel(lbl);
        label.setFont(UITheme.FONT_BOLD);
        p.add(label, gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        p.add(comp, gbc);
    }

    private void loadProfileData() {
        String sql = "SELECT name, age, gender, phone, email, address, username FROM patients WHERE patient_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nameField.setText(rs.getString("name"));
                        ageField.setText(String.valueOf(rs.getInt("age")));
                        genderBox.setSelectedItem(rs.getString("gender"));
                        phoneField.setText(rs.getString("phone"));
                        emailField.setText(rs.getString("email"));
                        addressField.setText(rs.getString("address"));
                        newUsernameField.setText(rs.getString("username"));
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading profile: " + ex.getMessage());
        }
    }

    private void updatePersonalInfo() {
        String name = nameField.getText().trim();
        String ageStr = ageField.getText().trim();
        String gender = (String) genderBox.getSelectedItem();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || ageStr.isEmpty() || phone.isEmpty() || email.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all profile fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 125) {
                JOptionPane.showMessageDialog(this, "Please enter a valid age between 1 and 125.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Age must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!phone.matches("^[0-9]{10}$")) {
            JOptionPane.showMessageDialog(this, "Phone number must be exactly 10 digits.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE patients SET name = ?, age = ?, gender = ?, phone = ?, email = ?, address = ? WHERE patient_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setInt(2, age);
                ps.setString(3, gender);
                ps.setString(4, phone);
                ps.setString(5, email);
                ps.setString(6, address);
                ps.setInt(7, patientId);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    if (onProfileUpdate != null) onProfileUpdate.run();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCredentials() {
        String newUsername = newUsernameField.getText().trim();
        String curPass = new String(currentPasswordField.getPassword());
        String newPass = new String(newPasswordField.getPassword());
        String confPass = new String(confirmPasswordField.getPassword());

        if (curPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your current password to confirm changes.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newUsername.length() < 3) {
            JOptionPane.showMessageDialog(this, "Username must contain at least 3 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            // 1. Verify current password
            String checkPassSql = "SELECT password FROM patients WHERE patient_id = ?";
            try (PreparedStatement ps = con.prepareStatement(checkPassSql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String stored = rs.getString("password");
                        if (!stored.equals(curPass)) {
                            JOptionPane.showMessageDialog(this, "Current password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            }

            // 2. Check username uniqueness
            String checkUserSql = "SELECT COUNT(*) FROM patients WHERE username = ? AND patient_id <> ?";
            try (PreparedStatement ps = con.prepareStatement(checkUserSql)) {
                ps.setString(1, newUsername);
                ps.setInt(2, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "This username is already taken. Please choose another.", "Duplicate Username", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            // 3. Update password if provided
            String targetPass = curPass;
            if (!newPass.isEmpty()) {
                if (newPass.length() < 3) {
                    JOptionPane.showMessageDialog(this, "New password must be at least 3 characters.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!newPass.equals(confPass)) {
                    JOptionPane.showMessageDialog(this, "New password and Confirm Password do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                targetPass = newPass;
            }

            String updateSql = "UPDATE patients SET username = ?, password = ? WHERE patient_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setString(1, newUsername);
                ps.setString(2, targetPass);
                ps.setInt(3, patientId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Credentials updated successfully!\nPlease use your new credentials on your next login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    currentPasswordField.setText("");
                    newPasswordField.setText("");
                    confirmPasswordField.setText("");
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating credentials: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
