package ui;

import dataAccess.EmployeeDataAccess;
import model.Employee;
import util.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DashboardForm extends JFrame {

    private DefaultTableModel model; // kept as a field so we can refresh it later
    private JTable table;

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

        // Table — starts empty, gets filled by loadEmployeeData() below.
        // Columns 4 (Edit) and 5 (Delete) hold real buttons, not just text.
        String[] columns = {"ID", "Name", "Role", "Status", "Edit", "Delete"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5; // only the two button columns respond to clicks
            }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new EditButtonEditor(new JCheckBox()));
        table.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new DeleteButtonEditor(new JCheckBox()));

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

        try {
            Connection conn = DBConnection.getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("employee_id"),
                            rs.getString("full_name"),
                            rs.getString("role_name"),
                            rs.getString("status"),
                            "Edit",
                            "Delete"
                    });
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load employees: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Opens EmployeeForm in edit mode for the given employee_id, refreshing the table when it closes. */
    private void openEditForm(int employeeId) {
        EmployeeDataAccess dao = new EmployeeDataAccess();
        Employee employee = dao.getEmployeeById(employeeId);

        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Could not load employee #" + employeeId,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EmployeeForm form = new EmployeeForm(employee);
        form.setVisible(true);
        form.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadEmployeeData();
            }
        });
    }

    /** Confirms, checks for dependent records, and deletes the employee at the given row. */
    private void deleteEmployeeAtRow(int row) {
        int employeeId = (Integer) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);

        EmployeeDataAccess dao = new EmployeeDataAccess();
        int dependentCount = dao.countDependentRecords(employeeId);

        if (dependentCount > 0) {
            int forceDeactivate = JOptionPane.showConfirmDialog(this,
                    name + " has " + dependentCount + " related record(s) (performer profile, " +
                            "maintenance logs, or audit history) and cannot be permanently deleted.\n\n" +
                            "Would you like to mark them INACTIVE instead?",
                    "Cannot Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (forceDeactivate == JOptionPane.YES_OPTION) {
                if (dao.deactivateEmployee(employeeId)) {
                    JOptionPane.showMessageDialog(this, name + " marked as INACTIVE.");
                    loadEmployeeData();
                }
            }
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete employee \"" + name + "\"?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = dao.deleteEmployee(employeeId);
        if (success) {
            JOptionPane.showMessageDialog(this, name + " was deleted.");
            loadEmployeeData();
        } else {
            JOptionPane.showMessageDialog(this, "Delete failed. See console for details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
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

    // --- Shared Button Renderer for both Edit and Delete columns ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // --- Button Editor for the Edit column ---
    class EditButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public EditButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int employeeId = (Integer) model.getValueAt(currentRow, 0);
                SwingUtilities.invokeLater(() -> openEditForm(employeeId));
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    // --- Button Editor for the Delete column ---
    class DeleteButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;

        public DeleteButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int rowToDelete = currentRow;
                SwingUtilities.invokeLater(() -> deleteEmployeeAtRow(rowToDelete));
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}