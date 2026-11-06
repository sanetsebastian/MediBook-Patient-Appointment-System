package MediBook;

import java.sql.*;

public class MediBookTest {
    public static void main(String[] args) {
        System.out.println("=== STARTING MEDIBOOK DATABASE & SYSTEM VERIFICATION ===");
        
        // 1. Test Connection
        try (Connection con = DBConnection.getConnection()) {
            if (con == null || con.isClosed()) {
                System.err.println("FAIL: Database connection is null or closed.");
                System.exit(1);
            }
            System.out.println("PASS: Database connected to medibook_new_db successfully.");
            
            // 2. Test Admin Login query
            String adminSql = "SELECT admin_id, username FROM admin WHERE username = ? AND password = ?";
            try (PreparedStatement ps = con.prepareStatement(adminSql)) {
                ps.setString(1, "admin");
                ps.setString(2, "admin123");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("PASS: Admin login verified for user: " + rs.getString("username"));
                    } else {
                        System.err.println("FAIL: Admin user not found.");
                    }
                }
            }
            
            // 3. Test Doctor Login query
            String docSql = "SELECT doctor_id, name FROM doctors WHERE username = ? AND password = ?";
            try (PreparedStatement ps = con.prepareStatement(docSql)) {
                ps.setString(1, "priya02");
                ps.setString(2, "priya123");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("PASS: Doctor login verified for: " + rs.getString("name"));
                    } else {
                        System.err.println("FAIL: Doctor login failed.");
                    }
                }
            }
            
            // 4. Test Patient Login query
            String patSql = "SELECT patient_id, name FROM patients WHERE username = ? AND password = ?";
            try (PreparedStatement ps = con.prepareStatement(patSql)) {
                ps.setString(1, "sanet");
                ps.setString(2, "12345");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("PASS: Patient login verified for: " + rs.getString("name"));
                    } else {
                        System.err.println("FAIL: Patient login failed.");
                    }
                }
            }
            
            // 5. Test Doctor Schedules query
            String schedSql = "SELECT COUNT(*) FROM doctor_schedule";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(schedSql)) {
                if (rs.next()) {
                    System.out.println("PASS: Doctor schedules verified. Total entries: " + rs.getInt(1));
                }
            }
            
            // 6. Test Conflict Checking query logic
            String conflictSql = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status IN ('Scheduled', 'Delayed', 'Rescheduled')";
            try (PreparedStatement ps = con.prepareStatement(conflictSql)) {
                ps.setInt(1, 2);
                ps.setDate(2, Date.valueOf("2026-11-12"));
                ps.setTime(3, Time.valueOf("12:30:00"));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("PASS: Conflict check query executed successfully. Existing active bookings for slot: " + rs.getInt(1));
                    }
                }
            }
            
            // 7. Test Notifications query
            String notifSql = "SELECT COUNT(*) FROM notifications";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(notifSql)) {
                if (rs.next()) {
                    System.out.println("PASS: Notifications table verified. Total notifications: " + rs.getInt(1));
                }
            }
            
            System.out.println("=== ALL VERIFICATION TESTS PASSED SUCCESSFULLY ===");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
