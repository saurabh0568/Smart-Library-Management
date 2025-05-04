import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

public class FineAndReturnApp extends JFrame {
    private JTextField fineTextField;
    private JButton updateButton, retrieveButton;
    private JLabel fineLabel;
    private Connection conn;

    private JTable returnTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JLabel totalFineLabel;

    private JPanel cardsPanel;  // Panel to hold the different "cards" (sections)
    private CardLayout cardLayout;  // CardLayout to switch between sections

    public FineAndReturnApp() {
        setTitle("Library Management");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize CardLayout and main panel for sections
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // Create the Fine Charge panel and Return List panel
        JPanel fineChargePanel = createFineChargePanel();
        JPanel returnListPanel = createReturnListPanel();

        // Add both panels to the CardLayout container
        cardsPanel.add(fineChargePanel, "Fine Charge Management");
        cardsPanel.add(returnListPanel, "Returned Books");

        // Create the button panel for navigation
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton showFineChargeButton = new JButton("Fine Charge Management");
        showFineChargeButton.addActionListener(e -> cardLayout.show(cardsPanel, "Fine Charge Management"));

        JButton showReturnListButton = new JButton("Returned Books");
        showReturnListButton.addActionListener(e -> cardLayout.show(cardsPanel, "Returned Books"));

        buttonPanel.add(showFineChargeButton);
        buttonPanel.add(showReturnListButton);

        // Add components to the frame
        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);
        add(cardsPanel, BorderLayout.CENTER);

        // Default to show Fine Charge Management panel
        cardLayout.show(cardsPanel, "Fine Charge Management");

        // Initialize the database connection for Fine Charge Management
        initializeDatabaseConnection();

        setVisible(true);
    }

    private void initializeDatabaseConnection() {
        try {
            // Database connection for Fine Charge Management
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JPanel createFineChargePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        fineTextField = new JTextField(10);
        updateButton = new JButton("Update Fine");
        retrieveButton = new JButton("Retrieve Fine");
        fineLabel = new JLabel("Current Fine Per Day: ");

        // Set up Action for the update button
        updateButton.addActionListener(e -> updateFine());

        // Set up Action for the retrieve button
        retrieveButton.addActionListener(e -> retrieveFine());

        panel.add(fineLabel);
        panel.add(fineTextField);
        panel.add(updateButton);
        panel.add(retrieveButton);

        // Label to show this section
        panel.setBorder(BorderFactory.createTitledBorder("Fine Charge Management"));

        return panel;
    }

    private JPanel createReturnListPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Search Bar
        searchField = new JTextField(20);
        searchField.setToolTipText("Search by Return ID or Issue ID");
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterReturns(searchField.getText());
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search: "));
        topPanel.add(searchField);
        panel.add(topPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Return ID", "Issue ID", "Return Date", "Fine (₹)"}, 0);
        returnTable = new JTable(tableModel);
        returnTable.setEnabled(false);
        returnTable.setRowHeight(25);
        panel.add(new JScrollPane(returnTable), BorderLayout.CENTER);

        // Total Fine Label
        totalFineLabel = new JLabel("Total Fine: ₹0");
        totalFineLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(totalFineLabel);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        loadReturns();

        // Label to show this section
        panel.setBorder(BorderFactory.createTitledBorder("Returned Books"));

        return panel;
    }

    private void updateFine() {
        try {
            String fineValue = fineTextField.getText();
            if (fineValue.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter a fine value!");
            } else {
                String updateQuery = "UPDATE fine SET fine_per_day = ? WHERE id = 1"; // Assuming only one record
                PreparedStatement stmt = conn.prepareStatement(updateQuery);
                stmt.setBigDecimal(1, new java.math.BigDecimal(fineValue));
                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(null, "Fine charge updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "Update failed. Please check your query.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void retrieveFine() {
        try {
            String selectQuery = "SELECT fine_per_day FROM fine WHERE id = 1"; // Assuming only one record
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(selectQuery);

            if (rs.next()) {
                fineLabel.setText("Current Fine Per Day: " + rs.getBigDecimal("fine_per_day"));
            } else {
                JOptionPane.showMessageDialog(null, "No fine data found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadReturns() {
        tableModel.setRowCount(0);
        int totalFine = 0;

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM returns")) {

            while (rs.next()) {
                int fine = rs.getInt("fine");
                totalFine += fine;
                tableModel.addRow(new Object[]{
                        rs.getInt("return_id"),
                        rs.getInt("issue_id"),
                        rs.getDate("return_date"),
                        "₹" + fine
                });
            }
            totalFineLabel.setText("Total Fine: ₹" + totalFine);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading returns: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterReturns(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        returnTable.setRowSorter(sorter);
        if (query.trim().isEmpty()) {
            sorter.setRowFilter(null);  // No filter
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 0, 1));  // Filter on Return ID and Issue ID (column 0 and 1)
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FineAndReturnApp::new);
    }
}
