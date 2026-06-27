package report;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import util.DBConnection;
import util.PdfReportBuilder;

import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * REPORT 4: Customer List / New Registrations Report
 * Parameters: registration start date, registration end date
 */
public class CustomerListReport {

    public static void generate(String filePath, java.util.Date startDate, java.util.Date endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String start = sdf.format(startDate);
        String end = sdf.format(endDate);

        String sql = "SELECT full_name, email, phone, registration_date FROM Customers " +
                "WHERE registration_date BETWEEN ? AND ? ORDER BY registration_date";

        Document document = PdfReportBuilder.createDocument(filePath, "Customer Registrations Report",
                "Registered Between: " + start + " and " + end);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();

            PdfPTable table = PdfReportBuilder.createTable(
                    new String[]{"Full Name", "Email", "Phone", "Registration Date"},
                    new float[]{2.2f, 2.5f, 1.5f, 1.5f});

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                PdfReportBuilder.addRow(table, rs.getString("full_name"), rs.getString("email"),
                        rs.getString("phone"), rs.getDate("registration_date"));
            }

            if (hasData) {
                document.add(table);
            } else {
                PdfReportBuilder.addEmptyMessage(document, "No customers registered in the selected date range.");
            }

        } finally {
            PdfReportBuilder.closeDocument(document);
        }
    }
}

