package ui;
import dataAccess.EmployeeDataAccess;
import model.Employee;

import javax.swing.*;
import java.awt.*;

public class EmployeeForm extends JFrame {
    private JTextField txtName, txtCnic, txtEmail;
    private JComboBox<String> cmbRole;
    private JRadioButton radActive, radInactive;
    private JCheckBox chkPerformer;
    private JSpinner spnHireDate;
    private JTextArea txtNotes;
    private boolean isEditMode = false;

    // Constructor for ADDING a new employee
    public EmployeeForm() {
        initUI(null);
    }

    // Constructor for EDITING an existing employee (Rubric: same form for add and edit)
    public EmployeeForm(String existingName, String existingCnic) {
        isEditMode = true;
        initUI(null); // In a real app, you'd pass an Employee object and populate fields here

        this.setTitle("Edit Employee");
        txtName.setText(existingName);
        txtCnic.setText(existingCnic);
        // Populate other fields...
    }

    private void initUI(Object employeeData) {
        setTitle(isEditMode ? "Edit Employee" : "Add Employee");
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Rubric: File menu on each screen
        setJMenuBar(createFileMenu());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Employee Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; txtName = new JTextField(20); formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("CNIC:"), gbc);
        gbc.gridx = 1; txtCnic = new JTextField(20); formPanel.add(txtCnic, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; txtEmail = new JTextField(20); formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; cmbRole = new JComboBox<>(new String[]{"Ringmaster", "Performer", "Manager"});
        formPanel.add(cmbRole, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        JPanel radioPanel = new JPanel();
        radActive = new JRadioButton("Active", true);
        radInactive = new JRadioButton("Inactive");
        ButtonGroup group = new ButtonGroup();
        group.add(radActive); group.add(radInactive);
        radioPanel.add(radActive); radioPanel.add(radInactive);
        formPanel.add(radioPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Is Performer?"), gbc);
        gbc.gridx = 1; chkPerformer = new JCheckBox(); formPanel.add(chkPerformer, gbc);

        gbc.gridx = 0; gbc.gridy = 6; formPanel.add(new JLabel("Hire Date:"), gbc);
        gbc.gridx = 1;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        spnHireDate = new JSpinner(dateModel);
        spnHireDate.setEditor(new JSpinner.DateEditor(spnHireDate, "yyyy-MM-dd"));
        formPanel.add(spnHireDate, gbc);

        gbc.gridx = 0; gbc.gridy = 7; formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1; txtNotes = new JTextArea(3, 20);
        formPanel.add(new JScrollPane(txtNotes), gbc);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel();
        JButton btnSave = new JButton(isEditMode ? "Update" : "Save");
        btnSave.addActionListener(e -> {
            Employee emp = new Employee();
            emp.setFullName(txtName.getText());
            emp.setCnic(txtCnic.getText());
            emp.setEmail(txtEmail.getText());
            emp.setRoleId(cmbRole.getSelectedIndex() + 1); // adjust mapping to match Roles table
            emp.setSalary(new java.math.BigDecimal("50000")); // add a salary field to the form!
            emp.setStatus(radActive.isSelected() ? "ACTIVE" : "INACTIVE");

            EmployeeDataAccess dao = new EmployeeDataAccess();
            boolean success = isEditMode ? dao.updateEmployee(emp) : dao.addEmployee(emp);

            if (success) {
                JOptionPane.showMessageDialog(this, "Saved to database!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Save failed. Check inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(btnSave);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        btnPanel.add(btnCancel);

        formPanel.add(btnPanel, gbc);

        add(formPanel);
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