package ui;

import util.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DashboardForm extends JFrame {

    private DefaultTableModel model; // kept as a field so we can refresh it later

    public DashboardForm() {
        setTitle("Carnival Management System - Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create Menu Bar FIRST
        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);

        // Main Layout
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Welcome to the Dashboard", SwingConstants.CENTER));
        add(topPanel, BorderLayout.NORTH);

        // Table — starts empty, gets filled by loadEmployeeData() below
        String[] columns = {"ID", "Name", "Role", "Status", "Action"};
        model = new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);
        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        loadEmployeeData(); // pull real rows from the database right away
    }

    /** Queries the Employees table fresh and repopulates the dashboard table. */
    private void loadEmployeeData() {
        model.setRowCount(0); // clear out whatever was there before

        String sql = "SELECT e.employee_id, e.full_name, r.role_name, e.status " +
                "FROM Employees e JOIN Roles r ON e.role_id = r.role_id " +
                "ORDER BY e.employee_id";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("employee_id"),
                        rs.getString("full_name"),
                        rs.getString("role_name"),
                        rs.getString("status"),
                        "Edit"
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load employees: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // Employee Menu
        JMenu employeeMenu = new JMenu("Employees");
        JMenuItem addEmpItem = new JMenuItem("Add Employee");
        addEmpItem.addActionListener(e -> {
            EmployeeForm form = new EmployeeForm();
            form.setVisible(true);
            // Refresh the dashboard table as soon as the Add Employee window closes,
            // so newly added employees show up without restarting the app.
            form.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    loadEmployeeData();
                }
            });
        });
        employeeMenu.add(addEmpItem);

        // Customer Menu
        JMenu customerMenu = new JMenu("Customers");
        JMenuItem addCustItem = new JMenuItem("Add Customer");
        addCustItem.addActionListener(e -> new CustomerForm().setVisible(true));
        customerMenu.add(addCustItem);

        // Ticket Menu
        JMenu ticketMenu = new JMenu("Tickets");
        JMenuItem bookTicketItem = new JMenuItem("Book Ticket");
        bookTicketItem.addActionListener(e -> new TicketBookingForm().setVisible(true));
        ticketMenu.add(bookTicketItem);

        // Show Menu
        JMenu showMenu = new JMenu("Shows");
        JMenuItem scheduleItem = new JMenuItem("Schedule Shows");
        scheduleItem.addActionListener(e -> new ShowScheduleForm().setVisible(true));
        showMenu.add(scheduleItem);

        // Venue Menu
        JMenu venueMenu = new JMenu("Venues");
        JMenuItem venueItem = new JMenuItem("Manage Venues");
        venueItem.addActionListener(e -> new VenueForm().setVisible(true));
        venueMenu.add(venueItem);

        // Reports Menu
        JMenu reportMenu = new JMenu("Reports");
        JMenuItem reportItem = new JMenuItem("Generate Reports");
        reportItem.addActionListener(e -> new ReportsForm().setVisible(true));
        reportMenu.add(reportItem);

        // Add all menus to menuBar
        menuBar.add(fileMenu);
        menuBar.add(employeeMenu);
        menuBar.add(customerMenu);
        menuBar.add(ticketMenu);
        menuBar.add(showMenu);
        menuBar.add(venueMenu);
        menuBar.add(reportMenu);

        return menuBar;
    }
}