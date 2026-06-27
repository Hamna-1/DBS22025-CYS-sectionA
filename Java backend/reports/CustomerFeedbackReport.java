package report;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import util.DBConnection;
import util.PdfReportBuilder;

import java.sql.*;

/**
 * REPORT 8: Customer Feedback Report
 * Parameters: minRating (1-5, pass 0 for "no minimum"), show_id (0 = all shows)
 * Uses your vw_CustomerFeedbackOverview VIEW.
 */
public class CustomerFeedbackReport {

    public static void generate(String filePath, int minRating, int showId, String showNameForDisplay) throws Exception {
        boolean allShows = (showId <= 0);
        boolean noMinRating = (minRating <= 0);

        StringBuilder sql = new StringBuilder(
                "SELECT f.full_name, f.title, f.rating, f.feedback_date FROM vw_CustomerFeedbackOverview f WHERE 1=1 ");
        if (!allShows) sql.append("AND f.title = (SELECT title FROM Shows WHERE show_id = ?) ");
        if (!noMinRating) sql.append("AND f.rating >= ? ");
        sql.append("ORDER BY f.feedback_date DESC");

        StringBuilder filterDesc = new StringBuilder();
        filterDesc.append("Show: ").append(allShows ? "All" : showNameForDisplay);
        filterDesc.append(" | Min Rating: ").append(noMinRating ? "Any" : minRating + " stars");

        Document document = PdfReportBuilder.createDocument(filePath, "Customer Feedback Report", filterDesc.toString());

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (!allShows) ps.setInt(idx++, showId);
            if (!noMinRating) ps.setInt(idx++, minRating);

            ResultSet rs = ps.executeQuery();

            PdfPTable table = PdfReportBuilder.createTable(
                    new String[]{"Customer", "Show", "Rating", "Feedback Date"},
                    new float[]{2.2f, 2.2f, 1.0f, 1.6f});

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                PdfReportBuilder.addRow(table, rs.getString("full_name"), rs.getString("title"),
                        rs.getInt("rating") + " / 5", rs.getTimestamp("feedback_date"));
            }

            if (hasData) {
                document.add(table);
            } else {
                PdfReportBuilder.addEmptyMessage(document, "No feedback found matching the selected filters.");
            }

        } finally {
            PdfReportBuilder.closeDocument(document);
        }
    }
}

