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
            PreparedStatement pstmt = conn.prepareStatement(query);

            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhone());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

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

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
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

        } catch (SQLException e) {
            System.err.println("Error fetching customers: " + e.getMessage());
            AuditLogger.Error("Get All Customers", e);
        }

        return customers;
    }
}