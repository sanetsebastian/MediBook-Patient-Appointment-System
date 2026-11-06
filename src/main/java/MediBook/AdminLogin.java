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
 * AdminLogin handles authentication for administrators.
 * Admin account is pre-registered in the database.
 */
public class AdminLogin extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public AdminLogin() {
        initComponents();
    }

    private void initComponents() {
        setTitle("MediBook - Admin Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("MediBook", "Administrator Portal"), BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(UITheme.BG_LIGHT);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(24, 32, 24, 32));

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
        contentPanel.add(Box.createVerticalStrut(18));

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        btnPanel.setOpaque(false);

        JButton loginBtn = new JButton("Login");
        UITheme.stylePrimaryButton(loginBtn);
        loginBtn.addActionListener(e -> performLogin());

        JButton backBtn = new JButton("Back to Main Menu");
        UITheme.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> {
            new MainMenu().setVisible(true);
            dispose();
        });

        btnPanel.add(loginBtn);
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

        String sql = "SELECT admin_id, username FROM admin WHERE username = ? AND password = ?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, username);
                ps.setString(2, password);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this,
                                "Admin authentication successful!",
                                "Welcome",
                                JOptionPane.INFORMATION_MESSAGE);
                        new AdminDashboard().setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Invalid admin username or password.",
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

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new AdminLogin().setVisible(true));
    }
}
