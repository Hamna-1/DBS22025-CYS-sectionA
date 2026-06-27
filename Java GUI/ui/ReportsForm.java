package ui;

import report.*;
import util.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.Date;

/**
 * ReportsForm
 * -----------
 * One screen to generate all 10 PDF reports.
 * Pick a report from the dropdown -> the parameter panel below changes
 * to show only the fields that report needs -> click Generate.
 *
 * Rubric coverage: dropdowns, date selectors, panels, file menu, buttons,
 * scroll bar (description text area), PDF reports with parameters.
 */
public class ReportsForm extends JFrame {

    private JComboBox<String> cmbReportType;
    private JPanel parameterPanel;   // swaps contents depending on selected report
    private JTextArea txtDescription; // shows what the selected report does

    // --- Possible parameter fields (only the relevant ones are shown/added per report) ---
    private JSpinner spnStartDate, spnEndDate;
    private JComboBox<ComboItem> cmbShow, cmbVenue, cmbRole;
    private JComboBox<String> cmbStatus, cmbDiscountStatus;
    private JSpinner spnMinRating;

    private static final String[] REPORT_NAMES = {
            "1. Show Revenue Report",
            "2. Revenue by Venue Report",
            "3. Employee Roster Report",
            "4. Customer Registrations Report",
            "5. Ticket Sales Summary Report",
            "6. Upcoming Shows Report",
            "7. Performer Lineup Report",
            "8. Customer Feedback Report",
            "9. Equipment Maintenance History Report",
            "10. Discount Code Usage Report"
    };

    public ReportsForm() {
        setTitle("Business Reports");
        setSize(650, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setJMenuBar(createFileMenu());

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ---- Top: report selector ----
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createTitledBorder("Select Report"));
        cmbReportType = new JComboBox<>(REPORT_NAMES);
        cmbReportType.addActionListener(e -> onReportSelected());
        topPanel.add(cmbReportType, BorderLayout.NORTH);

        txtDescription = new JTextArea(3, 40);
        txtDescription.setEditable(false);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBackground(getBackground());
        topPanel.add(new JScrollPane(txtDescription), BorderLayout.CENTER); // scroll bar requirement

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ---- Middle: dynamic parameter panel ----
        parameterPanel = new JPanel();
        parameterPanel.setBorder(BorderFactory.createTitledBorder("Report Parameters"));
        JScrollPane paramScroll = new JScrollPane(parameterPanel);
        mainPanel.add(paramScroll, BorderLayout.CENTER);

        // ---- Bottom: generate button ----
        JPanel btnPanel = new JPanel();
        JButton btnGenerate = new JButton("Generate PDF Report");
        btnGenerate.addActionListener(e -> generateSelectedReport());
        btnPanel.add(btnGenerate);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);

