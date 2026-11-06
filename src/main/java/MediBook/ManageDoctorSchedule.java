package MediBook;

import java.awt.*;
import java.sql.*;
import java.time.LocalTime;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

/**
 * ManageDoctorSchedule enables administrators to define consultation hours
 * and slot durations per doctor and weekday.
 */
public class ManageDoctorSchedule extends JFrame {

    private JTable scheduleTable;
    private DefaultTableModel model;

    private JComboBox<String> doctorBox;
    private JComboBox<String> dayBox;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JComboBox<String> durationBox;

    public ManageDoctorSchedule() {
        initComponents();
        loadDoctors();
        loadSchedules();
    }

    private void initComponents() {
        setTitle("MediBook - Manage Doctor Schedules");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(920, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("Doctor Schedules", "Administrator Management"), BorderLayout.NORTH);

        // Content
        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBackground(UITheme.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // LEFT: Add Schedule Form Card
        JPanel formCard = UITheme.createCard();
        formCard.setPreferredSize(new Dimension(320, 480));
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel lblAdd = new JLabel("➕ Add Doctor Schedule");
        lblAdd.setFont(UITheme.FONT_SECTION);
        lblAdd.setForeground(UITheme.PRIMARY_COLOR);
        lblAdd.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(lblAdd);
        formCard.add(Box.createVerticalStrut(12));

        doctorBox = new JComboBox<>();
        dayBox = new JComboBox<>(new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"});
        startTimeField = new JTextField("09:00", 12);
        endTimeField = new JTextField("17:00", 12);
        durationBox = new JComboBox<>(new String[]{"15", "20", "30", "45", "60"});
        durationBox.setSelectedItem("30");

        styleInputs(doctorBox, dayBox, startTimeField, endTimeField, durationBox);

        addFormField(formCard, "Select Doctor *:", doctorBox);
        addFormField(formCard, "Day of Week *:", dayBox);
        addFormField(formCard, "Start Time (HH:MM) *:", startTimeField);
        addFormField(formCard, "End Time (HH:MM) *:", endTimeField);
        addFormField(formCard, "Slot Duration (Minutes) *:", durationBox);

        JButton addBtn = new JButton("Save Schedule");
        UITheme.stylePrimaryButton(addBtn);
        addBtn.setMaximumSize(new Dimension(300, 36));
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.addActionListener(e -> addSchedule());

        formCard.add(Box.createVerticalStrut(12));
        formCard.add(addBtn);
        formCard.add(Box.createVerticalGlue());

        mainPanel.add(formCard, BorderLayout.WEST);

        // RIGHT: Schedules Table Card
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel lblTable = new JLabel("📅 Active Consultation Schedules");
        lblTable.setFont(UITheme.FONT_SECTION);
        lblTable.setForeground(UITheme.PRIMARY_COLOR);
        tableHeader.add(lblTable, BorderLayout.WEST);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableActions.setOpaque(false);

        JButton deleteBtn = new JButton("🗑️ Delete Selected");
        UITheme.styleDangerButton(deleteBtn);
        deleteBtn.addActionListener(e -> deleteSchedule());

        JButton refreshBtn = new JButton("🔄 Refresh");
        UITheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadSchedules());

        JButton backBtn = new JButton("Back to Dashboard");
        UITheme.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> {
            new AdminDashboard().setVisible(true);
            dispose();
        });

        tableActions.add(deleteBtn);
        tableActions.add(refreshBtn);
        tableActions.add(backBtn);
        tableHeader.add(tableActions, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"ID", "Doctor", "Day", "Start Time", "End Time", "Slot (mins)"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        scheduleTable = new JTable(model);
        UITheme.styleTable(scheduleTable);

        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        scheduleTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        scheduleTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        scheduleTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        scheduleTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        scheduleTable.getColumnModel().getColumn(5).setPreferredWidth(75);

        JScrollPane sp = new JScrollPane(scheduleTable);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(sp, BorderLayout.CENTER);

        mainPanel.add(tableCard, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void styleInputs(JComponent... comps) {
        for (JComponent c : comps) {
            c.setFont(UITheme.FONT_REGULAR);
            c.setMaximumSize(new Dimension(300, 32));
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            if (c instanceof JTextField) {
                c.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UITheme.BORDER_COLOR, 1),
                    new EmptyBorder(5, 7, 5, 7)
                ));
            } else {
                c.setBackground(Color.WHITE);
            }
        }
    }

    private void addFormField(JPanel p, String lblText, JComponent comp) {
        JLabel l = new JLabel(lblText);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(2));
        p.add(comp);
        p.add(Box.createVerticalStrut(8));
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

    public void loadSchedules() {
        model.setRowCount(0);
        String sql = "SELECT s.schedule_id, d.name AS doc_name, s.day_of_week, s.start_time, s.end_time, s.slot_duration "
                + "FROM doctor_schedule s "
                + "JOIN doctors d ON s.doctor_id = d.doctor_id "
                + "ORDER BY d.name ASC, FIELD(s.day_of_week, 'Monday','Tuesday','Wednesday','Thursday','Friday','Saturday','Sunday')";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("schedule_id"),
                        rs.getString("doc_name"),
                        rs.getString("day_of_week"),
                        rs.getTime("start_time").toString().substring(0, 5),
                        rs.getTime("end_time").toString().substring(0, 5),
                        rs.getInt("slot_duration")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading schedules: " + ex.getMessage());
        }
    }

    private void addSchedule() {
        Object docObj = doctorBox.getSelectedItem();
        if (docObj == null) {
            JOptionPane.showMessageDialog(this, "Please select a doctor.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int docId = Integer.parseInt(docObj.toString().split(" - ")[0]);
        String day = (String) dayBox.getSelectedItem();
        String startStr = startTimeField.getText().trim();
        String endStr = endTimeField.getText().trim();
        int slotDuration = Integer.parseInt((String) durationBox.getSelectedItem());

        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(startStr);
            end = LocalTime.parse(endStr);
            if (!start.isBefore(end)) {
                JOptionPane.showMessageDialog(this, "Start time must be before end time.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid time format. Please use HH:MM (e.g. 09:00).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String insertSql = "INSERT INTO doctor_schedule (doctor_id, day_of_week, start_time, end_time, slot_duration) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setInt(1, docId);
                ps.setString(2, day);
                ps.setTime(3, Time.valueOf(start));
                ps.setTime(4, Time.valueOf(end));
                ps.setInt(5, slotDuration);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Schedule saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadSchedules();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving schedule: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSchedule() {
        int row = scheduleTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a schedule to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int scheduleId = (int) scheduleTable.getValueAt(row, 0);
        String doc = (String) scheduleTable.getValueAt(row, 1);
        String day = (String) scheduleTable.getValueAt(row, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete schedule for " + doc + " on " + day + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM doctor_schedule WHERE schedule_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, scheduleId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Schedule deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSchedules();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting schedule: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new ManageDoctorSchedule().setVisible(true));
    }
}
