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
            try (PreparedStatement ps = conn.prepareStatement(query)) {

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
        String query = "select e.*, r.role_name from Employees e JOIN Roles r ON e.role_id = r.role_id";

        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                while (rs.next()) {
                    employees.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching employees: " + e.getMessage());
            AuditLogger.Error("Get All Employees", e);
        }

        return employees;
    }

    /** Fetches a single employee (with role name joined in) — used by the Dashboard's Edit button. */
    public Employee getEmployeeById(int employeeId) {
        String query = "select e.*, r.role_name from Employees e JOIN Roles r ON e.role_id = r.role_id WHERE e.employee_id = ?";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, employeeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employee: " + e.getMessage());
            AuditLogger.Error("Get Employee By Id", e);
        }
        return null;
    }

    private Employee mapRow(ResultSet rs) throws SQLException {
        Employee emp = new Employee();
        emp.setEmployeeId(rs.getInt("employee_id"));
        emp.setFullName(rs.getString("full_name"));
        emp.setCnic(rs.getString("cnic"));
        emp.setEmail(rs.getString("email"));
        emp.setPhone(rs.getString("phone"));
        emp.setRoleId(rs.getInt("role_id"));
        emp.setRoleName(rs.getString("role_name"));
        emp.setHireDate(rs.getDate("hire_date"));
        emp.setSalary(rs.getBigDecimal("salary"));
        emp.setStatus(rs.getString("status"));
        return emp;
    }

    public boolean updateEmployee(Employee employee) {
        String query = "update Employees set full_name=?, cnic=?, email=?, phone=?, role_id=?, salary=?, status=? WHERE employee_id=?";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {

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
            }

        } catch (SQLException e) {
            System.err.println("Error updating employee: " + e.getMessage());
            AuditLogger.Error("Update Employee", e);
        }

        return false;
    }

    /** Soft-delete (kept for compatibility): marks the employee INACTIVE instead of removing them. */
    public boolean deactivateEmployee(int employeeId) {
        String query = "update Employees set status='INACTIVE' where employee_id=?";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, employeeId);
                int rowsAffected = ps.executeUpdate();
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error deactivating employee: " + e.getMessage());
            AuditLogger.Error("Deactivate Employee", e);
        }

        return false;
    }

    /**
     * Counts records in other tables that reference this employee
     * (Performers, MaintenanceRecords performed_by, AuditLogs performed_by).
     * The UI calls this BEFORE deleting, to give a clear warning instead of
     * letting a raw foreign-key SQL error bubble up.
     */
    public int countDependentRecords(int employeeId) {
        int total = 0;
        String[] queries = {
                "SELECT COUNT(*) FROM Performers WHERE employee_id = ?",
                "SELECT COUNT(*) FROM MaintenanceRecords WHERE performed_by = ?",
                "SELECT COUNT(*) FROM AuditLogs WHERE performed_by = ?"
        };

        try {
            Connection conn = DBConnection.getConnection();
            for (String sql : queries) {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, employeeId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) total += rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * Permanently deletes an employee. Returns false (without throwing) if the
     * database rejects it due to a foreign key constraint — the caller should
     * check countDependentRecords() first to give a clear warning.
     */
    public boolean deleteEmployee(int employeeId) {
        String sql = "DELETE FROM Employees WHERE employee_id = ?";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, employeeId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    AuditLogger.Activity("Employees", "DELETE", null,
                            "Employee deleted - ID: " + employeeId);
                }
                return rows > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            AuditLogger.Error("Delete Employee", e);
        }
        return false;
    }

    /** Looks up role_id from a role_name. Used by EmployeeForm so the dropdown maps correctly. */
    public static int getRoleIdByName(String roleName) {
        String sql = "SELECT role_id FROM Roles WHERE role_name = ?";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, roleName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("role_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // fallback if not found
    }
}