        initParameterComponents();
        onReportSelected(); // build UI for the first report immediately
    }

    /** Creates all possible parameter fields once; we just show/hide them per report. */
    private void initParameterComponents() {
        spnStartDate = dateSpinner();
        spnEndDate = dateSpinner();

        cmbShow = new JComboBox<>();
        cmbShow.addItem(new ComboItem(0, "-- All Shows --"));
        loadComboFromTable(cmbShow, "SELECT show_id, title FROM Shows WHERE status='ACTIVE'");

        cmbVenue = new JComboBox<>();
        cmbVenue.addItem(new ComboItem(0, "-- All Venues --"));
        loadComboFromTable(cmbVenue, "SELECT venue_id, venue_name FROM Venues");

        cmbRole = new JComboBox<>();
        cmbRole.addItem(new ComboItem(0, "-- All Roles --"));
        loadComboFromTable(cmbRole, "SELECT role_id, role_name FROM Roles");

        cmbStatus = new JComboBox<>(new String[]{"ALL", "ACTIVE", "INACTIVE"});
        cmbDiscountStatus = new JComboBox<>(new String[]{"ALL", "ACTIVE", "EXPIRED"});

        spnMinRating = new JSpinner(new SpinnerNumberModel(0, 0, 5, 1)); // 0 = no minimum
    }

    private JSpinner dateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        spinner.setValue(new Date());
        return spinner;
    }

    /** Rebuilds the parameter panel based on which report is currently selected. */
    private void onReportSelected() {
        parameterPanel.removeAll();
        parameterPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        int index = cmbReportType.getSelectedIndex();

        switch (index) {
            case 0: // Show Revenue Report -> date range
                txtDescription.setText("Shows total tickets sold and revenue earned per show within a date range.");
                addDateRangeFields(gbc);
                break;

            case 1: // Revenue by Venue -> venue dropdown
                txtDescription.setText("Shows total revenue generated at each venue (or one specific venue).");
                addLabeledField(gbc, "Venue:", cmbVenue);
                break;

            case 2: // Employee Roster -> role + status
                txtDescription.setText("Lists employees, filterable by role and active/inactive status.");
                addLabeledField(gbc, "Role:", cmbRole);
                addLabeledField(gbc, "Status:", cmbStatus);
                break;

            case 3: // Customer Registrations -> date range
                txtDescription.setText("Lists customers who registered within a chosen date range.");
                addDateRangeFields(gbc);
                break;

            case 4: // Ticket Sales Summary -> show dropdown
                txtDescription.setText("Shows ticket sales totals per schedule, built from the vw_TicketSalesSummary view.");
                addLabeledField(gbc, "Show:", cmbShow);
                break;

            case 5: // Upcoming Shows -> date range
                txtDescription.setText("Lists scheduled upcoming shows with available seat counts within a date range.");
                addDateRangeFields(gbc);
                break;

            case 6: // Performer Lineup -> show dropdown
                txtDescription.setText("Lists which performers are assigned to which shows.");
                addLabeledField(gbc, "Show:", cmbShow);
                break;

            case 7: // Customer Feedback -> show + min rating
                txtDescription.setText("Lists customer feedback and ratings, filterable by show and minimum rating.");
                addLabeledField(gbc, "Show:", cmbShow);
                addLabeledField(gbc, "Minimum Rating (0 = any):", spnMinRating);
                break;

            case 8: // Equipment Maintenance -> date range
                txtDescription.setText("Lists equipment maintenance records and costs within a date range.");
                addDateRangeFields(gbc);
                break;

            case 9: // Discount Code Usage -> status dropdown
                txtDescription.setText("Lists discount codes and how many times each has been used.");
                addLabeledField(gbc, "Status:", cmbDiscountStatus);
                break;
        }

        parameterPanel.revalidate();
        parameterPanel.repaint();
    }

    private void addDateRangeFields(GridBagConstraints gbc) {
        addLabeledField(gbc, "Start Date:", spnStartDate);
        addLabeledField(gbc, "End Date:", spnEndDate);
    }

    private void addLabeledField(GridBagConstraints gbc, String label, JComponent field) {
        gbc.gridx = 0;
        parameterPanel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        parameterPanel.add(field, gbc);
        gbc.gridy++;
    }

    private void generateSelectedReport() {
        int index = cmbReportType.getSelectedIndex();

        // Ask where to save (gives the project a proper "Save As" dialog -> nice UI touch)
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(REPORT_NAMES[index].replaceAll("[^a-zA-Z0-9]", "_") + ".pdf"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        String filePath = chooser.getSelectedFile().getAbsolutePath();
        if (!filePath.toLowerCase().endsWith(".pdf")) filePath += ".pdf";

        try {
            switch (index) {
                case 0:
                    ShowRevenueReport.generate(filePath, (Date) spnStartDate.getValue(), (Date) spnEndDate.getValue());
                    break;
                case 1: {
                    ComboItem venue = (ComboItem) cmbVenue.getSelectedItem();
                    VenueRevenueReport.generate(filePath, venue.id, venue.label);
                    break;
                }
                case 2: {
                    ComboItem role = (ComboItem) cmbRole.getSelectedItem();
                    EmployeeRosterReport.generate(filePath, role.id, role.label, (String) cmbStatus.getSelectedItem());
                    break;
                }
                case 3:
                    CustomerListReport.generate(filePath, (Date) spnStartDate.getValue(), (Date) spnEndDate.getValue());
                    break;
                case 4: {
                    ComboItem show = (ComboItem) cmbShow.getSelectedItem();
                    TicketSalesSummaryReport.generate(filePath, show.id, show.label);
                    break;
                }
                case 5:
                    UpcomingShowsReport.generate(filePath, (Date) spnStartDate.getValue(), (Date) spnEndDate.getValue());
                    break;
                case 6: {
                    ComboItem show = (ComboItem) cmbShow.getSelectedItem();
                    PerformerLineupReport.generate(filePath, show.id, show.label);
                    break;
                }
                case 7: {
                    ComboItem show = (ComboItem) cmbShow.getSelectedItem();
                    int minRating = (Integer) spnMinRating.getValue();
                    CustomerFeedbackReport.generate(filePath, minRating, show.id, show.label);
                    break;
                }
                case 8:
                    EquipmentMaintenanceReport.generate(filePath, (Date) spnStartDate.getValue(), (Date) spnEndDate.getValue());
                    break;
                case 9:
                    DiscountCodeUsageReport.generate(filePath, (String) cmbDiscountStatus.getSelectedItem());
                    break;
            }

            int openNow = JOptionPane.showConfirmDialog(this,
                    "Report generated successfully!\nSaved to: " + filePath + "\n\nOpen it now?",
                    "Success", JOptionPane.YES_NO_OPTION);

            if (openNow == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(new File(filePath));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to generate report:\n" + ex.getMessage(),
                    "Report Generation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Helper to fill a JComboBox<ComboItem> from any "id, label" style query. */
    private void loadComboFromTable(JComboBox<ComboItem> combo, String sql) {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                combo.addItem(new ComboItem(rs.getInt(1), rs.getString(2)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private JMenuBar createFileMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> dispose());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(closeItem);
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
