package MediBook;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

/**
 * AdminDashboard provides a clean, consolidated administrative console for MediBook.
 * Features:
 * - Real-time system metrics (Doctors, Patients, Appointments overview)
 * - Navigation to Manage Doctors, Schedules, Patients, and Appointments
 * - Quick recent appointments table
 */
public class AdminDashboard extends JFrame {

    private JLabel lblTotalDoctors;
    private JLabel lblTotalPatients;
    private JLabel lblTotalAppts;
    private JLabel lblActiveAppts;
    private JLabel lblDelayedAppts;
    private JLabel lblCompletedAppts;

    private JTable recentTable;
    private DefaultTableModel model;

    public AdminDashboard() {
        initComponents();
        loadOverviewMetrics();
        loadRecentAppointments();
    }

    private void initComponents() {
        setTitle("MediBook - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel lblTitle = new JLabel("🛡️ Administrator Dashboard");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);

        JButton btnLogout = new JButton("🚪 Logout");
        UITheme.styleDangerButton(btnLogout);
        btnLogout.addActionListener(e -> logout());
        rightHeader.add(btnLogout);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(rightHeader, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(UITheme.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. TOP: Overview Metrics Cards
        JPanel metricsPanel = new JPanel(new GridLayout(1, 6, 10, 0));
        metricsPanel.setOpaque(false);

        lblTotalDoctors = new JLabel("0", SwingConstants.CENTER);
        lblTotalPatients = new JLabel("0", SwingConstants.CENTER);
        lblTotalAppts = new JLabel("0", SwingConstants.CENTER);
        lblActiveAppts = new JLabel("0", SwingConstants.CENTER);
        lblDelayedAppts = new JLabel("0", SwingConstants.CENTER);
        lblCompletedAppts = new JLabel("0", SwingConstants.CENTER);

        metricsPanel.add(createMetricCard("Doctors", lblTotalDoctors, UITheme.PRIMARY_COLOR));
        metricsPanel.add(createMetricCard("Patients", lblTotalPatients, UITheme.ACCENT_TEAL));
        metricsPanel.add(createMetricCard("Total Appts", lblTotalAppts, new Color(71, 85, 105)));
        metricsPanel.add(createMetricCard("Scheduled", lblActiveAppts, UITheme.INFO_BLUE));
        metricsPanel.add(createMetricCard("Delayed", lblDelayedAppts, UITheme.WARNING_AMBER));
        metricsPanel.add(createMetricCard("Completed", lblCompletedAppts, UITheme.SUCCESS_GREEN));

        mainPanel.add(metricsPanel, BorderLayout.NORTH);

        // 2. CENTER: Navigation & Recent Table Split
        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setOpaque(false);

        // LEFT: Navigation Shortcuts Card
        JPanel navCard = UITheme.createCard();
        navCard.setPreferredSize(new Dimension(280, 420));
        navCard.setLayout(new BoxLayout(navCard, BoxLayout.Y_AXIS));

        JLabel lblNav = new JLabel("System Management");
        lblNav.setFont(UITheme.FONT_SECTION);
        lblNav.setForeground(UITheme.PRIMARY_COLOR);
        lblNav.setAlignmentX(Component.LEFT_ALIGNMENT);
        navCard.add(lblNav);
        navCard.add(Box.createVerticalStrut(16));

        JButton btnManageDoctors = new JButton("🩺 Manage Doctors");
        UITheme.stylePrimaryButton(btnManageDoctors);
        btnManageDoctors.setMaximumSize(new Dimension(250, 40));
        btnManageDoctors.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnManageDoctors.addActionListener(e -> {
            new ManageDoctors().setVisible(true);
            dispose();
        });

        JButton btnManageSchedules = new JButton("📅 Manage Schedules");
        UITheme.styleTealButton(btnManageSchedules);
        btnManageSchedules.setMaximumSize(new Dimension(250, 40));
        btnManageSchedules.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnManageSchedules.addActionListener(e -> {
            new ManageDoctorSchedule().setVisible(true);
            dispose();
        });

        JButton btnViewPatients = new JButton("👥 View Patients");
        UITheme.styleSecondaryButton(btnViewPatients);
        btnViewPatients.setMaximumSize(new Dimension(250, 40));
        btnViewPatients.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnViewPatients.addActionListener(e -> {
            new ViewPatients().setVisible(true);
            dispose();
        });

        JButton btnViewAppts = new JButton("📋 View All Appointments");
        UITheme.styleSecondaryButton(btnViewAppts);
        btnViewAppts.setMaximumSize(new Dimension(250, 40));
        btnViewAppts.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnViewAppts.addActionListener(e -> {
            new ViewAppoinments().setVisible(true);
            dispose();
        });

        JButton btnRefreshAll = new JButton("🔄 Refresh Metrics");
        UITheme.styleSecondaryButton(btnRefreshAll);
        btnRefreshAll.setMaximumSize(new Dimension(250, 40));
        btnRefreshAll.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRefreshAll.addActionListener(e -> {
            loadOverviewMetrics();
            loadRecentAppointments();
        });

        navCard.add(btnManageDoctors);
        navCard.add(Box.createVerticalStrut(12));
        navCard.add(btnManageSchedules);
        navCard.add(Box.createVerticalStrut(12));
        navCard.add(btnViewPatients);
        navCard.add(Box.createVerticalStrut(12));
        navCard.add(btnViewAppts);
        navCard.add(Box.createVerticalStrut(12));
        navCard.add(btnRefreshAll);
        navCard.add(Box.createVerticalGlue());

        centerPanel.add(navCard, BorderLayout.WEST);

        // RIGHT: Recent Appointments Table Card
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JLabel lblRecent = new JLabel("📊 Appointment Overview (Recent)");
        lblRecent.setFont(UITheme.FONT_SECTION);
        lblRecent.setForeground(UITheme.PRIMARY_COLOR);
        tableCard.add(lblRecent, BorderLayout.NORTH);

        String[] cols = {"ID", "Patient", "Doctor", "Date", "Time", "Status", "Delay Notes"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        recentTable = new JTable(model);
        UITheme.styleTable(recentTable);

        recentTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        recentTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        recentTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        recentTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        recentTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        recentTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        recentTable.getColumnModel().getColumn(6).setPreferredWidth(120);

        JScrollPane sp = new JScrollPane(recentTable);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(sp, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(3, 0, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(10, 8, 10, 8)
            )
        ));
        card.setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void loadOverviewMetrics() {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            // Doctors
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM doctors")) {
                if (rs.next()) lblTotalDoctors.setText(String.valueOf(rs.getInt(1)));
            }

            // Patients
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM patients")) {
                if (rs.next()) lblTotalPatients.setText(String.valueOf(rs.getInt(1)));
            }

            // Total Appts
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM appointments")) {
                if (rs.next()) lblTotalAppts.setText(String.valueOf(rs.getInt(1)));
            }

            // Scheduled
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM appointments WHERE status = 'Scheduled'")) {
                if (rs.next()) lblActiveAppts.setText(String.valueOf(rs.getInt(1)));
            }

            // Delayed / Rescheduled
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM appointments WHERE status IN ('Delayed', 'Rescheduled')")) {
                if (rs.next()) lblDelayedAppts.setText(String.valueOf(rs.getInt(1)));
            }

            // Completed
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM appointments WHERE status = 'Completed'")) {
                if (rs.next()) lblCompletedAppts.setText(String.valueOf(rs.getInt(1)));
            }

        } catch (SQLException ex) {
            System.err.println("Error loading admin metrics: " + ex.getMessage());
        }
    }

    public void loadRecentAppointments() {
        model.setRowCount(0);
        String sql = "SELECT a.appointment_id, p.name AS pat_name, d.name AS doc_name, "
                + "a.appointment_date, a.appointment_time, a.status, a.delay_reason "
                + "FROM appointments a "
                + "JOIN patients p ON a.patient_id = p.patient_id "
                + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                + "ORDER BY a.appointment_id DESC LIMIT 15";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("appointment_id"),
                        rs.getString("pat_name"),
                        rs.getString("doc_name"),
                        rs.getDate("appointment_date").toString(),
                        rs.getTime("appointment_time").toString().substring(0, 5),
                        rs.getString("status"),
                        rs.getString("delay_reason") != null ? rs.getString("delay_reason") : "-"
                    });
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error loading recent appointments: " + ex.getMessage());
        }
    }

    private void logout() {
        new MainMenu().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}
