package MediBook;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

/**
 * ViewPatients displays registered patients and supports search by name, phone, or ID.
 */
public class ViewPatients extends JFrame {

    private JTable patientTable;
    private DefaultTableModel model;
    private JTextField searchField;

    public ViewPatients() {
        initComponents();
        loadPatients("");
    }

    private void initComponents() {
        setTitle("MediBook - View Patients");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(920, 580);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        add(UITheme.createHeader("View Patients", "Administrator Directory"), BorderLayout.NORTH);

        // Content
        JPanel mainPanel = new JPanel(new BorderLayout(0, 12));
        mainPanel.setBackground(UITheme.BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Top Filter / Search Bar
        JPanel topBar = UITheme.createCard();
        topBar.setLayout(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Search Patient:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_PRIMARY);

        searchField = new JTextField(20);
        searchField.setFont(UITheme.FONT_REGULAR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1),
            new EmptyBorder(5, 7, 5, 7)
        ));

        JButton searchBtn = new JButton("Search");
        UITheme.stylePrimaryButton(searchBtn);
        searchBtn.addActionListener(e -> loadPatients(searchField.getText().trim()));

        JButton clearBtn = new JButton("Clear");
        UITheme.styleSecondaryButton(clearBtn);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            loadPatients("");
        });

        searchPanel.add(lblSearch);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton refreshBtn = new JButton("🔄 Refresh");
        UITheme.styleSecondaryButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadPatients(searchField.getText().trim()));

        JButton backBtn = new JButton("Back to Dashboard");
        UITheme.styleSecondaryButton(backBtn);
        backBtn.addActionListener(e -> {
            new AdminDashboard().setVisible(true);
            dispose();
        });

        actionPanel.add(refreshBtn);
        actionPanel.add(backBtn);

        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Patients Table Card
        JPanel tableCard = UITheme.createCard();
        tableCard.setLayout(new BorderLayout());

        String[] cols = {"ID", "Full Name", "Age", "Gender", "Phone", "Email", "Address", "Username"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        patientTable = new JTable(model);
        UITheme.styleTable(patientTable);

        patientTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        patientTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        patientTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        patientTable.getColumnModel().getColumn(3).setPreferredWidth(65);
        patientTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        patientTable.getColumnModel().getColumn(5).setPreferredWidth(140);
        patientTable.getColumnModel().getColumn(6).setPreferredWidth(180);
        patientTable.getColumnModel().getColumn(7).setPreferredWidth(90);

        JScrollPane sp = new JScrollPane(patientTable);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableCard.add(sp, BorderLayout.CENTER);

        mainPanel.add(tableCard, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
    }

    public void loadPatients(String query) {
        model.setRowCount(0);
        String sql = "SELECT patient_id, name, age, gender, phone, email, address, username FROM patients ";
        if (query != null && !query.isEmpty()) {
            sql += "WHERE name LIKE ? OR phone LIKE ? OR username LIKE ? OR patient_id = ? ";
        }
        sql += "ORDER BY patient_id ASC";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                if (query != null && !query.isEmpty()) {
                    String pattern = "%" + query + "%";
                    ps.setString(1, pattern);
                    ps.setString(2, pattern);
                    ps.setString(3, pattern);
                    int idVal = -1;
                    try { idVal = Integer.parseInt(query); } catch (Exception ignored) {}
                    ps.setInt(4, idVal);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getInt("patient_id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("gender"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("address"),
                            rs.getString("username")
                        });
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading patients: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new ViewPatients().setVisible(true));
    }
}
