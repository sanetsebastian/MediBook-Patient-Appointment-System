package MediBook;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Notifications view for patients to track confirmations, delays, rescheduling,
 * and status updates, acknowledge unread notifications, and view history.
 */
public class Notifications extends JDialog {

    private final int patientId;
    private final Runnable onAcknowledge;
    private JTable table;
    private DefaultTableModel model;
    private JCheckBox unreadOnlyCheck;

    public Notifications(Frame parent, int patientId, Runnable onAcknowledge) {
        super(parent, "Notifications & Alerts", true);
        this.patientId = patientId;
        this.onAcknowledge = onAcknowledge;

        initComponents();
        loadNotifications();
    }

    private void initComponents() {
        setSize(780, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("Notifications & Alerts", "MediBook Updates"), BorderLayout.NORTH);

        // Center Panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(UITheme.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);
        unreadOnlyCheck = new JCheckBox("Show Unread Only");
        unreadOnlyCheck.setFont(UITheme.FONT_BOLD);
        unreadOnlyCheck.setForeground(UITheme.TEXT_PRIMARY);
        unreadOnlyCheck.setOpaque(false);
        unreadOnlyCheck.addActionListener(e -> loadNotifications());
        filterBar.add(unreadOnlyCheck);
        mainPanel.add(filterBar, BorderLayout.NORTH);

        // Notifications Table
        String[] columns = {"ID", "Date / Time", "Message", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        UITheme.styleTable(table);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(450);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);

        // Highlight unread vs acknowledged
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, row, col);
                String status = val != null ? val.toString() : "";
                if ("Unread".equalsIgnoreCase(status)) {
                    setForeground(UITheme.DANGER_RED);
                    setFont(UITheme.FONT_BOLD);
                } else {
                    setForeground(UITheme.SUCCESS_GREEN);
                    setFont(UITheme.FONT_REGULAR);
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton viewBtn = new JButton("View Details");
        UITheme.stylePrimaryButton(viewBtn);
        viewBtn.addActionListener(e -> viewNotificationDetails());

        JButton ackBtn = new JButton("Acknowledge (Mark Read)");
        UITheme.styleTealButton(ackBtn);
        ackBtn.addActionListener(e -> acknowledgeSelected());

        JButton markAllBtn = new JButton("Mark All as Read");
        UITheme.styleSecondaryButton(markAllBtn);
        markAllBtn.addActionListener(e -> markAllAsRead());

        JButton closeBtn = new JButton("Close");
        UITheme.styleSecondaryButton(closeBtn);
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(viewBtn);
        btnPanel.add(ackBtn);
        btnPanel.add(markAllBtn);
        btnPanel.add(closeBtn);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    public void loadNotifications() {
        model.setRowCount(0);
        String sql = "SELECT notification_id, message, notification_date, is_read FROM notifications "
                + "WHERE patient_id = ? "
                + (unreadOnlyCheck.isSelected() ? "AND is_read = FALSE " : "")
                + "ORDER BY notification_date DESC";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("notification_id");
                        String msg = rs.getString("message");
                        Timestamp ts = rs.getTimestamp("notification_date");
                        String dateStr = ts != null ? ts.toString().substring(0, 16) : "-";
                        boolean isRead = rs.getBoolean("is_read");
                        String status = isRead ? "Acknowledged" : "Unread";

                        // First line of message for table display
                        String summary = msg.replace("\n", " ");
                        if (summary.length() > 70) {
                            summary = summary.substring(0, 67) + "...";
                        }

                        model.addRow(new Object[]{id, dateStr, summary, status});
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading notifications: " + ex.getMessage());
        }
    }

    private void viewNotificationDetails() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a notification to view.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int notifId = (int) table.getValueAt(row, 0);
        String fullMessage = getFullMessage(notifId);

        JTextArea area = new JTextArea(fullMessage);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UITheme.FONT_REGULAR);
        area.setBackground(Color.WHITE);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(450, 220));

        Object[] options = {"Acknowledge & Close", "Close"};
        int choice = JOptionPane.showOptionDialog(this, sp, "Notification Details",
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            markAsRead(notifId);
            loadNotifications();
            if (onAcknowledge != null) onAcknowledge.run();
        }
    }

    private String getFullMessage(int notifId) {
        String sql = "SELECT message FROM notifications WHERE notification_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return "";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, notifId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("message");
                }
            }
        } catch (SQLException ignored) {}
        return "";
    }

    private void acknowledgeSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a notification first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int notifId = (int) table.getValueAt(row, 0);
        markAsRead(notifId);
        loadNotifications();
        if (onAcknowledge != null) onAcknowledge.run();
    }

    private void markAsRead(int notifId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE notification_id = ? AND patient_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, notifId);
                ps.setInt(2, patientId);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error marking as read: " + ex.getMessage());
        }
    }

    private void markAllAsRead() {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE patient_id = ? AND is_read = FALSE";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                int updated = ps.executeUpdate();
                JOptionPane.showMessageDialog(this, updated + " notification(s) marked as acknowledged.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadNotifications();
                if (onAcknowledge != null) onAcknowledge.run();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
