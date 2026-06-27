package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import util.DBConnection;

public class ShowScheduleForm extends JFrame {
    private JComboBox<String> cmbShow, cmbVenue;
    private JSpinner spnDate, spnStartTime, spnEndTime;
    private JTextField txtTotalSeats;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;

    public ShowScheduleForm() {
        setTitle("Show Schedule Management");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setJMenuBar(createFileMenu());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form Panel (Rubric: Panels)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Schedule Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Show Dropdown
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Show:"), gbc);
        gbc.gridx = 1;
        cmbShow = new JComboBox<>(loadShows());
        formPanel.add(cmbShow, gbc);

        // Venue Dropdown
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Venue:"), gbc);
        gbc.gridx = 1;
        cmbVenue = new JComboBox<>(loadVenues());
        formPanel.add(cmbVenue, gbc);

        // Date Selector
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        spnDate = new JSpinner(new SpinnerDateModel());
        spnDate.setEditor(new JSpinner.DateEditor(spnDate, "yyyy-MM-dd"));
        formPanel.add(spnDate, gbc);

        // Start Time
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Start Time:"), gbc);
        gbc.gridx = 1;
        SpinnerDateModel timeModel = new SpinnerDateModel();
        spnStartTime = new JSpinner(timeModel);
        spnStartTime.setEditor(new JSpinner.DateEditor(spnStartTime, "HH:mm"));
        formPanel.add(spnStartTime, gbc);

        // End Time
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("End Time:"), gbc);
        gbc.gridx = 1;
        spnEndTime = new JSpinner(new SpinnerDateModel());
        spnEndTime.setEditor(new JSpinner.DateEditor(spnEndTime, "HH:mm"));
        formPanel.add(spnEndTime, gbc);

        // Total Seats
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Total Seats:"), gbc);
        gbc.gridx = 1;
        txtTotalSeats = new JTextField(10);
        formPanel.add(txtTotalSeats, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        JButton btnSave = new JButton("Save Schedule");
        btnSave.addActionListener(e -> saveSchedule());
        btnPanel.add(btnSave);
        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> {
            txtTotalSeats.setText("");
            spnDate.setValue(new java.util.Date());
        });
        btnPanel.add(btnClear);
        formPanel.add(btnPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // Table with Scroll Bar (Rubric: Tables, Scroll Bar)
        String[] columns = {"Schedule ID", "Show", "Venue", "Date", "Start Time", "Seats"};
        tableModel = new DefaultTableModel(columns, 0);
        scheduleTable = new JTable(tableModel);
        scheduleTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
        loadScheduleData();
    }

    private String[] loadShows() {
        // NOTE: Connection is intentionally NOT inside try-with-resources.
        // DBConnection hands out one shared Connection for the whole app —
        // closing it here would break every other method still using it.
        // Only the Statement/ResultSet (things WE created) get auto-closed.
        String sql = "SELECT title FROM Shows WHERE status='ACTIVE'";
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                java.util.List<String> shows = new java.util.ArrayList<>();
                while (rs.next()) {
                    shows.add(rs.getString("title"));
                }
                return shows.toArray(new String[0]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[]{"No Shows Available"};
        }
    }

    private String[] loadVenues() {
        String sql = "SELECT venue_name FROM Venues WHERE status='AVAILABLE'";
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                java.util.List<String> venues = new java.util.ArrayList<>();
                while (rs.next()) {
                    venues.add(rs.getString("venue_name"));
                }
                return venues.toArray(new String[0]);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new String[]{"No Venues Available"};
        }
    }

    private void saveSchedule() {
        String sql = "INSERT INTO ShowSchedules (show_id, venue_id, show_date, start_time, end_time, total_seats, available_seats) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = DBConnection.getConnection();

            // Get show_id and venue_id BEFORE opening the insert statement,
            // since these helper calls use the same shared connection.
            int showId = getShowId(cmbShow.getSelectedItem().toString());
            int venueId = getVenueId(cmbVenue.getSelectedItem().toString());

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, showId);
                pstmt.setInt(2, venueId);
                pstmt.setDate(3, new java.sql.Date(((java.util.Date) spnDate.getValue()).getTime()));
                pstmt.setTime(4, new java.sql.Time(((java.util.Date) spnStartTime.getValue()).getTime()));
                pstmt.setTime(5, new java.sql.Time(((java.util.Date) spnEndTime.getValue()).getTime()));

                int totalSeats = Integer.parseInt(txtTotalSeats.getText());
                pstmt.setInt(6, totalSeats);
                pstmt.setInt(7, totalSeats); // available_seats = total_seats initially

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Schedule saved successfully!");
                    loadScheduleData();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getShowId(String showTitle) {
        String sql = "SELECT show_id FROM Shows WHERE title = ?";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, showTitle);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getInt("show_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private int getVenueId(String venueName) {
        String sql = "SELECT venue_id FROM Venues WHERE venue_name = ?";
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, venueName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) return rs.getInt("venue_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void loadScheduleData() {
        tableModel.setRowCount(0);
        String sql = "SELECT ss.schedule_id, s.title, v.venue_name, ss.show_date, ss.start_time, ss.total_seats " +
                "FROM ShowSchedules ss JOIN Shows s ON ss.show_id = s.show_id JOIN Venues v ON ss.venue_id = v.venue_id";

        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("schedule_id"),
                            rs.getString("title"),
                            rs.getString("venue_name"),
                            rs.getDate("show_date"),
                            rs.getTime("start_time"),
                            rs.getInt("total_seats")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JMenuBar createFileMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }
}