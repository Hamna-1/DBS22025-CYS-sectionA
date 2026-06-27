package ui;

import util.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginForm extends JFrame {
    private JTextField txtEmail;
    private JPasswordField txtPassword;

    public LoginForm() {
        setTitle("Carnival Management - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Email Label and Field
        panel.add(new JLabel("Email:"));
        txtEmail = new JTextField("ali.raza@carnival.com"); // Pre-filled for testing
        panel.add(txtEmail);

        // Password Label and Field
        panel.add(new JLabel("Password (Dummy):"));
        txtPassword = new JPasswordField("123");
        panel.add(txtPassword);

        // Login Button
        JButton btnLogin = new JButton("Login");
        panel.add(new JLabel()); // Empty cell for alignment
        panel.add(btnLogin);

        add(panel);

        // Add action listener to login button
        btnLogin.addActionListener(e -> loginToDatabase());

        // Also allow pressing Enter in password field
        txtPassword.addActionListener(e -> loginToDatabase());
    }

    private void loginToDatabase() {
        String email = txtEmail.getText();

        // Validate input
        if (email.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter an email address!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }

        // SQL Query to check if employee exists and is ACTIVE
        String sql = "SELECT employee_id, full_name, status FROM Employees WHERE email = ? AND status = 'ACTIVE'";

        // Use try-with-resources to automatically close connections
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the parameter (the email)
            pstmt.setString(1, email);

            // Execute query
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Login Successful!
                int empId = rs.getInt("employee_id");
                String name = rs.getString("full_name");

                JOptionPane.showMessageDialog(this,
                        "Welcome, " + name + "!",
                        "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE);

                dispose(); // Close login window

                // Open the main dashboard
                // Note: If DashboardForm doesn't have constructor with parameter, use:
                new DashboardForm().setVisible(true);

                // OR if you updated DashboardForm to accept name:
                // new DashboardForm(name).setVisible(true);

            } else {
                // Login Failed
                JOptionPane.showMessageDialog(this,
                        "Invalid email or inactive account!\n\nPlease check:\n1. Email is correct\n2. Employee status is ACTIVE",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                txtEmail.requestFocus();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + ex.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Unexpected Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new LoginForm().setVisible(true);
        });
    }
}