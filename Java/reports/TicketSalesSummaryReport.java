package report;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import util.DBConnection;
import util.PdfReportBuilder;

import java.math.BigDecimal;
import java.sql.*;

/**
 * REPORT 5: Ticket Sales Summary Report
 * Parameter: show_id (0 = all shows)
 * Uses the vw_TicketSalesSummary VIEW you already created in SQL — good example
 * of a report built directly off one of your 7 required views.
 */
public class TicketSalesSummaryReport {

    public static void generate(String filePath, int showId, String showNameForDisplay) throws Exception {
        boolean allShows = (showId <= 0);

        String sql =
                "SELECT v.title, v.schedule_id, v.tickets_sold, v.gross_sales " +
                        "FROM vw_TicketSalesSummary v " +
                        (allShows ? "" : "JOIN ShowSchedules ss ON v.schedule_id = ss.schedule_id WHERE ss.show_id = ? ") +
                        "ORDER BY v.schedule_id";

        Document document = PdfReportBuilder.createDocument(filePath, "Ticket Sales Summary Report",
                allShows ? "All Shows" : "Show: " + showNameForDisplay);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!allShows) ps.setInt(1, showId);
            ResultSet rs = ps.executeQuery();

            PdfPTable table = PdfReportBuilder.createTable(
                    new String[]{"Show Title", "Schedule ID", "Tickets Sold", "Gross Sales (PKR)"},
                    new float[]{2.5f, 1.2f, 1.3f, 1.5f});

            BigDecimal grandTotal = BigDecimal.ZERO;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                BigDecimal sales = rs.getBigDecimal("gross_sales") == null ? BigDecimal.ZERO : rs.getBigDecimal("gross_sales");
                grandTotal = grandTotal.add(sales);
                PdfReportBuilder.addRow(table, rs.getString("title"), rs.getInt("schedule_id"),
                        rs.getInt("tickets_sold"), sales);
            }

            if (hasData) {
                PdfReportBuilder.addTotalRow(table, "Grand Total:", grandTotal, 3);
                document.add(table);
            } else {
                PdfReportBuilder.addEmptyMessage(document, "No ticket sales data found.");
            }

        } finally {
            PdfReportBuilder.closeDocument(document);
        }
    }
}

