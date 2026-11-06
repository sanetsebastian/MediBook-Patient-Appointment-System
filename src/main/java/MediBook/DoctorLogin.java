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
 * DoctorLogin handles authentication for doctors.
 * Includes Forgot Password feature for credential recovery.
 */
public class DoctorLogin extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public DoctorLogin() {
        initComponents();
    }

    private void initComponents() {
        setTitle("MediBook - Doctor Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 440);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("MediBook", "Doctor Portal"), BorderLayout.NORTH);

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
        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 0, 8));
        btnPanel.setOpaque(false);

        JButton loginBtn = new JButton("Login");
        UITheme.stylePrimaryButton(loginBtn);
        loginBtn.addActionListener(e -> performLogin());

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
        btnPanel.add(forgotPasswordBtn);
        btnPanel.add(backBtn);

        contentPanel.add(btnPanel);
        add(contentPanel, BorderLayout.CENTER);

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

        String sql = "SELECT doctor_id, name FROM doctors WHERE username = ? AND password = ?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int doctorId = rs.getInt("doctor_id");
                        String name = rs.getString("name");
                        JOptionPane.showMessageDialog(this,
                                "Login successful! Welcome, " + name + ".",
                                "Welcome",
                                JOptionPane.INFORMATION_MESSAGE);
                        new DoctorDashboard(doctorId, name).setVisible(true);
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

        int option = JOptionPane.showConfirmDialog(this, message, "Doctor - Reset Password",
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

        String verifySql = "SELECT doctor_id, username, name FROM doctors WHERE (username = ? OR phone = ?) AND email = ?";
        String updateSql = "UPDATE doctors SET password = ? WHERE doctor_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            int doctorId = -1;
            String foundUsername = "";

            try (PreparedStatement ps = con.prepareStatement(verifySql)) {
                ps.setString(1, userOrPhone);
                ps.setString(2, userOrPhone);
                ps.setString(3, email);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        doctorId = rs.getInt("doctor_id");
                        foundUsername = rs.getString("username");
                    }
                }
            }

            if (doctorId == -1) {
                JOptionPane.showMessageDialog(this,
                        "No doctor account found matching the provided details.\nPlease check your username/phone and email.",
                        "Account Not Found",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                updatePs.setString(1, newPass);
                updatePs.setInt(2, doctorId);
                updatePs.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Password reset successfully for Doctor account '" + foundUsername + "'!\n"
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
        SwingUtilities.invokeLater(() -> new DoctorLogin().setVisible(true));
    }
}
