package MediBook;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * ViewAppoinments provides administrators with full visibility over all appointments
 * across all doctors and patients, with filtering by status and doctor.
 */
public class ViewAppoinments extends JFrame {

    private JTable apptTable;
    private DefaultTableModel model;
    private JComboBox<String> statusFilter;
    private JComboBox<String> doctorFilter;
    private JLabel lblCount;

    public ViewAppoinments() {
        initComponents();
        loadDoctorsFilter();
        loadAppointments();
    }

    private void initComponents() {
        setTitle("MediBook - All Appointments");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(980, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("All Appointments", "System Overview"), BorderLayout.NORTH);

        // Content
        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(UITheme.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Filter Bar Card
        JPanel filterCard = UITheme.createCard();
        filterCard.setLayout(new BorderLayout());

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftFilters.setOpaque(false);

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.TEXT_PRIMARY);

        statusFilter = new JComboBox<>(new String[]{"All Statuses", "Scheduled", "Delayed", "Rescheduled", "Completed", "Cancelled"});
        statusFilter.setFont(UITheme.FONT_REGULAR);
        statusFilter.setBackground(Color.WHITE);
        statusFilter.addActionListener(e -> loadAppointments());

        JLabel lblDoc = new JLabel("Doctor:");
        lblDoc.setFont(UITheme.FONT_BOLD);
        lblDoc.setForeground(UITheme.TEXT_PRIMARY);

        doctorFilter = new JComboBox<>();
        doctorFilter.setFont(UITheme.FONT_REGULAR);
        doctorFilter.setBackground(Color.WHITE);
        doctorFilter.addItem("All Doctors");
        doctorFilter.addActionListener(e -> loadAppointments());

        leftFilters.add(lblStatus);
        leftFilters.add(statusFilter);
        leftFilters.add(lblDoc);
        leftFilters.add(doctorFilter);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        lblCount = new JLabel("Total: 0");
        lblCount.setFont(UITheme.FONT_BOLD);
        lblCount.setForeground(UITheme.PRIMARY_COLOR);

        JButton refreshBtn = new JButton("🔄 Refresh");
        UITheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadAppointments());

        JButton backBtn = new JButton("Back to Dashboard");
        UITheme.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> {
            new AdminDashboard().setVisible(true);
            dispose();
        });

        rightActions.add(lblCount);
        rightActions.add(refreshBtn);
        rightActions.add(backBtn);

        filterCard.add(leftFilters, BorderLayout.WEST);
        filterCard.add(rightActions, BorderLayout.EAST);
        mainPanel.add(filterCard, BorderLayout.NORTH);

        // Appointments Table Card
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {
            "ID", "Patient", "Doctor", "Specialization", "Date", "Original Time", "Expected Time", "Status", "Delay Notes"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        apptTable = new JTable(model);
        UITheme.styleTable(apptTable);

        apptTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        apptTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        apptTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        apptTable.getColumnModel().getColumn(3).setPreferredWidth(110);
        apptTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        apptTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        apptTable.getColumnModel().getColumn(6).setPreferredWidth(85);
        apptTable.getColumnModel().getColumn(7).setPreferredWidth(95);
        apptTable.getColumnModel().getColumn(8).setPreferredWidth(130);

        JScrollPane sp = new JScrollPane(apptTable);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(sp, BorderLayout.CENTER);

        mainPanel.add(tableCard, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void loadDoctorsFilter() {
        String sql = "SELECT doctor_id, name FROM doctors ORDER BY name ASC";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    doctorFilter.addItem(rs.getInt("doctor_id") + " - " + rs.getString("name"));
                }
            }
        } catch (SQLException ignored) {}
    }

    public void loadAppointments() {
        model.setRowCount(0);

        String statusSel = (String) statusFilter.getSelectedItem();
        String docSel = (String) doctorFilter.getSelectedItem();

        StringBuilder sql = new StringBuilder(
            "SELECT a.appointment_id, p.name AS patient_name, d.name AS doc_name, d.specialization, "
            + "a.appointment_date, a.appointment_time, a.expected_time, a.status, a.delay_reason "
            + "FROM appointments a "
            + "JOIN patients p ON a.patient_id = p.patient_id "
            + "JOIN doctors d ON a.doctor_id = d.doctor_id "
            + "WHERE 1=1 "
        );

        if (statusSel != null && !statusSel.equals("All Statuses")) {
            sql.append("AND a.status = ? ");
        }

        int docId = -1;
        if (docSel != null && !docSel.equals("All Doctors")) {
            try {
                docId = Integer.parseInt(docSel.split(" - ")[0]);
                sql.append("AND a.doctor_id = ? ");
            } catch (Exception ignored) {}
        }

        sql.append("ORDER BY a.appointment_date DESC, a.appointment_time DESC");

        int count = 0;
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                int paramIdx = 1;
                if (statusSel != null && !statusSel.equals("All Statuses")) {
                    ps.setString(paramIdx++, statusSel);
                }
                if (docId > 0) {
                    ps.setInt(paramIdx++, docId);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        count++;
                        int id = rs.getInt("appointment_id");
                        String pat = rs.getString("patient_name");
                        String doc = rs.getString("doc_name");
                        String spec = rs.getString("specialization");
                        String date = rs.getDate("appointment_date").toString();
                        String time = rs.getTime("appointment_time").toString().substring(0, 5);
                        Time expTime = rs.getTime("expected_time");
                        String exp = expTime != null ? expTime.toString().substring(0, 5) : "-";
                        String status = rs.getString("status");
                        String reason = rs.getString("delay_reason");
                        if (reason == null) reason = "-";

                        model.addRow(new Object[]{id, pat, doc, spec, date, time, exp, status, reason});
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + ex.getMessage());
        }

        lblCount.setText("Appointments: " + count);
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new ViewAppoinments().setVisible(true));
    }
}
