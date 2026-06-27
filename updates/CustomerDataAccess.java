package dataAccess;

import model.Customer;
import util.DBConnection;
import util.AuditLogger;
import validator.CustomerValidation;
import exception.Validation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDataAccess {

    public boolean addCustomer(Customer customer) {
        String query = "insert into Customers (full_name, email, phone) values (?, ?, ?)";

        try {
            CustomerValidation.validateFullName(customer.getFullName());
            CustomerValidation.validateEmail(customer.getEmail());
            CustomerValidation.validatePhone(customer.getPhone());

            Connection conn = DBConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setString(1, customer.getFullName());
                pstmt.setString(2, customer.getEmail());
                pstmt.setString(3, customer.getPhone());

                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }

        } catch (Validation e) {
            System.err.println("Validation error: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            AuditLogger.Error("Add Customer", e);
        }

        return false;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "select * from Customers";

        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    Customer cust = new Customer();
                    cust.setCustomerId(rs.getInt("customer_id"));
                    cust.setFullName(rs.getString("full_name"));
                    cust.setEmail(rs.getString("email"));
                    cust.setPhone(rs.getString("phone"));
                    cust.setRegistrationDate(rs.getDate("registration_date"));

                    customers.add(cust);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
            AuditLogger.Error("Get All Customers", e);
        }

        return customers;
    }

    /**
     * Counts how many tickets reference this customer.
     * The UI calls this BEFORE deleting, so it can warn the user with a clear
     * message instead of letting a raw foreign-key SQL error bubble up.
     */
    public int countTicketsForCustomer(int customerId) {
        String sql = "SELECT COUNT(*) FROM Tickets WHERE customer_id = ?";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Permanently deletes a customer. Returns false (without throwing) if the
     * database rejects it due to a foreign key constraint (e.g. existing tickets) —
     * the caller should check countTicketsForCustomer() first to give a clear warning.
     */
    public boolean deleteCustomer(int customerId) {
        String sql = "DELETE FROM Customers WHERE customer_id = ?";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, customerId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    AuditLogger.Activity("Customers", "DELETE", null,
                            "Customer deleted - ID: " + customerId);
                }
                return rows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            AuditLogger.Error("Delete Customer", e);
        }
        return false;
    }
}