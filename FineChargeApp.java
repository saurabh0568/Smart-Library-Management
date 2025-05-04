import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class FineChargeApp extends JFrame {
    private JTextField fineTextField;
    private JButton updateButton, retrieveButton;
    private JLabel fineLabel;
    private Connection conn;

    public FineChargeApp() {
        setTitle("Fine Charge Management");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize UI components
        fineTextField = new JTextField(10);
        updateButton = new JButton("Update Fine");
        retrieveButton = new JButton("Retrieve Fine");
        fineLabel = new JLabel("Current Fine Per Day: ");

        // Set up Layout
        setLayout(new FlowLayout());
        add(fineLabel);
        add(fineTextField);
        add(updateButton);
        add(retrieveButton);

        // Database Connection
        try {
            // Assuming you have set up the database URL, username, and password
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Action for the update button
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Get the value from the text field and update the database
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
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Action for the retrieve button
        retrieveButton.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                try {
                    String selectQuery = "SELECT fine_per_day FROM fine WHERE id = 1"; // Assuming only one record
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(selectQuery);

                    if (rs.next()) {
                        fineLabel.setText("Current Fine Per Day: " + rs.getBigDecimal("fine_per_day"));
                    } else {
                        JOptionPane.showMessageDialog(null, "No fine data found.");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FineChargeApp().setVisible(true);
            }
        });
    }
}
