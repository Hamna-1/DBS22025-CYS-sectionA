package report;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import util.DBConnection;
import util.PdfReportBuilder;

import java.sql.*;

/**
 * REPORT 3: Employee Roster Report
 * Parameters: role_id (0 = all roles), status ("ALL", "ACTIVE", "INACTIVE")
 */
public class EmployeeRosterReport {

    public static void generate(String filePath, int roleId, String roleNameForDisplay, String status) throws Exception {
        boolean allRoles = (roleId <= 0);
        boolean allStatus = (status == null || status.equalsIgnoreCase("ALL"));

        StringBuilder sql = new StringBuilder(
                "SELECT e.full_name, r.role_name, e.email, e.phone, e.hire_date, e.salary, e.status " +
                        "FROM Employees e JOIN Roles r ON e.role_id = r.role_id WHERE 1=1 ");

        if (!allRoles) sql.append("AND e.role_id = ? ");
        if (!allStatus) sql.append("AND e.status = ? ");
        sql.append("ORDER BY e.full_name");

        StringBuilder filterDesc = new StringBuilder();
        filterDesc.append("Role: ").append(allRoles ? "All" : roleNameForDisplay);
        filterDesc.append(" | Status: ").append(allStatus ? "All" : status);

        Document document = PdfReportBuilder.createDocument(filePath, "Employee Roster Report", filterDesc.toString());

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (!allRoles) ps.setInt(idx++, roleId);
            if (!allStatus) ps.setString(idx++, status);

            ResultSet rs = ps.executeQuery();

            PdfPTable table = PdfReportBuilder.createTable(
                    new String[]{"Name", "Role", "Email", "Phone", "Hire Date", "Salary", "Status"},
                    new float[]{2.2f, 1.5f, 2.2f, 1.4f, 1.3f, 1.3f, 1.2f});

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                PdfReportBuilder.addRow(table,
                        rs.getString("full_name"), rs.getString("role_name"), rs.getString("email"),
                        rs.getString("phone"), rs.getDate("hire_date"), rs.getBigDecimal("salary"), rs.getString("status"));
            }

            if (hasData) {
                document.add(table);
            } else {
                PdfReportBuilder.addEmptyMessage(document, "No employees found for the selected filters.");
            }

        } finally {
            PdfReportBuilder.closeDocument(document);
        }
    }
}

