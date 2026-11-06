package MediBook;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * MainMenu is the central entry portal of MediBook.
 * Directs users to Patient Login, Doctor Login, Admin Login, or Patient Registration.
 */
public class MainMenu extends JFrame {

    public MainMenu() {
        initComponents();
    }

    private void initComponents() {
        setTitle("MEDIBOOK - Patient Appointment Booking Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 480);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = UITheme.createHeader("MEDIBOOK", "Healthcare Management System");
        add(headerPanel, BorderLayout.NORTH);

        // Main Panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(UITheme.BG_LIGHT);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(30, 60, 30, 60));

        // Welcome Tagline
        JLabel welcomeLabel = new JLabel("Welcome to MediBook", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(UITheme.PRIMARY_COLOR);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("Please select your portal to continue", SwingConstants.CENTER);
        subLabel.setFont(UITheme.FONT_SUBTITLE);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createVerticalStrut(4));
        mainPanel.add(subLabel);
        mainPanel.add(Box.createVerticalStrut(25));

        // Button Card
        JPanel card = UITheme.createCard();
        card.setLayout(new GridLayout(4, 1, 0, 12));
        card.setMaximumSize(new Dimension(420, 240));

        JButton patientLoginBtn = new JButton("👤  Patient Login");
        UITheme.stylePrimaryButton(patientLoginBtn);
        patientLoginBtn.addActionListener(e -> {
            new PatientLogin().setVisible(true);
            dispose();
        });

        JButton doctorLoginBtn = new JButton("🩺  Doctor Login");
        UITheme.styleTealButton(doctorLoginBtn);
        doctorLoginBtn.addActionListener(e -> {
            new DoctorLogin().setVisible(true);
            dispose();
        });

        JButton adminLoginBtn = new JButton("🛡️  Admin Login");
        UITheme.styleButton(adminLoginBtn, new Color(71, 85, 105), Color.WHITE);
        adminLoginBtn.addActionListener(e -> {
            new AdminLogin().setVisible(true);
            dispose();
        });

        JButton patientRegisterBtn = new JButton("📝  New Patient? Register Here");
        UITheme.styleSecondaryButton(patientRegisterBtn);
        patientRegisterBtn.addActionListener(e -> {
            new PatientRegistration().setVisible(true);
            dispose();
        });

        card.add(patientLoginBtn);
        card.add(doctorLoginBtn);
        card.add(adminLoginBtn);
        card.add(patientRegisterBtn);

        mainPanel.add(card);
        mainPanel.add(Box.createVerticalStrut(15));

        // Footer note
        JLabel footerLabel = new JLabel("B.Tech CSE S3 Project - MediBook", SwingConstants.CENTER);
        footerLabel.setFont(UITheme.FONT_SMALL);
        footerLabel.setForeground(UITheme.TEXT_MUTED);
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(footerLabel);

        add(mainPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}
