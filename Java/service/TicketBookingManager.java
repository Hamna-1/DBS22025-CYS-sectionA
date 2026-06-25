package service;

import model.Ticket;
import model.Payment;
import util.DBConnection;
import util.AuditLogger;
import exception.SeatNotAvailable;
import java.sql.*;

public class TicketBookingManager {

    // TRANSACTION 1: Book Ticket with Payment
    public boolean bookTicket(Ticket ticket, Payment payment) {
        Connection conn = null;
        boolean autoCommit = false;

        try {
            conn = DBConnection.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Check seat availability
            String checkSeats = "select available_seats from ShowSchedules where schedule_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSeats);
            checkStmt.setInt(1, ticket.getScheduleId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int availableSeats = rs.getInt("available_seats");
                if (availableSeats <= 0) {
                    throw new SeatNotAvailable("No seats available for this show!");
                }
            }

            // Step 2: Insert ticket
            String insertTicket = "insert into Tickets (schedule_id, customer_id, discount_id, seat_number, price, status) values (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt1 = conn.prepareStatement(insertTicket, Statement.RETURN_GENERATED_KEYS);
            pstmt1.setInt(1, ticket.getScheduleId());
            pstmt1.setInt(2, ticket.getCustomerId());
            if (ticket.getDiscountId() != null) {
                pstmt1.setInt(3, ticket.getDiscountId());
            } else {
                pstmt1.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt1.setString(4, ticket.getSeatNumber());
            pstmt1.setBigDecimal(5, ticket.getPrice());
            pstmt1.setString(6, "ooked");

            int rowsAffected = pstmt1.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Failed to insert ticket");
            }

            // Get generated ticket ID
            int ticketId = 0;
            ResultSet generatedKeys = pstmt1.getGeneratedKeys();
            if (generatedKeys.next()) {
                ticketId = generatedKeys.getInt(1);
            }

            // Step 3: Insert payment
            String insertPayment = "insert into Payments (ticket_id, amount, payment_method, status) values (?, ?, ?, ?)";
            PreparedStatement pstmt2 = conn.prepareStatement(insertPayment);
            pstmt2.setInt(1, ticketId);
            pstmt2.setBigDecimal(2, payment.getAmount());
            pstmt2.setString(3, payment.getPaymentMethod());
            pstmt2.setString(4, "SUCCESS");
            pstmt2.executeUpdate();

            // Step 4: Update available seats (trigger will also do this, but we do it in transaction)
            String updateSeats = "update ShowSchedules set available_seats = available_seats - 1 where schedule_id = ?";
            PreparedStatement pstmt3 = conn.prepareStatement(updateSeats);
            pstmt3.setInt(1, ticket.getScheduleId());
            pstmt3.executeUpdate();

            // Commit transaction
            conn.commit();
            System.out.println("Ticket booked successfully! Ticket ID: " + ticketId);

            // Log activity
            AuditLogger.Activity("Tickets", "INSERT", null,
                    "Ticket booked - ID: " + ticketId + ", Seat: " + ticket.getSeatNumber());

            return true;

        } catch (SeatNotAvailable e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Booking failed: " + e.getMessage());
            return false;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Transaction failed: " + e.getMessage());
            AuditLogger.Error("Book Ticket Transaction", e);
            return false;

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(autoCommit);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // TRANSACTION 2: Cancel Ticket with Refund
    public boolean cancelTicket(int ticketId) {
        Connection conn = null;
        boolean autoCommit = false;

        try {
            conn = DBConnection.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Step 1: Get schedule_id from ticket
            String getSchedule = "SELECT schedule_id FROM Tickets WHERE ticket_id = ?";
            PreparedStatement pstmt1 = conn.prepareStatement(getSchedule);
            pstmt1.setInt(1, ticketId);
            ResultSet rs = pstmt1.executeQuery();

            int scheduleId = 0;
            if (rs.next()) {
                scheduleId = rs.getInt("schedule_id");
            } else {
                throw new SQLException("Ticket not found!");
            }

            // Step 2: Update ticket status to CANCELLED
            String updateTicket = "UPDATE Tickets SET status = 'CANCELLED' WHERE ticket_id = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(updateTicket);
            pstmt2.setInt(1, ticketId);
            pstmt2.executeUpdate();

            // Step 3: Restore seat
            String restoreSeat = "UPDATE ShowSchedules SET available_seats = available_seats + 1 WHERE schedule_id = ?";
            PreparedStatement pstmt3 = conn.prepareStatement(restoreSeat);
            pstmt3.setInt(1, scheduleId);
            pstmt3.executeUpdate();

            // Step 4: Create refund payment record
            String insertRefund = "INSERT INTO Payments (ticket_id, amount, payment_method, status) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt4 = conn.prepareStatement(insertRefund);
            pstmt4.setInt(1, ticketId);
            pstmt4.setBigDecimal(2, java.math.BigDecimal.ZERO);
            pstmt4.setString(3, "REFUND");
            pstmt4.setString(4, "REFUNDED");
            pstmt4.executeUpdate();

            conn.commit();
            System.out.println("Ticket " + ticketId + " cancelled successfully!");

            AuditLogger.Activity("Tickets", "CANCEL", null, "Ticket cancelled - ID: " + ticketId);

            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Cancellation failed: " + e.getMessage());
            AuditLogger.Error("Cancel Ticket", e);
            return false;

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(autoCommit);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // TRANSACTION 3: Register Employee as Performer
    public boolean registerPerformer(int employeeId, String stageName, String specialty, int experienceYears) {
        Connection conn = null;
        boolean autoCommit = false;

        try {
            conn = DBConnection.getConnection();
            autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            // Step 1: Check if employee exists
            String checkEmployee = "SELECT employee_id FROM Employees WHERE employee_id = ? AND status = 'ACTIVE'";
            PreparedStatement pstmt1 = conn.prepareStatement(checkEmployee);
            pstmt1.setInt(1, employeeId);
            ResultSet rs = pstmt1.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Employee not found or inactive!");
            }

            // Step 2: Insert into Performers table
            String insertPerformer = "INSERT INTO Performers (employee_id, stage_name, specialty, experience_years) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt2 = conn.prepareStatement(insertPerformer);
            pstmt2.setInt(1, employeeId);
            pstmt2.setString(2, stageName);
            pstmt2.setString(3, specialty);
            pstmt2.setInt(4, experienceYears);
            pstmt2.executeUpdate();

            conn.commit();
            System.out.println("Performer registered successfully!");

            AuditLogger.Activity("Performers", "INSERT", employeeId,
                    "Performer registered - Stage Name: " + stageName);

            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Performer registration failed: " + e.getMessage());
            AuditLogger.Error("Register Performer", e);
            return false;

        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(autoCommit);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}