package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AuditLogger {

    public static void Error(String action, Exception e) {
        String logQuery = "insert into AuditLogs (table_name, action_type, details) values (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(logQuery)) {

            pstmt.setString(1, "System");
            pstmt.setString(2, "Error");
            pstmt.setString(3, action + " - Error: " + e.getMessage());
            pstmt.executeUpdate();

            System.err.println("Error : " + action);
        } catch (SQLException ex) {
            System.err.println("Failed to log error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void Activity(String tableName, String actionType,
                                Integer employeeId, String details) {
        String logQuery = "insert into AuditLogs (table_name, action_type, performed_by, details) values (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(logQuery)) {

            ps.setString(1, tableName);
            ps.setString(2, actionType);
            if (employeeId != null) {
                ps.setInt(3, employeeId);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, details);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }
}