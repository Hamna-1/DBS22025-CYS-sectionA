package report;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import util.DBConnection;
import util.PdfReportBuilder;

import java.math.BigDecimal;
import java.sql.*;

/**
 * REPORT 2: Revenue by Venue
 * Parameter: venue_id (pass 0 / -1 to mean "All Venues")
 * Shows how much money each venue generated.
 */
public class VenueRevenueReport {

    public static void generate(String filePath, int venueId, String venueNameForDisplay) throws Exception {
        boolean allVenues = (venueId <= 0);

        String sql =
                "SELECT v.venue_name, COUNT(t.ticket_id) AS tickets_sold, COALESCE(SUM(p.amount),0) AS revenue " +
                        "FROM Venues v " +
                        "JOIN ShowSchedules ss ON v.venue_id = ss.venue_id " +
                        "JOIN Tickets t ON ss.schedule_id = t.schedule_id AND t.status != 'CANCELLED' " +
                        "JOIN Payments p ON t.ticket_id = p.ticket_id AND p.status = 'SUCCESS' " +
                        (allVenues ? "" : "WHERE v.venue_id = ? ") +
                        "GROUP BY v.venue_id, v.venue_name " +
                        "ORDER BY revenue DESC";

        Document document = PdfReportBuilder.createDocument(filePath, "Revenue by Venue Report",
                allVenues ? "All Venues" : "Venue: " + venueNameForDisplay);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!allVenues) {
                ps.setInt(1, venueId);
            }
            ResultSet rs = ps.executeQuery();

            PdfPTable table = PdfReportBuilder.createTable(
                    new String[]{"Venue Name", "Tickets Sold", "Revenue (PKR)"},
                    new float[]{3, 1.5f, 2});

            BigDecimal grandTotal = BigDecimal.ZERO;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                BigDecimal revenue = rs.getBigDecimal("revenue");
                grandTotal = grandTotal.add(revenue);
                PdfReportBuilder.addRow(table, rs.getString("venue_name"), rs.getInt("tickets_sold"), revenue);
            }

            if (hasData) {
                PdfReportBuilder.addTotalRow(table, "Grand Total:", grandTotal, 2);
                document.add(table);
            } else {
                PdfReportBuilder.addEmptyMessage(document, "No revenue data found for the selected venue.");
            }

        } finally {
            PdfReportBuilder.closeDocument(document);
        }
    }
}
