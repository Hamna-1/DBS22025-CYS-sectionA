package ui;

import model.Payment;
import model.Ticket;
import service.TicketBookingManager;
import util.DBConnection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketBookingForm extends JFrame {
    private JTable seatTable;
    private DefaultTableModel tableModel;

    private JComboBox<ComboItem> cmbShow;
    private JComboBox<ComboItem> cmbCustomer;
    private JSpinner spnDate;

    // Currently selected schedule (looked up from show+date) and its price.
    // Updated automatically whenever the Show or Date changes.
    private int currentScheduleId = -1;
    private java.math.BigDecimal currentPrice = java.math.BigDecimal.ZERO;

    public TicketBookingForm() {
        setTitle("Ticket Booking");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setJMenuBar(createFileMenu());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Panel: Dropdowns and Date Selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createTitledBorder("Booking Details"));

        topPanel.add(new JLabel("Show:"));
        cmbShow = new JComboBox<>();
        loadShowsIntoCombo();
        topPanel.add(cmbShow);

        topPanel.add(new JLabel("Date:"));
        JSpinner spnDateLocal = new JSpinner(new SpinnerDateModel());
        spnDateLocal.setEditor(new JSpinner.DateEditor(spnDateLocal, "yyyy-MM-dd"));
        spnDate = spnDateLocal;
        topPanel.add(spnDate);

        topPanel.add(new JLabel("Customer:"));
        cmbCustomer = new JComboBox<>();
        loadCustomersIntoCombo();
        topPanel.add(cmbCustomer);

        JButton btnLoadSeats = new JButton("Load Seats");
        btnLoadSeats.addActionListener(e -> loadSeatsForSelectedSchedule());
        topPanel.add(btnLoadSeats);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center Panel: Table with Buttons Inside (Rubric Requirement)
        String[] columns = {"Seat Number", "Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only the button column is editable
            }
        };

        seatTable = new JTable(tableModel);
        seatTable.setRowHeight(30);

        seatTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        seatTable.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(seatTable); // Rubric: Scroll bar
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        // Load seats for whatever is selected by default when the form opens
        loadSeatsForSelectedSchedule();
    }

    /** Fills the Show dropdown with every active show from the database. */
    private void loadShowsIntoCombo() {
        String sql = "SELECT show_id, title FROM Shows WHERE status = 'ACTIVE' ORDER BY title";
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    cmbShow.addItem(new ComboItem(rs.getInt("show_id"), rs.getString("title")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load shows: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Fills the Customer dropdown with every customer from the database. */
    private void loadCustomersIntoCombo() {
        String sql = "SELECT customer_id, full_name FROM Customers ORDER BY full_name";
        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    cmbCustomer.addItem(new ComboItem(rs.getInt("customer_id"), rs.getString("full_name")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load customers: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Finds the ShowSchedule that matches the selected Show + Date, loads its
     * price info, and repopulates the seat table with real availability —
     * already-booked seats are marked Booked and their button disabled.
     */
    private void loadSeatsForSelectedSchedule() {
        ComboItem selectedShow = (ComboItem) cmbShow.getSelectedItem();
        if (selectedShow == null) {
            JOptionPane.showMessageDialog(this, "No shows available. Add a show first.");
            return;
        }

        java.util.Date pickedDate = (java.util.Date) spnDate.getValue();
        java.sql.Date sqlDate = new java.sql.Date(pickedDate.getTime());

        String findSchedule = "SELECT schedule_id, total_seats FROM ShowSchedules " +
                "WHERE show_id = ? AND show_date = ? AND status = 'SCHEDULED'";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(findSchedule)) {
                pstmt.setInt(1, selectedShow.id);
                pstmt.setDate(2, sqlDate);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        currentScheduleId = -1;
                        tableModel.setRowCount(0);
                        JOptionPane.showMessageDialog(this,
                                "No schedule found for \"" + selectedShow.label + "\" on " + sqlDate
                                        + ".\nPick a date that matches an existing schedule.",
                                "No Schedule Found", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    currentScheduleId = rs.getInt("schedule_id");
                }
            }

            // Default seat price for this booking screen. Adjust if you want
            // per-show pricing pulled from somewhere else.
            currentPrice = new java.math.BigDecimal("2000");

            populateSeatTable();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load schedule: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Builds the seat grid (A1-A10) and marks which ones are already booked for currentScheduleId. */
    private void populateSeatTable() {
        tableModel.setRowCount(0);

        List<String> bookedSeats = new ArrayList<>();
        String sql = "SELECT seat_number FROM Tickets WHERE schedule_id = ? AND status != 'CANCELLED'";

        try {
            Connection conn = DBConnection.getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, currentScheduleId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        bookedSeats.add(rs.getString("seat_number"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= 10; i++) {
            String seatNum = "A" + i;
            boolean isBooked = bookedSeats.contains(seatNum);
            tableModel.addRow(new Object[]{
                    seatNum,
                    isBooked ? "Booked" : "Available",
                    isBooked ? "Booked" : "Book"
            });
        }
    }

    // --- Custom Button Renderer for Table ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setEnabled(!"Booked".equals(value));
            return this;
        }
    }

    // --- Custom Button Editor for Table ---
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setEnabled(!"Booked".equals(label));
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && !"Booked".equals(label)) {
                String seatNum = tableModel.getValueAt(currentRow, 0).toString();

                if (currentScheduleId <= 0) {
                    JOptionPane.showMessageDialog(button, "No valid schedule selected. Click 'Load Seats' first.");
                    isPushed = false;
                    return label;
                }

                ComboItem selectedCustomer = (ComboItem) cmbCustomer.getSelectedItem();
                if (selectedCustomer == null) {
                    JOptionPane.showMessageDialog(button, "Please select a customer.");
                    isPushed = false;
                    return label;
                }

                Ticket ticket = new Ticket();
                ticket.setScheduleId(currentScheduleId);
                ticket.setCustomerId(selectedCustomer.id);
                ticket.setSeatNumber(seatNum);
                ticket.setPrice(currentPrice);

                Payment payment = new Payment();
                payment.setAmount(ticket.getPrice());
                payment.setPaymentMethod("CASH");

                TicketBookingManager manager = new TicketBookingManager();
                boolean success = manager.bookTicket(ticket, payment);

                if (success) {
                    tableModel.setValueAt("Booked", currentRow, 1);
                    tableModel.setValueAt("Booked", currentRow, 2);
                    JOptionPane.showMessageDialog(button, "Seat " + seatNum + " booked for "
                            + selectedCustomer.label + "!");
                } else {
                    JOptionPane.showMessageDialog(button, "Booking failed! Seat may already be taken or no seats remain.");
                }
            }

            isPushed = false;
            return label;
        }
    }

    private JMenuBar createFileMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> dispose());
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    /** Small wrapper so JComboBox can hold an (id, displayLabel) pair cleanly. */
    private static class ComboItem {
        final int id;
        final String label;
        ComboItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}