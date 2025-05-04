import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BookIssueSystem extends JFrame {
    private JPanel mainPanel;
    private CardLayout cardLayout;

    // DB Credentials
    private final String DB_URL = "jdbc:mysql://localhost:3306/library";
    private final String DB_USER = "root";
    private final String DB_PASS = "odd*357.";

    public BookIssueSystem() {
        setTitle("Library Book Issue");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Option Panel
        JPanel optionPanel = new JPanel();
        JButton scanBtn = new JButton("Scan Barcode");
        JButton formBtn = new JButton("Fill Form");

        optionPanel.add(scanBtn);
        optionPanel.add(formBtn);

        // Scan Panel (placeholder)
        JPanel scanPanel = new JPanel();
        scanPanel.add(new JLabel("Barcode scanning not yet implemented."));
        JButton back1 = new JButton("Back");
        scanPanel.add(back1);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        JTextField bookIdField = new JTextField();
        JTextField bookNameField = new JTextField();
        JTextField sapIdField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField durationField = new JTextField();
        JTextField quantityField = new JTextField();
        JButton okButton = new JButton("OK");
        JButton back2 = new JButton("Back");

        formPanel.add(new JLabel("Book ID:"));
        formPanel.add(bookIdField);
        formPanel.add(new JLabel("Book Name:"));
        formPanel.add(bookNameField);
        formPanel.add(new JLabel("SAP ID:"));
        formPanel.add(sapIdField);
        formPanel.add(new JLabel("Student Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Duration (days):"));
        formPanel.add(durationField);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(quantityField);
        formPanel.add(okButton);
        formPanel.add(back2);

        // Add panels to card layout
        mainPanel.add(optionPanel, "options");
        mainPanel.add(scanPanel, "scan");
        mainPanel.add(formPanel, "form");

        add(mainPanel);
        cardLayout.show(mainPanel, "options");

        // Button actions
        scanBtn.addActionListener(e -> cardLayout.show(mainPanel, "scan"));
        formBtn.addActionListener(e -> cardLayout.show(mainPanel, "form"));
        back1.addActionListener(e -> cardLayout.show(mainPanel, "options"));
        back2.addActionListener(e -> cardLayout.show(mainPanel, "options"));

        okButton.addActionListener(e -> {
            try {
                int bookId = Integer.parseInt(bookIdField.getText().trim());
                String title = bookNameField.getText().trim();
                String sapId = sapIdField.getText().trim();
                String studentName = nameField.getText().trim();
                int duration = Integer.parseInt(durationField.getText().trim());
                int quantity = Integer.parseInt(quantityField.getText().trim());
        
                if (duration >= 20) {
                    JOptionPane.showMessageDialog(this, "Duration must be less than 20 days.");
                    return;
                }
        
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        
                // Step 1: Fetch student_id
                String studentQuery = "SELECT student_id FROM students WHERE sap_id = ?";
                PreparedStatement studentStmt = conn.prepareStatement(studentQuery);
                studentStmt.setString(1, sapId);
                ResultSet studentRs = studentStmt.executeQuery();
        
                if (!studentRs.next()) {
                    JOptionPane.showMessageDialog(this, "Student not found with SAP ID: " + sapId);
                    conn.close();
                    return;
                }
        
                int studentId = studentRs.getInt("student_id");
        
                // Step 2: Check book availability
                String bookQuery = "SELECT copies_available FROM books WHERE book_id = ?";
                PreparedStatement bookStmt = conn.prepareStatement(bookQuery);
                bookStmt.setInt(1, bookId);
                ResultSet bookRs = bookStmt.executeQuery();
        
                if (!bookRs.next()) {
                    JOptionPane.showMessageDialog(this, "Book not found with ID: " + bookId);
                    conn.close();
                    return;
                }
        
                int available = bookRs.getInt("copies_available");
                if (available < quantity) {
                    JOptionPane.showMessageDialog(this, "Only " + available + " copies are available.");
                    conn.close();
                    return;
                }
        
                // Step 3: Reduce available copies
                String updateBook = "UPDATE books SET copies_available = copies_available - ? WHERE book_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateBook);
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, bookId);
                updateStmt.executeUpdate();
        
                // Step 4: Insert into issued_books
                String issueInsert = "INSERT INTO issued_books (student_id, book_id, issue_date, due_date, quantity) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement issueStmt = conn.prepareStatement(issueInsert);
                java.sql.Date issueDate = new java.sql.Date(System.currentTimeMillis());
                java.sql.Date dueDate = new java.sql.Date(System.currentTimeMillis() + (long) duration * 24 * 60 * 60 * 1000);
        
                issueStmt.setInt(1, studentId);
                issueStmt.setInt(2, bookId);
                issueStmt.setDate(3, issueDate);
                issueStmt.setDate(4, dueDate);
                issueStmt.setInt(5, quantity);
        
                issueStmt.executeUpdate();
        
                JOptionPane.showMessageDialog(this, "Book(s) issued and recorded successfully!");
                conn.close();
        
                // Close the window after success
                this.dispose();
        
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        
        
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BookIssueSystem().setVisible(true);
        });
    }
}


