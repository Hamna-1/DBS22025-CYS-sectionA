package report;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import util.DBConnection;
import util.PdfReportBuilder;

import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * REPORT 6: Upcoming Shows Report
 * Parameters: start date, end date (defaults to today -> +30 days if not picked)
 * Uses your vw_UpcomingShows VIEW.
 */
public class UpcomingShowsReport {

    public static void generate(String filePath, java.util.Date startDate, java.util.Date endDate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String start = sdf.format(startDate);
        String end = sdf.format(endDate);

        String sql = "SELECT title, venue_name, show_date, start_time, available_seats " +
                "FROM vw_UpcomingShows WHERE show_date BETWEEN ? AND ? ORDER BY show_date, start_time";

        Document document = PdfReportBuilder.createDocument(filePath, "Upcoming Shows Report",
                "Date Range: " + start + " to " + end);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, start);
            ps.setString(2, end);
            ResultSet rs = ps.executeQuery();

            PdfPTable table = PdfReportBuilder.createTable(
                    new String[]{"Show", "Venue", "Date", "Start Time", "Seats Left"},
                    new float[]{2.2f, 1.8f, 1.3f, 1.3f, 1.2f});

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                PdfReportBuilder.addRow(table, rs.getString("title"), rs.getString("venue_name"),
                        rs.getDate("show_date"), rs.getTime("start_time"), rs.getInt("available_seats"));
            }

            if (hasData) {
                document.add(table);
            } else {
                PdfReportBuilder.addEmptyMessage(document, "No upcoming shows scheduled in this date range.");
            }

        } finally {
            PdfReportBuilder.closeDocument(document);
        }
    }
}

