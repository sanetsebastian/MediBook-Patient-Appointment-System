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

/**
 * AppointmentBooking screen enables patients to select doctors, dates, and times,
 * and securely books appointments preventing conflicts/double-booking.
 */
public class AppointmentBooking extends JDialog {

    private final int patientId;
    private final String patientName;
    private final Runnable onBookingSuccess;

    private JComboBox<String> doctorBox;
    private JLabel lblDoctorDetails;
    private JTextField dateField;
    private JComboBox<String> timeBox;

    public AppointmentBooking(Frame parent, int patientId, String patientName, Runnable onBookingSuccess) {
        super(parent, "Book Doctor Appointment", true);
        this.patientId = patientId;
        this.patientName = patientName;
        this.onBookingSuccess = onBookingSuccess;

        initComponents();
    }

    private void initComponents() {
        setSize(560, 520);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("Book Appointment", "Patient: " + patientName), BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(UITheme.BG_LIGHT);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel card = UITheme.createCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // 1. Doctor Selection
        doctorBox = new JComboBox<>();
        doctorBox.setFont(UITheme.FONT_REGULAR);
        doctorBox.setBackground(Color.WHITE);

        lblDoctorDetails = new JLabel(" ");
        lblDoctorDetails.setFont(UITheme.FONT_SMALL);
        lblDoctorDetails.setForeground(UITheme.TEXT_MUTED);

        // 2. Date Selection
        dateField = new JTextField(12);
        dateField.setEditable(false);
        dateField.setFont(UITheme.FONT_REGULAR);
        dateField.setBackground(Color.WHITE);
        dateField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1),
            new EmptyBorder(6, 8, 6, 8)
        ));

        JButton selectDateBtn = new JButton("📅 Pick Date");
        UITheme.styleSecondaryButton(selectDateBtn);

        JPanel datePanel = new JPanel(new BorderLayout(8, 0));
        datePanel.setOpaque(false);
        datePanel.add(dateField, BorderLayout.CENTER);
        datePanel.add(selectDateBtn, BorderLayout.EAST);

        // 3. Time Slot Selection
        timeBox = new JComboBox<>();
        timeBox.setFont(UITheme.FONT_REGULAR);
        timeBox.setBackground(Color.WHITE);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lblDoc = new JLabel("Select Doctor *:");
        lblDoc.setFont(UITheme.FONT_BOLD);
        card.add(lblDoc, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        card.add(doctorBox, gbc);

        row++;
        gbc.gridx = 1; gbc.gridy = row;
        card.add(lblDoctorDetails, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lblDate = new JLabel("Appointment Date *:");
        lblDate.setFont(UITheme.FONT_BOLD);
        card.add(lblDate, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        card.add(datePanel, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lblTime = new JLabel("Available Time Slot *:");
        lblTime.setFont(UITheme.FONT_BOLD);
        card.add(lblTime, gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        card.add(timeBox, gbc);

        contentPanel.add(card);
        contentPanel.add(Box.createVerticalStrut(20));

        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        btnPanel.setOpaque(false);

        JButton bookBtn = new JButton("Confirm Booking");
        UITheme.stylePrimaryButton(bookBtn);
        bookBtn.addActionListener(e -> bookAppointment());

        JButton cancelBtn = new JButton("Cancel");
        UITheme.styleSecondaryButton(cancelBtn);
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(bookBtn);
        btnPanel.add(cancelBtn);
        contentPanel.add(btnPanel);

        add(contentPanel, BorderLayout.CENTER);

        // Event Listeners
        loadDoctors();

        doctorBox.addActionListener(e -> {
            updateDoctorDetails();
            loadAvailableTimeSlots();
        });

        selectDateBtn.addActionListener(e -> {
            int docId = getSelectedDoctorId();
            if (docId <= 0) {
                JOptionPane.showMessageDialog(this, "Please select a doctor first.", "Notice", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CalendarDialog calendar = new CalendarDialog((Frame) getParent(), dateField, docId);
            calendar.setVisible(true);
            loadAvailableTimeSlots();
        });

        if (doctorBox.getItemCount() > 0) {
            doctorBox.setSelectedIndex(0);
        }
    }

    private int getSelectedDoctorId() {
        Object item = doctorBox.getSelectedItem();
        if (item == null) return -1;
        String str = item.toString();
        try {
            return Integer.parseInt(str.split(" - ")[0].trim());
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
    }

    private void updateDoctorDetails() {
        int docId = getSelectedDoctorId();
        if (docId <= 0) {
            lblDoctorDetails.setText(" ");
            return;
        }
        String sql = "SELECT phone, email, available_days FROM doctors WHERE doctor_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, docId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String days = rs.getString("available_days");
                        lblDoctorDetails.setText("Available Days: " + (days != null ? days : "Mon-Sat"));
                    }
                }
            }
        } catch (SQLException ignored) {}
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

        // Prevent past dates
        if (selectedDate.isBefore(LocalDate.now())) {
            timeBox.addItem("-- Cannot select past dates --");
            return;
        }

        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        java.util.List<String> generatedSlots = new ArrayList<>();

        // Check doctor_schedule
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
                            generatedSlots.add(cur.format(DateTimeFormatter.ofPattern("HH:mm")));
                            cur = cur.plusMinutes(duration);
                        }
                    }
                }
            }

            // If no custom schedule for this doctor on this day, fallback to standard 09:00 - 19:30 slots
            if (!hasSchedule) {
                LocalTime cur = LocalTime.of(9, 0);
                LocalTime end = LocalTime.of(19, 30);
                while (!cur.isAfter(end)) {
                    generatedSlots.add(cur.format(DateTimeFormatter.ofPattern("HH:mm")));
                    cur = cur.plusMinutes(30);
                }
            }

            // Find already booked slots for this doctor and date
            Set<String> bookedSlots = new HashSet<>();
            String checkBookedSql = "SELECT appointment_time FROM appointments "
                    + "WHERE doctor_id = ? AND appointment_date = ? "
                    + "AND status IN ('Scheduled', 'Delayed', 'Rescheduled')";

            try (PreparedStatement ps = con.prepareStatement(checkBookedSql)) {
                ps.setInt(1, docId);
                ps.setDate(2, java.sql.Date.valueOf(selectedDate));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Time t = rs.getTime("appointment_time");
                        String timeStr = t.toString().substring(0, 5);
                        bookedSlots.add(timeStr);
                    }
                }
            }

            // Filter out booked slots and slots that passed today
            LocalTime nowTime = LocalTime.now();
            boolean isToday = selectedDate.isEqual(LocalDate.now());

            int availableCount = 0;
            for (String slot : generatedSlots) {
                if (bookedSlots.contains(slot)) {
                    continue; // Double-booking prevention
                }
                if (isToday) {
                    LocalTime slotTime = LocalTime.parse(slot);
                    if (slotTime.isBefore(nowTime)) {
                        continue; // Cannot book past time today
                    }
                }
                timeBox.addItem(slot);
                availableCount++;
            }

            if (availableCount == 0) {
                timeBox.addItem("-- No Available Slots For This Date --");
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

        // DOUBLE-BOOKING CONFLICT CHECK in application/database logic
        String conflictCheckSql = "SELECT COUNT(*) FROM appointments "
                + "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? "
                + "AND status IN ('Scheduled', 'Delayed', 'Rescheduled')";

        String insertApptSql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status) "
                + "VALUES (?, ?, ?, ?, 'Scheduled')";

        String insertNotifSql = "INSERT INTO notifications (patient_id, appointment_id, message, is_read, notification_date) "
                + "VALUES (?, ?, ?, FALSE, CURRENT_TIMESTAMP)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            // Step 1: Conflict check
            try (PreparedStatement psCheck = con.prepareStatement(conflictCheckSql)) {
                psCheck.setInt(1, docId);
                psCheck.setDate(2, java.sql.Date.valueOf(apptDate));
                psCheck.setTime(3, java.sql.Time.valueOf(apptTime));

                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this,
                                "This appointment slot is already booked.\nPlease select another time slot.",
                                "Conflict Detected",
                                JOptionPane.WARNING_MESSAGE);
                        loadAvailableTimeSlots();
                        return;
                    }
                }
            }

            // Step 2: Book appointment
            int generatedApptId = -1;
            try (PreparedStatement psInsert = con.prepareStatement(insertApptSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, patientId);
                psInsert.setInt(2, docId);
                psInsert.setDate(3, java.sql.Date.valueOf(apptDate));
                psInsert.setTime(4, java.sql.Time.valueOf(apptTime));

                psInsert.executeUpdate();
                try (ResultSet rsKeys = psInsert.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        generatedApptId = rsKeys.getInt(1);
                    }
                }
            }

            // Step 3: Fetch doctor name and insert confirmation notification
            String docName = "Doctor";
            String docSpec = "General";
            String docInfoSql = "SELECT name, specialization FROM doctors WHERE doctor_id = ?";
            try (PreparedStatement docPs = con.prepareStatement(docInfoSql)) {
                docPs.setInt(1, docId);
                try (ResultSet rs = docPs.executeQuery()) {
                    if (rs.next()) {
                        docName = rs.getString("name");
                        docSpec = rs.getString("specialization");
                    }
                }
            }

            String notifMsg = "✅ Appointment Confirmed!\n"
                    + "Doctor: " + docName + " (" + docSpec + ")\n"
                    + "Date: " + dateText + "\n"
                    + "Time: " + timeText + "\n"
                    + "Status: Scheduled\n"
                    + "Please arrive 10 minutes before your appointment.";

            try (PreparedStatement psNotif = con.prepareStatement(insertNotifSql)) {
                psNotif.setInt(1, patientId);
                if (generatedApptId > 0) {
                    psNotif.setInt(2, generatedApptId);
                } else {
                    psNotif.setNull(2, java.sql.Types.INTEGER);
                }
                psNotif.setString(3, notifMsg);
                psNotif.executeUpdate();
            }

            JOptionPane.showMessageDialog(this,
                    "Appointment booked successfully!\n"
                    + "Date: " + dateText + " at " + timeText + "\n"
                    + "Doctor: " + docName,
                    "Booking Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            if (onBookingSuccess != null) {
                onBookingSuccess.run();
            }

            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error booking appointment: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
