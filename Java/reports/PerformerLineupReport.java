package report;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPTable;
import util.DBConnection;
import util.PdfReportBuilder;

import java.sql.*;

/**
 * REPORT 7: Performer Lineup Report
 * Parameter: show_id (0 = all shows)
 * Uses your vw_PerformerLineup VIEW.
 */
public class PerformerLineupReport {

    public static void generate(String filePath, int showId, String showNameForDisplay) throws Exception {
        boolean allShows = (showId <= 0);

        String sql = "SELECT title, stage_name, role_in_show FROM vw_PerformerLineup " +
                (allShows ? "" : "WHERE show_id = ? ") + "ORDER BY title, stage_name";

        Document document = PdfReportBuilder.createDocument(filePath, "Performer Lineup Report",
                allShows ? "All Shows" : "Show: " + showNameForDisplay);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!allShows) ps.setInt(1, showId);
            ResultSet rs = ps.executeQuery();

            PdfPTable table = PdfReportBuilder.createTable(
                    new String[]{"Show", "Performer (Stage Name)", "Role in Show"},
                    new float[]{2.2f, 2.2f, 2.0f});

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                PdfReportBuilder.addRow(table, rs.getString("title"), rs.getString("stage_name"), rs.getString("role_in_show"));
            }

            if (hasData) {
                document.add(table);
            } else {
                PdfReportBuilder.addEmptyMessage(document, "No performer lineup data found.");
            }

        } finally {
            PdfReportBuilder.closeDocument(document);
        }
    }
}

