package report;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import util.DBConnection;
import util.PdfReportBuilder;

import java.sql.*;

/**
 * REPORT 10: Discount Code Usage Report
 * Parameter: statusFilter -> "ALL", "ACTIVE" (valid_to >= today), "EXPIRED" (valid_to < today)
 * Shows how much each discount code has been used, useful for marketing analysis.
 */
public class DiscountCodeUsageReport {

    public static void generate(String filePath, String statusFilter) throws Exception {
        boolean allCodes = (statusFilter == null || statusFilter.equalsIgnoreCase("ALL"));
        boolean activeOnly = "ACTIVE".equalsIgnoreCase(statusFilter);

        StringBuilder sql = new StringBuilder(
                "SELECT code, discount_percent, valid_from, valid_to, max_uses, times_used " +
                        "FROM DiscountCodes WHERE 1=1 ");

        if (activeOnly) {
            sql.append("AND valid_to >= CURRENT_DATE ");
        } else if (!allCodes) { // EXPIRED
            sql.append("AND valid_to < CURRENT_DATE ");
        }
        sql.append("ORDER BY times_used DESC");

        Document document = PdfReportBuilder.createDocument(filePath, "Discount Code Usage Report",
                "Status Filter: " + (allCodes ? "All Codes" : statusFilter));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString());
             ResultSet rs = ps.executeQuery()) {

            PdfPTable table = PdfReportBuilder.createTable(
                    new String[]{"Code", "Discount %", "Valid From", "Valid To", "Max Uses", "Times Used"},
                    new float[]{1.6f, 1.2f, 1.4f, 1.4f, 1.2f, 1.2f});

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                PdfReportBuilder.addRow(table,
                        rs.getString("code"), rs.getBigDecimal("discount_percent") + "%",
                        rs.getDate("valid_from"), rs.getDate("valid_to"),
                        rs.getInt("max_uses"), rs.getInt("times_used"));
            }

            if (hasData) {
                document.add(table);
            } else {
                PdfReportBuilder.addEmptyMessage(document, "No discount codes found for the selected filter.");
            }

        } finally {
            PdfReportBuilder.closeDocument(document);
        }
    }
}
