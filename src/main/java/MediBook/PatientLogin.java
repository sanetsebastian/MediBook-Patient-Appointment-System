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
 * PatientLogin handles authentication for patients.
 * Includes Forgot Password feature for credential recovery.
 */
public class PatientLogin extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public PatientLogin() {
        initComponents();
    }

    private void initComponents() {
        setTitle("MediBook - Patient Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("MediBook", "Patient Login"), BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(UITheme.BG_LIGHT);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 32, 20, 32));

        JPanel card = UITheme.createCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        usernameField = new JTextField(18);
        passwordField = new JPasswordField(18);

        usernameField.setFont(UITheme.FONT_REGULAR);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1),
            new EmptyBorder(6, 8, 6, 8)
        ));

        passwordField.setFont(UITheme.FONT_REGULAR);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1),
            new EmptyBorder(6, 8, 6, 8)
        ));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(UITheme.FONT_BOLD);
        card.add(lblUser, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        card.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(UITheme.FONT_BOLD);
        card.add(lblPass, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        card.add(passwordField, gbc);

        contentPanel.add(card);
        contentPanel.add(Box.createVerticalStrut(14));

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(4, 1, 0, 8));
        btnPanel.setOpaque(false);

        JButton loginBtn = new JButton("Login");
        UITheme.stylePrimaryButton(loginBtn);
        loginBtn.addActionListener(e -> performLogin());

        JButton registerBtn = new JButton("New Patient? Register Here");
        UITheme.styleTealButton(registerBtn);
        registerBtn.addActionListener(e -> {
            new PatientRegistration().setVisible(true);
            dispose();
        });

        JButton forgotPasswordBtn = new JButton("🔑 Forgot Password?");
        UITheme.styleSecondaryButton(forgotPasswordBtn);
        forgotPasswordBtn.addActionListener(e -> handleForgotPassword());

        JButton backBtn = new JButton("Back to Main Menu");
        UITheme.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });

        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        btnPanel.add(forgotPasswordBtn);
        btnPanel.add(backBtn);

        contentPanel.add(btnPanel);
        add(contentPanel, BorderLayout.CENTER);

        // Press Enter to login
        getRootPane().setDefaultButton(loginBtn);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter all required fields.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT patient_id, name FROM patients WHERE username = ? AND password = ?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int patientId = rs.getInt("patient_id");
                        String name = rs.getString("name");
                        JOptionPane.showMessageDialog(this,
                                "Login successful! Welcome, " + name + ".",
                                "Welcome",
                                JOptionPane.INFORMATION_MESSAGE);
                        new PatientDashboard(patientId, name).setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Invalid username or password.",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleForgotPassword() {
        JTextField userOrPhoneField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JPasswordField newPasswordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);

        Object[] message = {
            "Enter Registered Username or Phone:", userOrPhoneField,
            "Enter Registered Email Address:", emailField,
            "Enter New Password:", newPasswordField,
            "Confirm New Password:", confirmPasswordField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Patient - Reset Password",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) return;

        String userOrPhone = userOrPhoneField.getText().trim();
        String email = emailField.getText().trim();
        String newPass = new String(newPasswordField.getPassword());
        String confPass = new String(confirmPasswordField.getPassword());

        if (userOrPhone.isEmpty() || email.isEmpty() || newPass.isEmpty() || confPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all fields to reset your password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newPass.length() < 3) {
            JOptionPane.showMessageDialog(this, "New password must be at least 3 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPass.equals(confPass)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String verifySql = "SELECT patient_id, username, name FROM patients WHERE (username = ? OR phone = ?) AND email = ?";
        String updateSql = "UPDATE patients SET password = ? WHERE patient_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            int patientId = -1;
            String foundUsername = "";

            try (PreparedStatement ps = con.prepareStatement(verifySql)) {
                ps.setString(1, userOrPhone);
                ps.setString(2, userOrPhone);
                ps.setString(3, email);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        patientId = rs.getInt("patient_id");
                        foundUsername = rs.getString("username");
                    }
                }
            }

            if (patientId == -1) {
                JOptionPane.showMessageDialog(this,
                        "No patient account found matching the provided details.\nPlease check your username/phone and email.",
                        "Account Not Found",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                updatePs.setString(1, newPass);
                updatePs.setInt(2, patientId);
                updatePs.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Password reset successfully for account '" + foundUsername + "'!\n"
                    + "You can now log in with your new password.",
                    "Password Reset Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            usernameField.setText(foundUsername);
            passwordField.setText("");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new PatientLogin().setVisible(true));
    }
}
