package ui;

import model.Customer;
import dataAccess.CustomerDataAccess;

import javax.swing.*;
import java.awt.*;

public class CustomerForm extends JFrame {
    private JTextField txtFullName, txtEmail, txtPhone;
    private JTextArea txtAddress;

    public CustomerForm() {
        setTitle("Customer Management");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setJMenuBar(createFileMenu());

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
        btnSave.addActionListener(e -> {
            Customer c = new Customer();
            c.setFullName(txtFullName.getText());
            c.setEmail(txtEmail.getText());
            c.setPhone(txtPhone.getText());

            CustomerDataAccess dao = new CustomerDataAccess();
            boolean success = dao.addCustomer(c);

            if (success) {
                JOptionPane.showMessageDialog(this, "Customer saved!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save — check your inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnPanel.add(btnSave);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        btnPanel.add(btnCancel);

        formPanel.add(btnPanel, gbc); // <-- this line was missing; without it, btnPanel never appears on screen

        add(formPanel);
    }

    private JMenuBar createFileMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem("Exit"));
        menuBar.add(fileMenu);
        return menuBar;
    }
}