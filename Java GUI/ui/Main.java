package ui;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Always run Swing UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set a modern look and feel (Optional but recommended)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Open Login Screen
            new LoginForm().setVisible(true);
        });
    }
}
