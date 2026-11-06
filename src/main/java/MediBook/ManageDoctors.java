package MediBook;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

/**
 * ManageDoctors allows administrators to view, add, and manage doctors in the system.
 */
public class ManageDoctors extends JFrame {

    private JTable doctorTable;
    private DefaultTableModel model;

    private JTextField nameField;
    private JTextField specField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField daysField;
    private JTextField usernameField;
    private JPasswordField passwordField;

    public ManageDoctors() {
        initComponents();
        loadDoctors();
    }

    private void initComponents() {
        setTitle("MediBook - Manage Doctors");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(940, 640);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("Manage Doctors", "Administrator Portal"), BorderLayout.NORTH);

        // Content
        JPanel mainPanel = new JPanel(new BorderLayout(16, 16));
        mainPanel.setBackground(UITheme.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // LEFT: Add Doctor Form Card
        JPanel formCard = UITheme.createCard();
        formCard.setPreferredSize(new Dimension(340, 520));
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel lblAdd = new JLabel("➕ Add New Doctor");
        lblAdd.setFont(UITheme.FONT_SECTION);
        lblAdd.setForeground(UITheme.PRIMARY_COLOR);
        lblAdd.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(lblAdd);
        formCard.add(Box.createVerticalStrut(12));

        nameField = new JTextField(16);
        specField = new JTextField(16);
        phoneField = new JTextField(16);
        emailField = new JTextField(16);
        daysField = new JTextField(16);
        daysField.setText("Mon-Sat");
        usernameField = new JTextField(16);
        passwordField = new JPasswordField(16);

        styleInputs(nameField, specField, phoneField, emailField, daysField, usernameField, passwordField);

        addFormField(formCard, "Full Name *:", nameField);
        addFormField(formCard, "Specialization *:", specField);
        addFormField(formCard, "Phone Number *:", phoneField);
        addFormField(formCard, "Email Address *:", emailField);
        addFormField(formCard, "Available Days *:", daysField);
        addFormField(formCard, "Username *:", usernameField);
        addFormField(formCard, "Password *:", passwordField);

        JButton addBtn = new JButton("Add Doctor");
        UITheme.stylePrimaryButton(addBtn);
        addBtn.setMaximumSize(new Dimension(320, 36));
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.addActionListener(e -> addDoctor());
        formCard.add(Box.createVerticalStrut(12));
        formCard.add(addBtn);

        mainPanel.add(formCard, BorderLayout.WEST);

        // RIGHT: Doctor Table Card
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel lblTable = new JLabel("👨‍⚕️ Registered Doctors");
        lblTable.setFont(UITheme.FONT_SECTION);
        lblTable.setForeground(UITheme.PRIMARY_COLOR);
        tableHeader.add(lblTable, BorderLayout.WEST);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableActions.setOpaque(false);

        JButton deleteBtn = new JButton("🗑️ Delete Selected");
        UITheme.styleDangerButton(deleteBtn);
        deleteBtn.addActionListener(e -> deleteDoctor());

        JButton refreshBtn = new JButton("🔄 Refresh");
        UITheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadDoctors());

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

        String[] cols = {"ID", "Name", "Specialization", "Phone", "Email", "Available Days", "Username"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        doctorTable = new JTable(model);
        UITheme.styleTable(doctorTable);

        doctorTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        doctorTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        doctorTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        doctorTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        doctorTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        doctorTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        doctorTable.getColumnModel().getColumn(6).setPreferredWidth(85);

        JScrollPane sp = new JScrollPane(doctorTable);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(sp, BorderLayout.CENTER);

        mainPanel.add(tableCard, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void styleInputs(JComponent... comps) {
        for (JComponent c : comps) {
            c.setFont(UITheme.FONT_REGULAR);
            c.setMaximumSize(new Dimension(320, 30));
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            c.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(4, 6, 4, 6)
            ));
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

    public void loadDoctors() {
        model.setRowCount(0);
        String sql = "SELECT doctor_id, name, specialization, phone, email, available_days, username FROM doctors ORDER BY doctor_id ASC";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("doctor_id"),
                        rs.getString("name"),
                        rs.getString("specialization"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("available_days"),
                        rs.getString("username")
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading doctors: " + ex.getMessage());
        }
    }

    private void addDoctor() {
        String name = nameField.getText().trim();
        String spec = specField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String days = daysField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (name.isEmpty() || spec.isEmpty() || phone.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String checkSql = "SELECT COUNT(*) FROM doctors WHERE username = ?";
        String insertSql = "INSERT INTO doctors (name, specialization, phone, email, available_days, username, password) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                checkPs.setString(1, username);
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "Doctor username already exists. Please choose another.", "Duplicate Username", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            try (PreparedStatement insertPs = con.prepareStatement(insertSql)) {
                insertPs.setString(1, name);
                insertPs.setString(2, spec);
                insertPs.setString(3, phone);
                insertPs.setString(4, email);
                insertPs.setString(5, days);
                insertPs.setString(6, username);
                insertPs.setString(7, password);
                insertPs.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Doctor added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            nameField.setText("");
            specField.setText("");
            phoneField.setText("");
            emailField.setText("");
            usernameField.setText("");
            passwordField.setText("");
            loadDoctors();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding doctor: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDoctor() {
        int row = doctorTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to delete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int docId = (int) doctorTable.getValueAt(row, 0);
        String name = (String) doctorTable.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete doctor '" + name + "' (ID: " + docId + ")?\n"
                + "This will also remove their schedule and appointments.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, docId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Doctor deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDoctors();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting doctor: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new ManageDoctors().setVisible(true));
    }
}
