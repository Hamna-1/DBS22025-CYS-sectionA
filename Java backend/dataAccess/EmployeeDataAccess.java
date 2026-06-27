package dataAccess;

import model.Employee;
import util.AuditLogger;
import util.DBConnection;
import validator.EmployeeValidation;
import exception.Validation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDataAccess  {

    public boolean addEmployee(Employee employee) {
        String query = "insert into Employees (full_name, cnic, email, phone, role_id, salary, status) values (?, ?, ?, ?, ?, ?, ?)";

        try {
            EmployeeValidation.validateFullName(employee.getFullName());
            EmployeeValidation.validateCNIC(employee.getCnic());
            EmployeeValidation.validateEmail(employee.getEmail());
            EmployeeValidation.validateSalary(employee.getSalary().doubleValue());

            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, employee.getFullName());
            ps.setString(2, employee.getCnic());
            ps.setString(3, employee.getEmail());
            ps.setString(4, employee.getPhone());
            ps.setInt(5, employee.getRoleId());
            ps.setBigDecimal(6, employee.getSalary());
            ps.setString(7, employee.getStatus());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Employee added successfully!");
                return true;
            }

        } catch (Validation e) {
            System.err.println("Validation error: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            AuditLogger.Error("Add Employee", e);
        }

        return false;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = "select * from Employees";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Employee emp = new Employee();
                emp.setEmployeeId(rs.getInt("employee_id"));
                emp.setFullName(rs.getString("full_name"));
                emp.setCnic(rs.getString("cnic"));
                emp.setEmail(rs.getString("email"));
                emp.setPhone(rs.getString("phone"));
                emp.setRoleId(rs.getInt("role_id"));
                emp.setHireDate(rs.getDate("hire_date"));
                emp.setSalary(rs.getBigDecimal("salary"));
                emp.setStatus(rs.getString("status"));

                employees.add(emp);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching employees: " + e.getMessage());
            AuditLogger.Error("Get All Employees", e);
        }

        return employees;
    }

    public boolean updateEmployee(Employee employee) {
        String query = "update Employees set full_name=?, cnic=?, email=?, phone=?, role_id=?, salary=?, status=? WHERE employee_id=?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(query);

            pstmt.setString(1, employee.getFullName());
            pstmt.setString(2, employee.getCnic());
            pstmt.setString(3, employee.getEmail());
            pstmt.setString(4, employee.getPhone());
            pstmt.setInt(5, employee.getRoleId());
            pstmt.setBigDecimal(6, employee.getSalary());
            pstmt.setString(7, employee.getStatus());
            pstmt.setInt(8, employee.getEmployeeId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
            AuditLogger.Error("Update Employee", e);
        }

        return false;
    }

    public boolean deleteEmployee(int employeeId) {
        String query = "update Employees set status='INACTIVE' where employee_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, employeeId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            AuditLogger.Error("Delete Employee", e);
        }

        return false;
    }
}