package service;

import util.DBConnection;
import util.AuditLogger;
import java.sql.*;

public class StoredProcedureManager {

    // Call sp_BookTicket stored procedure
    public boolean bookTicketViaProcedure(int scheduleId, int customerId, Integer discountId,
                                          String seatNumber, double price) {
        String callSQL = "{call sp_BookTicket(?, ?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSQL)) {

            cstmt.setInt(1, scheduleId);
            cstmt.setInt(2, customerId);
            if (discountId != null) {
                cstmt.setInt(3, discountId);
            } else {
                cstmt.setNull(3, java.sql.Types.INTEGER);
            }
            cstmt.setString(4, seatNumber);
            cstmt.setDouble(5, price);

            cstmt.execute();
            System.out.println("Ticket booked via stored procedure successfully!");
            return true;

        } catch (SQLException e) {
            System.err.println("Stored procedure booking failed: " + e.getMessage());
            AuditLogger.Error("sp_BookTicket", e);
            return false;
        }
    }

    // Call sp_CancelTicket stored procedure
    public boolean cancelTicketViaProcedure(int ticketId) {
        String callSQL = "{call sp_CancelTicket(?)}";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSQL)) {

            cstmt.setInt(1, ticketId);
            cstmt.execute();
            System.out.println("Ticket cancelled via stored procedure successfully!");
            return true;

        } catch (SQLException e) {
            System.err.println("Stored procedure cancellation failed: " + e.getMessage());
            AuditLogger.Error("sp_CancelTicket", e);
            return false;
        }
    }

    // Call sp_GetShowRevenue stored procedure
    public void getShowRevenue(int showId) {
        String callSQL = "{call sp_GetShowRevenue(?)}";

        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(callSQL)) {

            cstmt.setInt(1, showId);
            ResultSet rs = cstmt.executeQuery();

            System.out.println("\n===== SHOW REVENUE REPORT =====");
            while (rs.next()) {
                System.out.println("Show Title: " + rs.getString("title"));
                System.out.println("Tickets Sold: " + rs.getInt("tickets_sold"));
                System.out.println("Total Revenue: $" + rs.getDouble("total_revenue"));
            }
            System.out.println("===============================\n");

        } catch (SQLException e) {
            System.err.println("Revenue report failed: " + e.getMessage());
            AuditLogger.Error("sp_GetShowRevenue", e);
        }
    }
}