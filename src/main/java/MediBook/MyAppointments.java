package MediBook;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * MyAppointments provides the dedicated view of a patient's booked appointments,
 * status tracking, and cancellation capability.
 */
public class MyAppointments extends JDialog {

    private final int patientId;
    private final String patientName;
    private final Runnable onUpdate;
    private JTable table;
    private DefaultTableModel model;

    public MyAppointments(Frame parent, int patientId, String patientName, Runnable onUpdate) {
        super(parent, "My Appointments", true);
        this.patientId = patientId;
        this.patientName = patientName;
        this.onUpdate = onUpdate;

        initComponents();
        loadAppointments();
    }

    private void initComponents() {
        setSize(840, 520);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("My Appointments", "Patient: " + patientName), BorderLayout.NORTH);

        // Center Panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(UITheme.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Table
        String[] columns = {"ID", "Doctor", "Specialization", "Date", "Original Time", "Expected Time", "Status", "Delay Notes"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        UITheme.styleTable(table);

        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(85);
        table.getColumnModel().getColumn(5).setPreferredWidth(85);
        table.getColumnModel().getColumn(6).setPreferredWidth(95);
        table.getColumnModel().getColumn(7).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel Appointment");
        UITheme.styleDangerButton(cancelBtn);
        cancelBtn.addActionListener(e -> cancelSelectedAppointment());

        JButton refreshBtn = new JButton("Refresh");
        UITheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadAppointments());

        JButton closeBtn = new JButton("Close");
        UITheme.styleSecondaryButton(closeBtn);
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(cancelBtn);
        btnPanel.add(refreshBtn);
        btnPanel.add(closeBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    public void loadAppointments() {
        model.setRowCount(0);
        String sql = "SELECT a.appointment_id, d.name AS doc_name, d.specialization, "
                + "a.appointment_date, a.appointment_time, a.expected_time, a.status, a.delay_reason "
                + "FROM appointments a "
                + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                + "WHERE a.patient_id = ? "
                + "ORDER BY a.appointment_date ASC, a.appointment_time ASC";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("appointment_id");
                        String doc = rs.getString("doc_name");
                        String spec = rs.getString("specialization");
                        String date = rs.getDate("appointment_date").toString();
                        String time = rs.getTime("appointment_time").toString().substring(0, 5);
                        Time expTime = rs.getTime("expected_time");
                        String exp = expTime != null ? expTime.toString().substring(0, 5) : "-";
                        String status = rs.getString("status");
                        String reason = rs.getString("delay_reason");
                        if (reason == null) reason = "-";

                        model.addRow(new Object[]{id, doc, spec, date, time, exp, status, reason});
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + ex.getMessage());
        }
    }

    private void cancelSelectedAppointment() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int apptId = (int) table.getValueAt(row, 0);
        String doc = (String) table.getValueAt(row, 1);
        String date = (String) table.getValueAt(row, 3);
        String status = (String) table.getValueAt(row, 6);

        if ("Completed".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "A completed appointment cannot be cancelled.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Cancelled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This appointment is already cancelled.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel your appointment with " + doc + " on " + date + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String cancelSql = "UPDATE appointments SET status = 'Cancelled' WHERE appointment_id = ? AND patient_id = ?";
        String notifSql = "INSERT INTO notifications (patient_id, appointment_id, message, is_read, notification_date) "
                + "VALUES (?, ?, ?, FALSE, CURRENT_TIMESTAMP)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(cancelSql)) {
                ps.setInt(1, apptId);
                ps.setInt(2, patientId);
                int updated = ps.executeUpdate();

                if (updated > 0) {
                    // Send cancellation notification
                    String notifMsg = "❌ Appointment Cancelled\n"
                            + "Your appointment with " + doc + " on " + date + " has been cancelled.";
                    try (PreparedStatement psNotif = con.prepareStatement(notifSql)) {
                        psNotif.setInt(1, patientId);
                        psNotif.setInt(2, apptId);
                        psNotif.setString(3, notifMsg);
                        psNotif.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(this, "Your appointment has been cancelled.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAppointments();
                    if (onUpdate != null) {
                        onUpdate.run();
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error cancelling appointment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
