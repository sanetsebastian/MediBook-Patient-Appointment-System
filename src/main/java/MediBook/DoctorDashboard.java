package MediBook;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * DoctorDashboard provides a professional medical management interface for doctors.
 * Features:
 * - View today's and upcoming appointments
 * - View complete patient details related to appointments
 * - Update appointment status (Completed, Cancelled, Scheduled)
 * - Report delays with reasons, delay minutes, and expected times
 * - Reschedule affected appointments and send automatic patient notifications
 */
public class DoctorDashboard extends JFrame {

    private final int doctorId;
    private final String doctorName;

    private JTable appointmentsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterBox;
    private JLabel lblMetrics;

    public DoctorDashboard(int doctorId, String doctorName) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;

        initComponents();
        loadAppointments();
    }

    private void initComponents() {
        setTitle("MediBook - Doctor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1020, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel lblTitle = new JLabel("🩺 " + doctorName);
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

        // Center Content
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setBackground(UITheme.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Control / Filter Card
        JPanel topCard = UITheme.createCard();
        topCard.setLayout(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("View Appointments:");
        lblFilter.setFont(UITheme.FONT_BOLD);
        lblFilter.setForeground(UITheme.TEXT_PRIMARY);

        filterBox = new JComboBox<>(new String[]{"Today's Appointments", "Upcoming Appointments", "All Appointments"});
        filterBox.setFont(UITheme.FONT_REGULAR);
        filterBox.setBackground(Color.WHITE);
        filterBox.addActionListener(e -> loadAppointments());

        filterPanel.add(lblFilter);
        filterPanel.add(filterBox);

        lblMetrics = new JLabel(" ");
        lblMetrics.setFont(UITheme.FONT_BOLD);
        lblMetrics.setForeground(UITheme.PRIMARY_COLOR);

        topCard.add(filterPanel, BorderLayout.WEST);
        topCard.add(lblMetrics, BorderLayout.EAST);
        mainPanel.add(topCard, BorderLayout.NORTH);

        // Appointments Table Card
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout());

        String[] columns = {
            "Appt ID", "Patient ID", "Patient Name", "Date", "Original Time", "Expected Time", "Status", "Delay Minutes", "Reason"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        appointmentsTable = new JTable(tableModel);
        UITheme.styleTable(appointmentsTable);

        appointmentsTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        appointmentsTable.getColumnModel().getColumn(1).setPreferredWidth(65);
        appointmentsTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        appointmentsTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        appointmentsTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        appointmentsTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        appointmentsTable.getColumnModel().getColumn(6).setPreferredWidth(95);
        appointmentsTable.getColumnModel().getColumn(7).setPreferredWidth(85);
        appointmentsTable.getColumnModel().getColumn(8).setPreferredWidth(140);

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(tableCard, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomCard = UITheme.createCard();
        bottomCard.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton btnViewAppt = new JButton("👁️ View Appointment");
        UITheme.stylePrimaryButton(btnViewAppt);
        btnViewAppt.addActionListener(e -> viewPatientDetails());

        JButton btnUpdateStatus = new JButton("✔️ Complete / Update Status");
        UITheme.styleSuccessButton(btnUpdateStatus);
        btnUpdateStatus.addActionListener(e -> updateStatusDialog());

        JButton btnReportDelay = new JButton("⚠️ Report Delay");
        UITheme.styleButton(btnReportDelay, UITheme.WARNING_AMBER, Color.WHITE);
        btnReportDelay.addActionListener(e -> reportDelayDialog());

        JButton btnReschedule = new JButton("🔄 Reschedule");
        UITheme.styleTealButton(btnReschedule);
        btnReschedule.addActionListener(e -> rescheduleDialog());

        JButton btnRefresh = new JButton("🔄 Refresh");
        UITheme.styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadAppointments());

        bottomCard.add(btnViewAppt);
        bottomCard.add(btnUpdateStatus);
        bottomCard.add(btnReportDelay);
        bottomCard.add(btnReschedule);
        bottomCard.add(btnRefresh);

        mainPanel.add(bottomCard, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    public void loadAppointments() {
        tableModel.setRowCount(0);
        String filter = (String) filterBox.getSelectedItem();

        String whereClause = "WHERE a.doctor_id = ? ";
        if ("Today's Appointments".equals(filter)) {
            whereClause += "AND a.appointment_date = CURRENT_DATE() ";
        } else if ("Upcoming Appointments".equals(filter)) {
            whereClause += "AND a.appointment_date >= CURRENT_DATE() ";
        }

        String sql = "SELECT a.appointment_id, a.patient_id, p.name AS patient_name, "
                + "a.appointment_date, a.appointment_time, a.expected_time, "
                + "a.status, a.delay_minutes, a.delay_reason "
                + "FROM appointments a "
                + "JOIN patients p ON a.patient_id = p.patient_id "
                + whereClause
                + "ORDER BY a.appointment_date ASC, a.appointment_time ASC";

        int totalCount = 0;
        int scheduledCount = 0;

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, doctorId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        totalCount++;
                        int apptId = rs.getInt("appointment_id");
                        int patId = rs.getInt("patient_id");
                        String patName = rs.getString("patient_name");
                        String date = rs.getDate("appointment_date").toString();
                        String time = rs.getTime("appointment_time").toString().substring(0, 5);
                        Time expTime = rs.getTime("expected_time");
                        String exp = expTime != null ? expTime.toString().substring(0, 5) : "-";
                        String status = rs.getString("status");
                        int delayMin = rs.getInt("delay_minutes");
                        String reason = rs.getString("delay_reason");
                        if (reason == null) reason = "-";

                        if ("Scheduled".equalsIgnoreCase(status) || "Delayed".equalsIgnoreCase(status) || "Rescheduled".equalsIgnoreCase(status)) {
                            scheduledCount++;
                        }

                        tableModel.addRow(new Object[]{
                            apptId, patId, patName, date, time, exp, status, (delayMin > 0 ? delayMin + " min" : "-"), reason
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + ex.getMessage());
        }

        lblMetrics.setText("Total: " + totalCount + " | Active: " + scheduledCount);
    }

    private int getSelectedAppointmentId() {
        int row = appointmentsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        return (int) appointmentsTable.getValueAt(row, 0);
    }

    private void viewPatientDetails() {
        int apptId = getSelectedAppointmentId();
        if (apptId == -1) return;

        String sql = "SELECT a.appointment_id, a.appointment_date, a.appointment_time, a.expected_time, "
                + "a.status, a.delay_minutes, a.delay_reason, "
                + "p.patient_id, p.name, p.age, p.gender, p.phone, p.email, p.address "
                + "FROM appointments a "
                + "JOIN patients p ON a.patient_id = p.patient_id "
                + "WHERE a.appointment_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, apptId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("📋 APPOINTMENT DETAILS\n");
                        sb.append("Appointment ID: ").append(rs.getInt("appointment_id")).append("\n");
                        sb.append("Date: ").append(rs.getDate("appointment_date")).append("\n");
                        sb.append("Original Time: ").append(rs.getTime("appointment_time").toString().substring(0, 5)).append("\n");
                        Time expTime = rs.getTime("expected_time");
                        sb.append("Expected Time: ").append(expTime != null ? expTime.toString().substring(0, 5) : "On Schedule").append("\n");
                        sb.append("Status: ").append(rs.getString("status")).append("\n");
                        sb.append("Delay: ").append(rs.getInt("delay_minutes")).append(" mins\n");
                        sb.append("Delay Reason: ").append(rs.getString("delay_reason") != null ? rs.getString("delay_reason") : "None").append("\n\n");

                        sb.append("👤 PATIENT DETAILS\n");
                        sb.append("Patient ID: ").append(rs.getInt("patient_id")).append("\n");
                        sb.append("Full Name: ").append(rs.getString("name")).append("\n");
                        sb.append("Age: ").append(rs.getInt("age")).append(" years\n");
                        sb.append("Gender: ").append(rs.getString("gender")).append("\n");
                        sb.append("Phone: ").append(rs.getString("phone")).append("\n");
                        sb.append("Email: ").append(rs.getString("email")).append("\n");
                        sb.append("Address: ").append(rs.getString("address")).append("\n");

                        JTextArea area = new JTextArea(sb.toString());
                        area.setEditable(false);
                        area.setFont(UITheme.FONT_REGULAR);
                        area.setBorder(new EmptyBorder(10, 10, 10, 10));

                        JScrollPane sp = new JScrollPane(area);
                        sp.setPreferredSize(new Dimension(420, 320));
                        JOptionPane.showMessageDialog(this, sp, "Appointment & Patient Details", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching details: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStatusDialog() {
        int apptId = getSelectedAppointmentId();
        if (apptId == -1) return;

        int row = appointmentsTable.getSelectedRow();
        String currentStatus = (String) appointmentsTable.getValueAt(row, 6);
        int patientId = (int) appointmentsTable.getValueAt(row, 1);
        String patientName = (String) appointmentsTable.getValueAt(row, 2);

        String[] options = {"Completed", "Cancelled", "Scheduled"};
        String selectedStatus = (String) JOptionPane.showInputDialog(
                this,
                "Select new status for Appointment #" + apptId + " (Patient: " + patientName + "):",
                "Update Appointment Status",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                currentStatus
        );

        if (selectedStatus == null || selectedStatus.equals(currentStatus)) return;

        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ? AND doctor_id = ?";
        String notifSql = "INSERT INTO notifications (patient_id, appointment_id, message, is_read, notification_date) "
                + "VALUES (?, ?, ?, FALSE, CURRENT_TIMESTAMP)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, selectedStatus);
                ps.setInt(2, apptId);
                ps.setInt(3, doctorId);
                ps.executeUpdate();

                // Send status update notification to patient
                String notifMsg = "🔔 Appointment Status Update\n"
                        + "Your appointment with Dr. " + doctorName + " has been marked as: " + selectedStatus + ".";
                try (PreparedStatement psN = con.prepareStatement(notifSql)) {
                    psN.setInt(1, patientId);
                    psN.setInt(2, apptId);
                    psN.setString(3, notifMsg);
                    psN.executeUpdate();
                }

                JOptionPane.showMessageDialog(this, "Status updated to '" + selectedStatus + "'. Patient notified.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadAppointments();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reportDelayDialog() {
        int apptId = getSelectedAppointmentId();
        if (apptId == -1) return;

        int row = appointmentsTable.getSelectedRow();
        int patientId = (int) appointmentsTable.getValueAt(row, 1);
        String apptDateStr = (String) appointmentsTable.getValueAt(row, 3);
        String origTimeStr = (String) appointmentsTable.getValueAt(row, 4);

        // Input Delay Reason
        String reason = JOptionPane.showInputDialog(this,
                "Enter delay reason (e.g. Heavy rainfall, Emergency consultation, Traffic delay):",
                "Report Delay",
                JOptionPane.QUESTION_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) return;

        // Input Delay Minutes
        String minStr = JOptionPane.showInputDialog(this,
                "Enter delay in minutes (e.g. 30):",
                "30");
        if (minStr == null || minStr.trim().isEmpty()) return;

        int delayMinutes;
        try {
            delayMinutes = Integer.parseInt(minStr.trim());
            if (delayMinutes <= 0) {
                JOptionPane.showMessageDialog(this, "Delay minutes must be greater than 0.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number of minutes.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Calculate expected time
        LocalTime origTime = LocalTime.parse(origTimeStr);
        LocalTime expTime = origTime.plusMinutes(delayMinutes);
        String expTimeStr = expTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        // Prompt expected time verification
        String confirmedExpTime = (String) JOptionPane.showInputDialog(this,
                "Calculated Expected Time is " + expTimeStr + ".\nYou may confirm or adjust expected time (HH:mm):",
                "Expected Time",
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                expTimeStr);
        if (confirmedExpTime == null || confirmedExpTime.trim().isEmpty()) return;

        try {
            expTime = LocalTime.parse(confirmedExpTime.trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid time format. Please use HH:mm.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int notifySubsequent = JOptionPane.showConfirmDialog(this,
                "Would you like to also automatically notify subsequent scheduled patients today about this delay?",
                "Notify Subsequent Patients",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            // Update selected appointment
            String updateSql = "UPDATE appointments SET "
                    + "status = 'Delayed', delay_status = 'Delayed', "
                    + "delay_minutes = ?, expected_time = ?, delay_reason = ? "
                    + "WHERE appointment_id = ? AND doctor_id = ?";

            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setInt(1, delayMinutes);
                ps.setTime(2, Time.valueOf(expTime));
                ps.setString(3, reason.trim());
                ps.setInt(4, apptId);
                ps.setInt(5, doctorId);
                ps.executeUpdate();
            }

            // Send notification to selected patient
            String notifSql = "INSERT INTO notifications (patient_id, appointment_id, message, is_read, notification_date) "
                    + "VALUES (?, ?, ?, FALSE, CURRENT_TIMESTAMP)";

            String patientMsg = "⚠️ Appointment Delay Notification\n"
                    + "Doctor: " + doctorName + "\n"
                    + "Date: " + apptDateStr + "\n"
                    + "Original Time: " + origTimeStr + "\n"
                    + "Expected Time: " + expTime.toString().substring(0, 5) + "\n"
                    + "Delay: " + delayMinutes + " minutes\n"
                    + "Reason: " + reason.trim() + "\n\n"
                    + "Please adjust your schedule and arrive at " + expTime.toString().substring(0, 5) + ".";

            try (PreparedStatement psN = con.prepareStatement(notifSql)) {
                psN.setInt(1, patientId);
                psN.setInt(2, apptId);
                psN.setString(3, patientMsg);
                psN.executeUpdate();
            }

            // Notify subsequent patients if requested
            if (notifySubsequent == JOptionPane.YES_OPTION) {
                String subQuery = "SELECT appointment_id, patient_id, appointment_time FROM appointments "
                        + "WHERE doctor_id = ? AND appointment_date = ? AND appointment_id <> ? "
                        + "AND appointment_time >= ? AND status = 'Scheduled'";

                try (PreparedStatement psSub = con.prepareStatement(subQuery)) {
                    psSub.setInt(1, doctorId);
                    psSub.setDate(2, Date.valueOf(LocalDate.parse(apptDateStr)));
                    psSub.setInt(3, apptId);
                    psSub.setTime(4, Time.valueOf(origTime));

                    try (ResultSet rsSub = psSub.executeQuery()) {
                        while (rsSub.next()) {
                            int subApptId = rsSub.getInt("appointment_id");
                            int subPatId = rsSub.getInt("patient_id");
                            Time subOrigTime = rsSub.getTime("appointment_time");
                            LocalTime subExpTime = subOrigTime.toLocalTime().plusMinutes(delayMinutes);

                            // Update subsequent appointment
                            String updateSubSql = "UPDATE appointments SET "
                                    + "status = 'Delayed', delay_status = 'Delayed', "
                                    + "delay_minutes = ?, expected_time = ?, delay_reason = ? "
                                    + "WHERE appointment_id = ?";
                            try (PreparedStatement psUpSub = con.prepareStatement(updateSubSql)) {
                                psUpSub.setInt(1, delayMinutes);
                                psUpSub.setTime(2, Time.valueOf(subExpTime));
                                psUpSub.setString(3, reason.trim());
                                psUpSub.setInt(4, subApptId);
                                psUpSub.executeUpdate();
                            }

                            // Notification for subsequent patient
                            String subMsg = "⚠️ Doctor Delay Notice\n"
                                    + "Doctor: " + doctorName + "\n"
                                    + "Date: " + apptDateStr + "\n"
                                    + "Original Time: " + subOrigTime.toString().substring(0, 5) + "\n"
                                    + "Expected Time: " + subExpTime.toString().substring(0, 5) + "\n"
                                    + "Delay: " + delayMinutes + " minutes\n"
                                    + "Reason: " + reason.trim() + "\n\n"
                                    + "The doctor is delayed today. Please adjust your arrival time.";

                            try (PreparedStatement psSubNotif = con.prepareStatement(notifSql)) {
                                psSubNotif.setInt(1, subPatId);
                                psSubNotif.setInt(2, subApptId);
                                psSubNotif.setString(3, subMsg);
                                psSubNotif.executeUpdate();
                            }
                        }
                    }
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Appointment delayed. Patient notification sent.",
                    "Delay Reported",
                    JOptionPane.INFORMATION_MESSAGE);

            loadAppointments();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rescheduleDialog() {
        int apptId = getSelectedAppointmentId();
        if (apptId == -1) return;

        int row = appointmentsTable.getSelectedRow();
        int patientId = (int) appointmentsTable.getValueAt(row, 1);
        String origDate = (String) appointmentsTable.getValueAt(row, 3);
        String origTime = (String) appointmentsTable.getValueAt(row, 4);

        JTextField newDateField = new JTextField(origDate, 10);
        JTextField newTimeField = new JTextField(origTime, 6);
        JTextField reasonField = new JTextField("Doctor Rescheduled", 15);

        Object[] fields = {
            "Original Date / Time: " + origDate + " at " + origTime,
            "New Date (YYYY-MM-DD):", newDateField,
            "New Time (HH:MM):", newTimeField,
            "Rescheduling Reason:", reasonField
        };

        int result = JOptionPane.showConfirmDialog(this, fields, "Reschedule Appointment",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        String newDateStr = newDateField.getText().trim();
        String newTimeStr = newTimeField.getText().trim();
        String reasonStr = reasonField.getText().trim();

        if (newDateStr.isEmpty() || newTimeStr.isEmpty() || reasonStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate newDate;
        LocalTime newTime;
        try {
            newDate = LocalDate.parse(newDateStr);
            newTime = LocalTime.parse(newTimeStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date or Time format. Use YYYY-MM-DD and HH:MM.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (newDate.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Cannot reschedule to a past date.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Conflict check for doctor on new date/time
        String conflictSql = "SELECT COUNT(*) FROM appointments "
                + "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? "
                + "AND appointment_id <> ? AND status IN ('Scheduled', 'Delayed', 'Rescheduled')";

        String updateSql = "UPDATE appointments SET "
                + "appointment_date = ?, appointment_time = ?, expected_time = ?, "
                + "status = 'Rescheduled', delay_status = 'Rescheduled', delay_reason = ? "
                + "WHERE appointment_id = ? AND doctor_id = ?";

        String notifSql = "INSERT INTO notifications (patient_id, appointment_id, message, is_read, notification_date) "
                + "VALUES (?, ?, ?, FALSE, CURRENT_TIMESTAMP)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            try (PreparedStatement checkPs = con.prepareStatement(conflictSql)) {
                checkPs.setInt(1, doctorId);
                checkPs.setDate(2, Date.valueOf(newDate));
                checkPs.setTime(3, Time.valueOf(newTime));
                checkPs.setInt(4, apptId);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this,
                                "You already have another booked appointment at that date and time.\nPlease choose another time.",
                                "Conflict Detected",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setDate(1, Date.valueOf(newDate));
                ps.setTime(2, Time.valueOf(newTime));
                ps.setTime(3, Time.valueOf(newTime));
                ps.setString(4, reasonStr);
                ps.setInt(5, apptId);
                ps.setInt(6, doctorId);
                ps.executeUpdate();
            }

            String notifMsg = "🔄 Appointment Rescheduled\n"
                    + "Doctor: " + doctorName + "\n"
                    + "New Date: " + newDateStr + "\n"
                    + "New Time: " + newTimeStr + "\n"
                    + "Reason: " + reasonStr + "\n\n"
                    + "Please arrive at " + newTimeStr + " on " + newDateStr + ".";

            try (PreparedStatement psN = con.prepareStatement(notifSql)) {
                psN.setInt(1, patientId);
                psN.setInt(2, apptId);
                psN.setString(3, notifMsg);
                psN.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Appointment rescheduled successfully!\nPatient notification sent.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            loadAppointments();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error rescheduling appointment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        new MainMenu().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new DoctorDashboard(2, "Dr. Priya Nair").setVisible(true));
    }
}
