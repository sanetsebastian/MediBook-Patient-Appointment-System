package MediBook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Custom Calendar Dialog for date selection.
 * Prevents past dates from being selected.
 * Disables days when doctor is not scheduled/available.
 */
public class CalendarDialog extends JDialog {

    private LocalDate currentDate;
    private JPanel calendarPanel;
    private JLabel monthLabel;
    private final int doctorId;

    public CalendarDialog(Frame parent, JTextField dateField, int doctorId) {
        super(parent, "Select Appointment Date", true);
        this.doctorId = doctorId;
        this.currentDate = LocalDate.now();

        setSize(420, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.PRIMARY_COLOR);
        topPanel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JButton prevButton = new JButton("◀");
        JButton nextButton = new JButton("▶");
        UITheme.styleSecondaryButton(prevButton);
        UITheme.styleSecondaryButton(nextButton);
        prevButton.setPreferredSize(new Dimension(50, 30));
        nextButton.setPreferredSize(new Dimension(50, 30));

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        monthLabel.setForeground(Color.WHITE);

        topPanel.add(prevButton, BorderLayout.WEST);
        topPanel.add(monthLabel, BorderLayout.CENTER);
        topPanel.add(nextButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Calendar Grid Panel
        calendarPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarPanel.setBackground(UITheme.BG_LIGHT);
        calendarPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        add(calendarPanel, BorderLayout.CENTER);

        // Bottom Controls
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(8, 16, 8, 16));

        JButton cancelButton = new JButton("Cancel");
        UITheme.styleSecondaryButton(cancelButton);
        cancelButton.addActionListener(e -> dispose());
        bottomPanel.add(cancelButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        prevButton.addActionListener(e -> {
            currentDate = currentDate.minusMonths(1);
            createCalendar(dateField);
        });

        nextButton.addActionListener(e -> {
            currentDate = currentDate.plusMonths(1);
            createCalendar(dateField);
        });

        createCalendar(dateField);
    }

    private void createCalendar(JTextField dateField) {
        calendarPanel.removeAll();

        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthLabel.setText(currentDate.format(monthFormatter));

        // Day of week headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            dayLabel.setForeground(UITheme.TEXT_MUTED);
            calendarPanel.add(dayLabel);
        }

        YearMonth yearMonth = YearMonth.from(currentDate);
        LocalDate firstDay = yearMonth.atDay(1);
        int startDay = firstDay.getDayOfWeek().getValue() % 7; // Sunday = 0

        // Empty cells before first day
        for (int i = 0; i < startDay; i++) {
            calendarPanel.add(new JLabel(""));
        }

        LocalDate today = LocalDate.now();

        // Date buttons
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            final LocalDate date = yearMonth.atDay(day);
            JButton dateBtn = new JButton(String.valueOf(day));
            dateBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dateBtn.setFocusPainted(false);
            dateBtn.setBackground(Color.WHITE);
            dateBtn.setForeground(UITheme.TEXT_PRIMARY);

            // Disable past dates
            if (date.isBefore(today)) {
                dateBtn.setEnabled(false);
                dateBtn.setBackground(new Color(241, 245, 249));
                dateBtn.setForeground(new Color(203, 213, 225));
            } else if (!isDoctorAvailable(date)) {
                // Doctor is not scheduled or available on this day
                dateBtn.setEnabled(false);
                dateBtn.setToolTipText("Doctor not available on this day");
                dateBtn.setBackground(new Color(248, 250, 252));
                dateBtn.setForeground(new Color(203, 213, 225));
            } else {
                // Highlight today
                if (date.isEqual(today)) {
                    dateBtn.setBorder(BorderFactory.createLineBorder(UITheme.PRIMARY_COLOR, 2));
                    dateBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
                dateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                dateBtn.addActionListener(e -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    dateField.setText(date.format(formatter));
                    dispose();
                });
            }

            calendarPanel.add(dateBtn);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private boolean isDoctorAvailable(LocalDate date) {
        if (doctorId <= 0) {
            return true;
        }

        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        // Check if doctor has any schedule entries in doctor_schedule
        String countSql = "SELECT COUNT(*) FROM doctor_schedule WHERE doctor_id = ?";
        String checkDaySql = "SELECT COUNT(*) FROM doctor_schedule WHERE doctor_id = ? AND day_of_week = ?";

        try (java.sql.Connection con = DBConnection.getConnection()) {
            if (con == null) return true;

            try (java.sql.PreparedStatement ps = con.prepareStatement(countSql)) {
                ps.setInt(1, doctorId);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        // Schedules are defined; check if this day is scheduled
                        try (java.sql.PreparedStatement dayPs = con.prepareStatement(checkDaySql)) {
                            dayPs.setInt(1, doctorId);
                            dayPs.setString(2, dayOfWeek);
                            try (java.sql.ResultSet dayRs = dayPs.executeQuery()) {
                                return dayRs.next() && dayRs.getInt(1) > 0;
                            }
                        }
                    }
                }
            }

            // If no specific schedule table entry, check available_days in doctors table
            String docSql = "SELECT available_days FROM doctors WHERE doctor_id = ?";
            try (java.sql.PreparedStatement docPs = con.prepareStatement(docSql)) {
                docPs.setInt(1, doctorId);
                try (java.sql.ResultSet docRs = docPs.executeQuery()) {
                    if (docRs.next()) {
                        String avail = docRs.getString("available_days");
                        if (avail != null && !avail.trim().isEmpty()) {
                            String lowerAvail = avail.toLowerCase();
                            String lowerDay = dayOfWeek.toLowerCase().substring(0, 3);
                            // Sunday check
                            if (dayOfWeek.equalsIgnoreCase("Sunday")) {
                                return lowerAvail.contains("sun");
                            }
                            if (lowerAvail.contains("mon-sat")) {
                                return !dayOfWeek.equalsIgnoreCase("Sunday");
                            }
                            return lowerAvail.contains(lowerDay);
                        }
                    }
                }
            }

        } catch (java.sql.SQLException ex) {
            System.err.println("Availability check error: " + ex.getMessage());
        }

        // Default: available Monday through Saturday
        return !date.getDayOfWeek().equals(java.time.DayOfWeek.SUNDAY);
    }
}
