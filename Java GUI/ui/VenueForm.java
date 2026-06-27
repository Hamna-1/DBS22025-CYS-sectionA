package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import util.DBConnection;

public class VenueForm extends JFrame {
    private JTextField txtVenueName, txtLocation, txtCapacity;
    private JRadioButton radAvailable, radMaintenance;
    private JTable venueTable;
    private DefaultTableModel tableModel;

    public VenueForm() {
        setTitle("Venue Management");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setJMenuBar(createFileMenu());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Venue Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Venue Name:"), gbc);
        gbc.gridx = 1;
        txtVenueName = new JTextField(20);
        formPanel.add(txtVenueName, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 1;
        txtLocation = new JTextField(20);
        formPanel.add(txtLocation, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        txtCapacity = new JTextField(10);
        formPanel.add(txtCapacity, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        JPanel radioPanel = new JPanel();
        radAvailable = new JRadioButton("Available", true);
        radMaintenance = new JRadioButton("Under Maintenance");
        ButtonGroup group = new ButtonGroup();
        group.add(radAvailable);
        group.add(radMaintenance);
        radioPanel.add(radAvailable);
        radioPanel.add(radMaintenance);
        formPanel.add(radioPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        JButton btnSave = new JButton("Save Venue");
        btnSave.addActionListener(e -> saveVenue());
        btnPanel.add(btnSave);
        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> clearForm());
        btnPanel.add(btnClear);
        formPanel.add(btnPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Venue Name", "Location", "Capacity", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        venueTable = new JTable(tableModel);
        venueTable.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(venueTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
        loadVenueData();
    }

    private void saveVenue() {
        String sql = "INSERT INTO Venues (venue_name, location, capacity, status) VALUES (?, ?, ?, ?)";

        // NOTE: Connection is intentionally NOT inside try-with-resources.
        // DBConnection hands out one shared Connection for the whole app —
        // closing it here would break any other method still relying on it.
        // Only the PreparedStatement (something WE created) gets auto-closed.
        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, txtVenueName.getText());
                pstmt.setString(2, txtLocation.getText());
                pstmt.setInt(3, Integer.parseInt(txtCapacity.getText()));
                pstmt.setString(4, radAvailable.isSelected() ? "AVAILABLE" : "UNDER_MAINTENANCE");

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Venue saved successfully!");
                    loadVenueData();
                    clearForm();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadVenueData() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM Venues";

        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("venue_id"),
                            rs.getString("venue_name"),
                            rs.getString("location"),
                            rs.getInt("capacity"),
                            rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        txtVenueName.setText("");
        txtLocation.setText("");
        txtCapacity.setText("");
        radAvailable.setSelected(true);
    }

    private JMenuBar createFileMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("Exit"));
        menuBar.add(fileMenu);
        return menuBar;
    }
}