package MediBook;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

/**
 * PatientDashboard provides the clean, central interface for patients.
 * Features:
 * - Patient greeting and overview
 * - Upcoming appointments table (chronological order)
 * - Quick appointment booking with conflict checking
 * - Unread notifications counter and notification modal
 * - Profile and credential management
 */
public class PatientDashboard extends JFrame {

    private final int patientId;
    private String patientName;

    // UI Components
    private JLabel lblWelcome;
    private JButton btnNotifications;
    private JTable appointmentsTable;
    private DefaultTableModel tableModel;

    // Quick Booking Components
    private JComboBox<String> doctorBox;
    private JTextField dateField;
    private JComboBox<String> timeBox;
    private JLabel lblDoctorScheduleInfo;

    // Background Timer for Reminders
    private javax.swing.Timer reminderTimer;

    public PatientDashboard(int patientId, String patientName) {
        this.patientId = patientId;
        this.patientName = patientName;

        initComponents();

        loadAppointments();
        updateNotificationBadge();
        loadDoctors();

        // 30-second background polling for notifications and reminders
        reminderTimer = new javax.swing.Timer(30000, e -> {
            updateNotificationBadge();
            loadAppointments();
        });
        reminderTimer.start();
    }

    private void initComponents() {
        setTitle("MediBook - Patient Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        lblWelcome = new JLabel("Welcome, " + patientName);
        lblWelcome.setFont(UITheme.FONT_TITLE);
        lblWelcome.setForeground(Color.WHITE);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setOpaque(false);

        btnNotifications = new JButton("🔔 Notifications");
        UITheme.styleTealButton(btnNotifications);
        btnNotifications.addActionListener(e -> openNotifications());

        JButton btnMyAppts = new JButton("📋 My Appointments");
        UITheme.styleSecondaryButton(btnMyAppts);
        btnMyAppts.addActionListener(e -> openMyAppointments());

        JButton btnProfile = new JButton("👤 Profile");
        UITheme.styleSecondaryButton(btnProfile);
        btnProfile.addActionListener(e -> openProfile());

        JButton btnLogout = new JButton("🚪 Logout");
        UITheme.styleDangerButton(btnLogout);
        btnLogout.addActionListener(e -> logout());

        navPanel.add(btnNotifications);
        navPanel.add(btnMyAppts);
        navPanel.add(btnProfile);
        navPanel.add(btnLogout);

        headerPanel.add(lblWelcome, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainContent = new JPanel(new BorderLayout(16, 16));
        mainContent.setBackground(UITheme.BG_LIGHT);
        mainContent.setBorder(new EmptyBorder(16, 20, 16, 20));

        // LEFT: Quick Booking Card
        JPanel bookCard = UITheme.createCard();
        bookCard.setPreferredSize(new Dimension(360, 520));
        bookCard.setLayout(new BoxLayout(bookCard, BoxLayout.Y_AXIS));

        JLabel lblBookTitle = new JLabel("📅 Book New Appointment");
        lblBookTitle.setFont(UITheme.FONT_SECTION);
        lblBookTitle.setForeground(UITheme.PRIMARY_COLOR);
        lblBookTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblBookSub = new JLabel("Select doctor, date, and available slot:");
        lblBookSub.setFont(UITheme.FONT_SMALL);
        lblBookSub.setForeground(UITheme.TEXT_MUTED);
        lblBookSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        bookCard.add(lblBookTitle);
        bookCard.add(Box.createVerticalStrut(4));
        bookCard.add(lblBookSub);
        bookCard.add(Box.createVerticalStrut(16));

        // Doctor
        JLabel lblDoc = new JLabel("Doctor:");
        lblDoc.setFont(UITheme.FONT_BOLD);
        lblDoc.setAlignmentX(Component.LEFT_ALIGNMENT);
        doctorBox = new JComboBox<>();
        doctorBox.setFont(UITheme.FONT_REGULAR);
        doctorBox.setBackground(Color.WHITE);
        doctorBox.setMaximumSize(new Dimension(340, 32));
        doctorBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDoctorScheduleInfo = new JLabel(" ");
        lblDoctorScheduleInfo.setFont(UITheme.FONT_SMALL);
        lblDoctorScheduleInfo.setForeground(UITheme.TEXT_MUTED);
        lblDoctorScheduleInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Date
        JLabel lblDate = new JLabel("Date (YYYY-MM-DD):");
        lblDate.setFont(UITheme.FONT_BOLD);
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);

        dateField = new JTextField();
        dateField.setEditable(false);
        dateField.setFont(UITheme.FONT_REGULAR);
        dateField.setBackground(Color.WHITE);
        dateField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1),
            new EmptyBorder(6, 8, 6, 8)
        ));

        JButton selectDateBtn = new JButton("Pick Date");
        UITheme.styleSecondaryButton(selectDateBtn);
        selectDateBtn.addActionListener(e -> openCalendarPicker());

        JPanel dateRow = new JPanel(new BorderLayout(6, 0));
        dateRow.setOpaque(false);
        dateRow.setMaximumSize(new Dimension(340, 32));
        dateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateRow.add(dateField, BorderLayout.CENTER);
        dateRow.add(selectDateBtn, BorderLayout.EAST);

        // Time Slot
        JLabel lblTime = new JLabel("Available Time Slot (09:00 - 19:30):");
        lblTime.setFont(UITheme.FONT_BOLD);
        lblTime.setAlignmentX(Component.LEFT_ALIGNMENT);

        timeBox = new JComboBox<>();
        timeBox.setFont(UITheme.FONT_REGULAR);
        timeBox.setBackground(Color.WHITE);
        timeBox.setMaximumSize(new Dimension(340, 32));
        timeBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        bookCard.add(lblDoc);
        bookCard.add(Box.createVerticalStrut(4));
        bookCard.add(doctorBox);
        bookCard.add(Box.createVerticalStrut(2));
        bookCard.add(lblDoctorScheduleInfo);
        bookCard.add(Box.createVerticalStrut(12));

        bookCard.add(lblDate);
        bookCard.add(Box.createVerticalStrut(4));
        bookCard.add(dateRow);
        bookCard.add(Box.createVerticalStrut(12));

        bookCard.add(lblTime);
        bookCard.add(Box.createVerticalStrut(4));
        bookCard.add(timeBox);
        bookCard.add(Box.createVerticalStrut(20));

        JButton btnBook = new JButton("Book Appointment");
        UITheme.stylePrimaryButton(btnBook);
        btnBook.setMaximumSize(new Dimension(340, 38));
        btnBook.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBook.addActionListener(e -> bookAppointment());

        bookCard.add(btnBook);
        bookCard.add(Box.createVerticalGlue());

        mainContent.add(bookCard, BorderLayout.WEST);

        // RIGHT: Appointments Table Card
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel lblApptTitle = new JLabel("📋 Upcoming & Recent Appointments");
        lblApptTitle.setFont(UITheme.FONT_SECTION);
        lblApptTitle.setForeground(UITheme.PRIMARY_COLOR);
        tableHeader.add(lblApptTitle, BorderLayout.WEST);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableActions.setOpaque(false);

        JButton btnCancel = new JButton("Cancel Selected");
        UITheme.styleDangerButton(btnCancel);
        btnCancel.addActionListener(e -> cancelSelectedAppointment());

        JButton btnRefresh = new JButton("🔄 Refresh");
        UITheme.styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadAppointments());

        tableActions.add(btnCancel);
        tableActions.add(btnRefresh);
        tableHeader.add(tableActions, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);

        // Table Model
        String[] columns = {"ID", "Doctor", "Specialization", "Date", "Time", "Expected", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        appointmentsTable = new JTable(tableModel);
        UITheme.styleTable(appointmentsTable);

        appointmentsTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        appointmentsTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        appointmentsTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        appointmentsTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        appointmentsTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        appointmentsTable.getColumnModel().getColumn(5).setPreferredWidth(75);
        appointmentsTable.getColumnModel().getColumn(6).setPreferredWidth(95);

        JScrollPane scrollTable = new JScrollPane(appointmentsTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(scrollTable, BorderLayout.CENTER);

        mainContent.add(tableCard, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        // Listeners for dynamic slots
        doctorBox.addActionListener(e -> {
            updateDoctorInfo();
            loadAvailableTimeSlots();
        });
    }

    private int getSelectedDoctorId() {
        Object item = doctorBox.getSelectedItem();
        if (item == null) return -1;
        try {
            return Integer.parseInt(item.toString().split(" - ")[0].trim());
        } catch (Exception ex) {
            return -1;
        }
    }

    private void loadDoctors() {
        doctorBox.removeAllItems();
        String sql = "SELECT doctor_id, name, specialization FROM doctors ORDER BY name ASC";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("doctor_id");
                    String name = rs.getString("name");
                    String spec = rs.getString("specialization");
                    doctorBox.addItem(id + " - " + name + " (" + spec + ")");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + ex.getMessage());
        }
        if (doctorBox.getItemCount() > 0) {
            doctorBox.setSelectedIndex(0);
        }
    }

    private void updateDoctorInfo() {
        int docId = getSelectedDoctorId();
        if (docId <= 0) {
            lblDoctorScheduleInfo.setText(" ");
            return;
        }
        String sql = "SELECT available_days FROM doctors WHERE doctor_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, docId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String days = rs.getString("available_days");
                        lblDoctorScheduleInfo.setText("Days: " + (days != null ? days : "Mon-Sat"));
                    }
                }
            }
        } catch (SQLException ignored) {}
    }

    private void openCalendarPicker() {
        int docId = getSelectedDoctorId();
        if (docId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CalendarDialog dialog = new CalendarDialog(this, dateField, docId);
        dialog.setVisible(true);
        loadAvailableTimeSlots();
    }

    private void loadAvailableTimeSlots() {
        timeBox.removeAllItems();
        int docId = getSelectedDoctorId();
        String dateText = dateField.getText().trim();

        if (docId <= 0 || dateText.isEmpty()) {
            timeBox.addItem("-- Select Doctor & Date First --");
            return;
        }

        LocalDate selectedDate;
        try {
            selectedDate = LocalDate.parse(dateText);
        } catch (Exception ex) {
            timeBox.addItem("-- Invalid Date --");
            return;
        }

        if (selectedDate.isBefore(LocalDate.now())) {
            timeBox.addItem("-- Past Dates Unavailable --");
            return;
        }

        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        java.util.List<String> slots = new ArrayList<>();

        String scheduleSql = "SELECT start_time, end_time, slot_duration FROM doctor_schedule "
                + "WHERE doctor_id = ? AND day_of_week = ? ORDER BY start_time ASC";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            boolean hasSchedule = false;
            try (PreparedStatement ps = con.prepareStatement(scheduleSql)) {
                ps.setInt(1, docId);
                ps.setString(2, dayOfWeek);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        hasSchedule = true;
                        LocalTime start = rs.getTime("start_time").toLocalTime();
                        LocalTime end = rs.getTime("end_time").toLocalTime();
                        int duration = rs.getInt("slot_duration");
                        if (duration <= 0) duration = 30;

                        LocalTime cur = start;
                        while (!cur.plusMinutes(duration).isAfter(end)) {
                            slots.add(cur.format(DateTimeFormatter.ofPattern("HH:mm")));
                            cur = cur.plusMinutes(duration);
                        }
                    }
                }
            }

            // Fallback: standard 09:00 - 19:30 range (30-min intervals)
            if (!hasSchedule) {
                LocalTime cur = LocalTime.of(9, 0);
                LocalTime end = LocalTime.of(19, 30);
                while (!cur.isAfter(end)) {
                    slots.add(cur.format(DateTimeFormatter.ofPattern("HH:mm")));
                    cur = cur.plusMinutes(30);
                }
            }

            // Query existing appointments to remove double-booked slots
            Set<String> bookedSlots = new HashSet<>();
            String bookedSql = "SELECT appointment_time FROM appointments "
                    + "WHERE doctor_id = ? AND appointment_date = ? "
                    + "AND status IN ('Scheduled', 'Delayed', 'Rescheduled')";

            try (PreparedStatement ps = con.prepareStatement(bookedSql)) {
                ps.setInt(1, docId);
                ps.setDate(2, java.sql.Date.valueOf(selectedDate));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Time t = rs.getTime("appointment_time");
                        bookedSlots.add(t.toString().substring(0, 5));
                    }
                }
            }

            boolean isToday = selectedDate.isEqual(LocalDate.now());
            LocalTime nowTime = LocalTime.now();

            int count = 0;
            for (String slot : slots) {
                if (bookedSlots.contains(slot)) continue;
                if (isToday) {
                    LocalTime t = LocalTime.parse(slot);
                    if (t.isBefore(nowTime)) continue;
                }
                timeBox.addItem(slot);
                count++;
            }

            if (count == 0) {
                timeBox.addItem("-- No Available Slots --");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching slots: " + ex.getMessage());
        }
    }

    private void bookAppointment() {
        int docId = getSelectedDoctorId();
        if (docId <= 0) {
            JOptionPane.showMessageDialog(this, "Please select a valid doctor.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String dateText = dateField.getText().trim();
        if (dateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an appointment date.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object selectedTimeObj = timeBox.getSelectedItem();
        if (selectedTimeObj == null || selectedTimeObj.toString().startsWith("--")) {
            JOptionPane.showMessageDialog(this, "Please select an available appointment time slot.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String timeText = selectedTimeObj.toString().trim();
        LocalDate apptDate = LocalDate.parse(dateText);
        LocalTime apptTime = LocalTime.parse(timeText);

        if (apptDate.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Please select a valid appointment date (past dates cannot be booked).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (apptDate.isEqual(LocalDate.now()) && apptTime.isBefore(LocalTime.now())) {
            JOptionPane.showMessageDialog(this, "Cannot book an appointment slot that has already passed today.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // DOUBLE-BOOKING CONFLICT CHECK
        String conflictCheckSql = "SELECT COUNT(*) FROM appointments "
                + "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? "
                + "AND status IN ('Scheduled', 'Delayed', 'Rescheduled')";

        String insertSql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) "
                + "VALUES (?, ?, ?, ?, 'Scheduled')";

        String notifSql = "INSERT INTO notifications (patient_id, appointment_id, message, is_read, notification_date) "
                + "VALUES (?, ?, ?, FALSE, CURRENT_TIMESTAMP)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            // Step 1: Check conflict
            try (PreparedStatement psCheck = con.prepareStatement(conflictCheckSql)) {
                psCheck.setInt(1, docId);
                psCheck.setDate(2, java.sql.Date.valueOf(apptDate));
                psCheck.setTime(3, java.sql.Time.valueOf(apptTime));
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this,
                                "This appointment slot is already booked.\nPlease select another time.",
                                "Conflict Detected",
                                JOptionPane.WARNING_MESSAGE);
                        loadAvailableTimeSlots();
                        return;
                    }
                }
            }

            // Step 2: Insert appointment
            int newApptId = -1;
            try (PreparedStatement psInsert = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, patientId);
                psInsert.setInt(2, docId);
                psInsert.setDate(3, java.sql.Date.valueOf(apptDate));
                psInsert.setTime(4, java.sql.Time.valueOf(apptTime));
                psInsert.executeUpdate();

                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    if (rs.next()) newApptId = rs.getInt(1);
                }
            }

            // Step 3: Fetch doctor name & send notification
            String docName = "Doctor";
            String docSpec = "General";
            String docQuery = "SELECT name, specialization FROM doctors WHERE doctor_id = ?";
            try (PreparedStatement psDoc = con.prepareStatement(docQuery)) {
                psDoc.setInt(1, docId);
                try (ResultSet rs = psDoc.executeQuery()) {
                    if (rs.next()) {
                        docName = rs.getString("name");
                        docSpec = rs.getString("specialization");
                    }
                }
            }

            String msg = "✅ Appointment Confirmed!\n"
                    + "Doctor: " + docName + " (" + docSpec + ")\n"
                    + "Date: " + dateText + "\n"
                    + "Time: " + timeText + "\n"
                    + "Status: Scheduled";

            try (PreparedStatement psNotif = con.prepareStatement(notifSql)) {
                psNotif.setInt(1, patientId);
                if (newApptId > 0) psNotif.setInt(2, newApptId);
                else psNotif.setNull(2, java.sql.Types.INTEGER);
                psNotif.setString(3, msg);
                psNotif.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Appointment booked successfully!\n"
                    + "Doctor: " + docName + "\n"
                    + "Date: " + dateText + " at " + timeText,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            dateField.setText("");
            loadAvailableTimeSlots();
            loadAppointments();
            updateNotificationBadge();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error booking appointment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadAppointments() {
        tableModel.setRowCount(0);
        String sql = "SELECT a.appointment_id, d.name, d.specialization, a.appointment_date, "
                + "a.appointment_time, a.expected_time, a.status "
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
                        String doc = rs.getString("name");
                        String spec = rs.getString("specialization");
                        String date = rs.getDate("appointment_date").toString();
                        String time = rs.getTime("appointment_time").toString().substring(0, 5);
                        Time expTime = rs.getTime("expected_time");
                        String exp = expTime != null ? expTime.toString().substring(0, 5) : "-";
                        String status = rs.getString("status");

                        tableModel.addRow(new Object[]{id, doc, spec, date, time, exp, status});
                    }
                }
            }
        } catch (SQLException ex) {
            System.err.println("Error loading appointments: " + ex.getMessage());
        }
    }

    private void cancelSelectedAppointment() {
        int row = appointmentsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the table to cancel.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int apptId = (int) appointmentsTable.getValueAt(row, 0);
        String doc = (String) appointmentsTable.getValueAt(row, 1);
        String date = (String) appointmentsTable.getValueAt(row, 3);
        String status = (String) appointmentsTable.getValueAt(row, 6);

        if ("Completed".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "A completed appointment cannot be cancelled.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("Cancelled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This appointment is already cancelled.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel your appointment with " + doc + " on " + date + "?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE appointments SET status = 'Cancelled' WHERE appointment_id = ? AND patient_id = ?";
        String notifSql = "INSERT INTO notifications (patient_id, appointment_id, message, is_read, notification_date) "
                + "VALUES (?, ?, ?, FALSE, CURRENT_TIMESTAMP)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, apptId);
                ps.setInt(2, patientId);
                int updated = ps.executeUpdate();

                if (updated > 0) {
                    try (PreparedStatement psN = con.prepareStatement(notifSql)) {
                        psN.setInt(1, patientId);
                        psN.setInt(2, apptId);
                        psN.setString(3, "❌ Your appointment with " + doc + " on " + date + " has been cancelled.");
                        psN.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(this, "Your appointment has been cancelled.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadAppointments();
                    updateNotificationBadge();
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error cancelling appointment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateNotificationBadge() {
        String sql = "SELECT COUNT(*) FROM notifications WHERE patient_id = ? AND is_read = FALSE";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        if (count > 0) {
                            btnNotifications.setText("🔔 (" + count + ")");
                            btnNotifications.setBackground(UITheme.WARNING_AMBER);
                        } else {
                            btnNotifications.setText("🔔 Notifications");
                            btnNotifications.setBackground(UITheme.ACCENT_TEAL);
                        }
                    }
                }
            }
        } catch (SQLException ignored) {}
    }

    private void openNotifications() {
        Notifications dialog = new Notifications(this, patientId, () -> {
            updateNotificationBadge();
            loadAppointments();
        });
        dialog.setVisible(true);
    }

    private void openMyAppointments() {
        MyAppointments dialog = new MyAppointments(this, patientId, patientName, () -> {
            loadAppointments();
            updateNotificationBadge();
        });
        dialog.setVisible(true);
    }

    private void openProfile() {
        Profile dialog = new Profile(this, patientId, () -> {
            refreshPatientName();
        });
        dialog.setVisible(true);
    }

    private void refreshPatientName() {
        String sql = "SELECT name FROM patients WHERE patient_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        patientName = rs.getString("name");
                        lblWelcome.setText("Welcome, " + patientName);
                    }
                }
            }
        } catch (SQLException ignored) {}
    }

    private void logout() {
        if (reminderTimer != null) reminderTimer.stop();
        new MainMenu().setVisible(true);
        dispose();
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new PatientDashboard(1, "Test Patient").setVisible(true));
    }
}
