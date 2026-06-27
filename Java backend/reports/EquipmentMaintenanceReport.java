package report;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import util.DBConnection;
import util.PdfReportBuilder;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * REPORT 9: Equipment Maintenance History Report
 * Parameters: start date, end date
 * Uses your vw_EquipmentMaintenanceHistory VIEW.
 */
public class EquipmentMaintenanceReport {

    public static void generate(String filePath, java.util.Date startDate, java.util.Date endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String start = sdf.format(startDate);
        String end = sdf.format(endDate);

        String sql = "SELECT equipment_name, maintenance_date, cost, performed_by " +
                "FROM vw_EquipmentMaintenanceHistory " +
                "WHERE maintenance_date BETWEEN ? AND ? ORDER BY maintenance_date DESC";

        Document document = PdfReportBuilder.createDocument(filePath, "Equipment Maintenance History Report",
                "Date Range: " + start + " to " + end);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();

            PdfPTable table = PdfReportBuilder.createTable(
                    new String[]{"Equipment", "Maintenance Date", "Cost (PKR)", "Performed By"},
                    new float[]{2.2f, 1.6f, 1.3f, 2.0f});

            BigDecimal totalCost = BigDecimal.ZERO;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                BigDecimal cost = rs.getBigDecimal("cost") == null ? BigDecimal.ZERO : rs.getBigDecimal("cost");
                totalCost = totalCost.add(cost);
                PdfReportBuilder.addRow(table, rs.getString("equipment_name"), rs.getDate("maintenance_date"),
                        cost, rs.getString("performed_by"));
            }

            if (hasData) {
                PdfReportBuilder.addTotalRow(table, "Total Maintenance Cost:", totalCost, 3);
                document.add(table);
            } else {
                PdfReportBuilder.addEmptyMessage(document, "No maintenance records found in the selected date range.");
            }

        } finally {
            PdfReportBuilder.closeDocument(document);
        }
    }
}

