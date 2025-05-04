import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BookRequestApp extends JFrame {
    private int sapId;  // SAP ID passed through constructor
    private JTextField titleField, authorField, genreField, publisherField, requesterField;
    private JComboBox<String> genreComboBox;
    private JButton submitButton, cancelButton;
    private boolean isDarkMode = false;
    
    // Database details - matching the ones used in Test class
    private static final String URL = "jdbc:mysql://localhost:3306/library";
    private static final String USER = "root";
    private static final String PASSWORD = "odd*357.";
    
    // Colors for light/dark mode consistency with main application
    private Color lightBg = Color.WHITE;
    private Color darkBg = new Color(45, 45, 45);
    
    public BookRequestApp(int sapId) {
        this.sapId = sapId;
        
        // Fetch requester name from database based on SAP ID
        String requesterName = fetchRequesterName(sapId);
        
        setTitle("Request New Book");
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main panel with some padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel headerLabel = new JLabel("Request a Book Addition");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(headerLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        // Book details fields
        formPanel.add(new JLabel("Book Title:"));
        titleField = new JTextField(20);
        formPanel.add(titleField);
        
        formPanel.add(new JLabel("Author:"));
        authorField = new JTextField(20);
        formPanel.add(authorField);
        
        formPanel.add(new JLabel("Genre:"));
        // Use dropdown for genres to maintain consistency
        String[] genres = {"Adult", "Autobiography", "Classic", "Dystopian", "Fantasy", "Fiction", 
                           "Finance", "History", "Programming", "Psychology", "Self-help", "Science", "Thriller", "Other"};
        genreComboBox = new JComboBox<>(genres);
        formPanel.add(genreComboBox);
        
        formPanel.add(new JLabel("Publisher:"));
        publisherField = new JTextField(20);
        formPanel.add(publisherField);
        
        formPanel.add(new JLabel("Requester Name:"));
        requesterField = new JTextField(20);
        requesterField.setText(requesterName);
        requesterField.setEditable(false);  // Pre-filled and non-editable
        formPanel.add(requesterField);
        
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        submitButton = new JButton("Submit Request");
        cancelButton = new JButton("Cancel");
        
        submitButton.addActionListener(e -> {
            if (validateFields()) {
                storeBookRequest();
            }
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel);
        
        // Add the main panel to the frame
        add(mainPanel);
        
        // Initialize UI colors (could sync with parent if needed)
        updateColors(false);
        
        setVisible(true);
    }
    
    private String fetchRequesterName(int sapId) {
        String name = "";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT name FROM students WHERE sap_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, sapId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                name = rs.getString("name");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return name.isEmpty() ? "Student #" + sapId : name;
    }
    
    private boolean validateFields() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a book title.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (authorField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an author name.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void storeBookRequest() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String genre = genreComboBox.getSelectedItem().toString();
        String publisher = publisherField.getText().trim();
        String requester = requesterField.getText().trim();
        
        String query = "INSERT INTO book_requests (title, author, genre, publisher, requester_name, sap_id, request_date) " +
                       "VALUES (?, ?, ?, ?, ?, ?, NOW())";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, genre);
            stmt.setString(4, publisher);
            stmt.setString(5, requester);
            stmt.setInt(6, sapId);
            
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, 
                "Your book request has been submitted successfully!\nThe library staff will review your request.", 
                "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
                
            dispose(); // Close the window after successful submission
            
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error submitting request: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Method to update colors for dark/light mode
    public void updateColors(boolean isDark) {
        this.isDarkMode = isDark;
        
        Color bgColor = isDark ? darkBg : lightBg;
        Color fgColor = isDark ? Color.WHITE : Color.BLACK;
        
        // Update all components with new colors
        Container content = getContentPane();
        content.setBackground(bgColor);
        
        // Update all components recursively
        updateComponentColors(content, bgColor, fgColor);
        
        repaint();
    }
    
    private void updateComponentColors(Container container, Color bg, Color fg) {
        for (Component comp : container.getComponents()) {
            comp.setBackground(bg);
            comp.setForeground(fg);
            
            if (comp instanceof Container) {
                updateComponentColors((Container) comp, bg, fg);
            }
        }
    }
    
    // For standalone testing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BookRequestApp(1001));
    }
}