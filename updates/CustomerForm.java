package ui;

import dataAccess.CustomerDataAccess;
import model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CustomerForm extends JFrame {
    private JTextField txtFullName, txtEmail, txtPhone;
    private JTextArea txtAddress;

    private DefaultTableModel tableModel;
    private JTable customerTable;

    public CustomerForm() {
        setTitle("Customer Management");
        setSize(750, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setJMenuBar(createFileMenu());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- Form Panel (top) ----
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; txtFullName = new JTextField(20); formPanel.add(txtFullName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; txtEmail = new JTextField(20); formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; txtPhone = new JTextField(20); formPanel.add(txtPhone, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; txtAddress = new JTextArea(3, 20); formPanel.add(new JScrollPane(txtAddress), gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> saveCustomer());
        btnPanel.add(btnSave);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        btnPanel.add(btnCancel);

        formPanel.add(btnPanel, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);

        // ---- Table Panel (bottom) — lists existing customers with a Delete button ----
        String[] columns = {"ID", "Full Name", "Email", "Phone", "Registered", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // only the Action/button column responds to clicks
            }
        };

        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(30);
        customerTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        customerTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(customerTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);

        loadCustomerData();
    }

    private void saveCustomer() {
        Customer c = new Customer();
        c.setFullName(txtFullName.getText());
        c.setEmail(txtEmail.getText());
        c.setPhone(txtPhone.getText());

        CustomerDataAccess dao = new CustomerDataAccess();
        boolean success = dao.addCustomer(c);

        if (success) {
            JOptionPane.showMessageDialog(this, "Customer saved!");
            clearForm();
            loadCustomerData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save — check your inputs.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCustomerData() {
        tableModel.setRowCount(0);
        CustomerDataAccess dao = new CustomerDataAccess();

        for (Customer c : dao.getAllCustomers()) {
            tableModel.addRow(new Object[]{
                    c.getCustomerId(),
                    c.getFullName(),
                    c.getEmail(),
                    c.getPhone(),
                    c.getRegistrationDate(),
                    "Delete"
            });
        }
    }

    /** Confirms, checks for dependent tickets, and deletes the customer at the given row. */
    private void deleteCustomerAtRow(int row) {
        int customerId = (Integer) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        CustomerDataAccess dao = new CustomerDataAccess();
        int ticketCount = dao.countTicketsForCustomer(customerId);

        if (ticketCount > 0) {
            JOptionPane.showMessageDialog(this,
                    name + " has " + ticketCount + " ticket(s) on record and cannot be deleted.\n" +
                            "Cancel or reassign their tickets first if you need to remove this customer.",
                    "Cannot Delete", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Permanently delete customer \"" + name + "\"?\nThis cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = dao.deleteCustomer(customerId);
        if (success) {
            JOptionPane.showMessageDialog(this, name + " was deleted.");
            loadCustomerData();
        } else {
            JOptionPane.showMessageDialog(this, "Delete failed. See console for details.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtFullName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtAddress.setText("");
    }

    private JMenuBar createFileMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("Exit"));
        menuBar.add(fileMenu);
        return menuBar;
    }

    // --- Custom Button Renderer for the Action column ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    // --- Custom Button Editor for the Action column ---
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
            isPushed = true;
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int rowToDelete = currentRow;
                SwingUtilities.invokeLater(() -> deleteCustomerAtRow(rowToDelete));
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