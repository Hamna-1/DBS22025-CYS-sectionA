package report;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import util.DBConnection;
import util.PdfReportBuilder;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * REPORT 1: Show Revenue Report
 * Parameters: start date, end date
 * Shows total tickets sold + revenue per show within the date range.
 */
public class ShowRevenueReport {

    public static void generate(String filePath, java.util.Date startDate, java.util.Date endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String start = sdf.format(startDate);
        String end = sdf.format(endDate);

        String sql =
                "SELECT s.title, COUNT(t.ticket_id) AS tickets_sold, COALESCE(SUM(p.amount),0) AS revenue " +
                        "FROM Shows s " +
                        "JOIN ShowSchedules ss ON s.show_id = ss.show_id " +
                        "JOIN Tickets t ON ss.schedule_id = t.schedule_id AND t.status != 'CANCELLED' " +
                        "JOIN Payments p ON t.ticket_id = p.ticket_id AND p.status = 'SUCCESS' " +
                        "WHERE ss.show_date BETWEEN ? AND ? " +
                        "GROUP BY s.show_id, s.title " +
                        "ORDER BY revenue DESC";

        Document document = PdfReportBuilder.createDocument(filePath, "Show Revenue Report",
                "Date Range: " + start + " to " + end);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();

            PdfPTable table = PdfReportBuilder.createTable(
                    new String[]{"Show Title", "Tickets Sold", "Revenue (PKR)"},
                    new float[]{3, 1.5f, 2});

            BigDecimal grandTotal = BigDecimal.ZERO;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                BigDecimal revenue = rs.getBigDecimal("revenue");
                grandTotal = grandTotal.add(revenue);
                PdfReportBuilder.addRow(table, rs.getString("title"), rs.getInt("tickets_sold"), revenue);
            }

            if (hasData) {
                PdfReportBuilder.addTotalRow(table, "Grand Total:", grandTotal, 2);
                document.add(table);
            } else {
                PdfReportBuilder.addEmptyMessage(document, "No revenue data found for the selected date range.");
            }

        } finally {
            PdfReportBuilder.closeDocument(document);
        }
    }
}

