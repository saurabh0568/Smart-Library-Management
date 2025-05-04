import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Test extends JFrame {
    int studentId;  
    int sapId;      
    String studentName;

    JPanel bookPanel;
    JTextField searchField;
    JComboBox<String> genreFilter;
    boolean isDarkMode = false;
    ArrayList<JPanel> allCards = new ArrayList<>();
    ArrayList<Book> allBooks = new ArrayList<>();
    
    // Enhanced color scheme
    Color lightBg = Color.WHITE;
    Color darkBg = new Color(30, 30, 30);
    Color lightText = Color.BLACK;
    Color darkText = Color.WHITE;
    Color lightSidebar = new Color(230, 230, 250);
    Color darkSidebar = new Color(45, 45, 55);
    
    // Store sidebar components
    JPanel sidebar;
    ArrayList<JButton> sidebarButtons = new ArrayList<>();

    class Book {
        int id;
        String title, author, genre;
        int copies;
        float rating;

        Book(int id, String t, String a, String g, int c, float r) {
            this.id = id;
            title = t;
            author = a;
            genre = g;
            copies = c;
            rating = r;
        }
    }

    public Test(int sap_id, String name) {
        this.sapId = sap_id;
        this.studentName = name;
        
        // Get the actual student_id from database based on sap_id
        fetchStudentIdFromDatabase(sap_id);
        
        setTitle("Library Student Dashboard - " + studentName);
        setSize(1200, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel welcome = new JLabel("Welcome, " + studentName, SwingConstants.CENTER);
        add(welcome);

        // === Left Sidebar with Enhanced UI ===
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(250, getHeight()));
        sidebar.setBackground(lightSidebar);

        JLabel featureLabel = new JLabel("Features", JLabel.CENTER);
        featureLabel.setFont(new Font("Arial", Font.BOLD, 22));
        featureLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        featureLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        sidebar.add(featureLabel);

        String[] buttons = {
            "View Issued Books", "Return Book", "Request New Book", "Notifications",
            "Auto Fine Calculation", "Logout"
        };
        
        // Icons for each button (using Unicode characters as a simple solution)
        String[] icons = {
            "📚", "↩️", "➕", "🔔", "💰", "🚪"
        };
        
        for (int i = 0; i < buttons.length; i++) {
            String text = buttons[i];
            String icon = icons[i];
            
            JButton btn = new JButton(icon + " " + text);
            btn.setFont(new Font("Arial", Font.BOLD, 16));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(220, 50));
            btn.setFocusPainted(false);
            btn.setBorderPainted(true);
            btn.setMargin(new Insets(10, 15, 10, 15));
            
            sidebar.add(Box.createVerticalStrut(15));
            sidebar.add(btn);
            sidebarButtons.add(btn);
        
            btn.addActionListener(e -> {
                switch (text) {
                    case "View Issued Books":
                        // Check that we have a valid student ID before showing borrowed books
                        if (studentId > 0) {
                            new BorrowedBooksUI(studentId);
                        } else {
                            JOptionPane.showMessageDialog(this, "Cannot retrieve borrowed books. Invalid student ID.");
                        }
                        break;
                    case "Return Book":
                        new Return(studentId).setVisible(true);
                        break;
                    case "Request New Book":
                        new BookRequestApp(sapId).setVisible(true);
                        break;
                    case "Notifications":
                        new Notifications(studentId, sapId).setVisible(true);
                        break;
                    case "Auto Fine Calculation":
                        new AutoFineCalculatorNoLibrary();
                        break;
                    case "Logout":
                        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?");
                        if (confirm == JOptionPane.YES_OPTION) {
                            dispose(); // or redirect to login screen
                        }
                        break;
                }
            });
        }
        
        add(sidebar, BorderLayout.WEST);

        // === Top Header ===
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel dashboardTitle = new JLabel("Student Dashboard: " + studentName);
        dashboardTitle.setFont(new Font("SansSerif", Font.BOLD, 22));

        JButton darkToggle = new JButton("Toggle Dark Mode");
        darkToggle.addActionListener(e -> toggleDarkMode());

        headerPanel.add(dashboardTitle, BorderLayout.WEST);
        headerPanel.add(darkToggle, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // === Search and Filter ===
        JPanel searchFilterPanel = new JPanel(new BorderLayout(10, 10));
        searchFilterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        searchField = new JTextField();
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> applySearchFilter());

        JPanel leftSearch = new JPanel(new BorderLayout(5, 5));
        leftSearch.add(searchField, BorderLayout.CENTER);
        leftSearch.add(searchBtn, BorderLayout.EAST);

        String[] genres = {"All", "Adult", "Autobiography", "Classic", "Dystopian", "Fantasy", "Fiction", "Finance", "History", "Programming", "Psychology", "Self-help", "Science", "Thriller"};
        genreFilter = new JComboBox<>(genres);
        genreFilter.addActionListener(e -> applySearchFilter());

        searchFilterPanel.add(leftSearch, BorderLayout.CENTER);
        searchFilterPanel.add(genreFilter, BorderLayout.EAST);

        // === Book Cards Panel ===
        bookPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        bookPanel.setBackground(lightBg);
        JScrollPane scrollPane = new JScrollPane(bookPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === Center Panel contains Search and Books ===
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(searchFilterPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadBooksFromDatabase();
        applySearchFilter(); // initial display
        setVisible(true);
    }
    
    private void fetchStudentIdFromDatabase(int sap_id) {
        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library", "root", "odd*357.");
                
            String query = "SELECT student_id FROM students WHERE sap_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, sap_id);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                this.studentId = rs.getInt("student_id");
                System.out.println("Found student_id: " + this.studentId + " for SAP ID: " + sap_id);
            } else {
                // If student not found by SAP ID, use a default or show error
                JOptionPane.showMessageDialog(this, "Student with SAP ID " + sap_id + " not found.");
                this.studentId = -1; // Use an invalid ID to indicate problem
            }
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
            this.studentId = -1;
        }
    }

    private void loadBooksFromDatabase() {
        allCards.clear();
        allBooks.clear();
        bookPanel.removeAll();

        try {
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library", "root", "odd*357."
            );

            String query = "SELECT * FROM books";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Book b = new Book(
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getInt("copies_available"),
                    rs.getFloat("rating")
                );
                allBooks.add(b);
                JPanel card = createBookCard(b);
                allCards.add(card);
                bookPanel.add(card);
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }

        bookPanel.revalidate();
        bookPanel.repaint();
    }

    private JPanel createBookCard(Book book) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 120)); // increased width for image space
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        card.setBackground(isDarkMode ? darkBg : lightBg);
    
        JLabel titleLabel = new JLabel("<html><b>" + book.title + "</b></html>");
        JLabel authorLabel = new JLabel("Author: " + book.author);
        JLabel genreLabel = new JLabel("Genre: " + book.genre);
        JLabel copiesLabel = new JLabel("Available: " + book.copies);
        JLabel ratingLabel = new JLabel("Rating: " + book.rating);
    
        // Set text color based on mode
        Color textColor = isDarkMode ? darkText : lightText;
        titleLabel.setForeground(textColor);
        authorLabel.setForeground(textColor);
        genreLabel.setForeground(textColor);
        copiesLabel.setForeground(textColor);
        ratingLabel.setForeground(textColor);
    
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(card.getBackground());
        infoPanel.add(titleLabel);
        infoPanel.add(authorLabel);
        infoPanel.add(genreLabel);
        infoPanel.add(copiesLabel);
        infoPanel.add(ratingLabel);
    
        JButton borrowButton = new JButton(book.copies == 0 ? "Unavailable" : "Borrow");
        borrowButton.setEnabled(book.copies > 0);
        if (book.copies > 0) {
            borrowButton.addActionListener(e -> showBorrowDialog(book));
        }
    
        // === Review Button ===
        JButton reviewButton = new JButton("Review");
        reviewButton.addActionListener(e -> showReviewDialog(book.id));
    
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(card.getBackground());
        bottomPanel.add(reviewButton);
        bottomPanel.add(borrowButton);
    
        // === Default Image ===
        ImageIcon defaultIcon = new ImageIcon("book.png"); // ensure this image is in your project folder
        Image scaledImage = defaultIcon.getImage().getScaledInstance(80, 100, Image.SCALE_SMOOTH);
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
        imageLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        imageLabel.setBackground(card.getBackground());
    
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(imageLabel, BorderLayout.EAST); // add image to right side
        card.add(bottomPanel, BorderLayout.SOUTH);
        return card;
    }
    
    private void showReviewDialog(int bookId) {
        // Create a dialog to add a review
        JDialog reviewDialog = new JDialog();
        reviewDialog.setTitle("Add Review");
    
        // Text area for review input
        JTextArea reviewTextArea = new JTextArea(5, 20);
        JScrollPane scrollPane = new JScrollPane(reviewTextArea);
    
        // OK button to save review
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            String reviewText = reviewTextArea.getText().trim();
            if (!reviewText.isEmpty()) {
                saveReviewToDatabase(bookId, reviewText);
            }
            reviewDialog.dispose();
        });
    
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.add(new JLabel("Enter your review:"));
        dialogPanel.add(scrollPane);
        dialogPanel.add(okButton);
    
        reviewDialog.add(dialogPanel);
        reviewDialog.pack();
        reviewDialog.setVisible(true);
    }
    
    private void saveReviewToDatabase(int bookId, String reviewText) {
        // Your database code to insert the review into the "review" table without foreign key constraint
        String query = "INSERT INTO review (book_id, review_text) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.");
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, bookId);
            ps.setString(2, reviewText);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    private void showBorrowDialog(Book book) {
        JTextField nameField = new JTextField(this.studentName);
        nameField.setEditable(false);
        JTextField emailField = new JTextField(); // Optional: Pre-fill if you store emails
        JPasswordField passwordField = new JPasswordField(); // Optional: if used

        JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, book.copies, 1));
        JTextField durationField = new JTextField("14"); // Default 14 days

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Quantity to Borrow:"));
        panel.add(quantitySpinner);
        panel.add(new JLabel("Duration (days, max 30):"));
        panel.add(durationField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Borrow Book",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            int qty = (int) quantitySpinner.getValue();
            int duration;

            try {
                duration = Integer.parseInt(durationField.getText().trim());
                if (duration <= 0 || duration > 30) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Duration must be between 1 and 30 days.");
                return;
            }

            try {
                // Check if student ID is valid
                if (this.studentId <= 0) {
                    JOptionPane.showMessageDialog(this, "Invalid student ID. Please log in again.");
                    return;
                }
                
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/library", "root", "odd*357.");
                
                // Update available copies
                String updateQuery = "UPDATE books SET copies_available = copies_available - ? WHERE book_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setInt(1, qty);
                updateStmt.setInt(2, book.id);
                updateStmt.executeUpdate();

                // Insert into issued_books
                String insertQuery = "INSERT INTO issued_books (student_id, book_id, issue_date, due_date, quantity) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

                LocalDate issueDate = LocalDate.now();
                LocalDate dueDate = issueDate.plusDays(duration);
                insertStmt.setInt(1, this.studentId);
                insertStmt.setInt(2, book.id);
                insertStmt.setDate(3, Date.valueOf(issueDate));
                insertStmt.setDate(4, Date.valueOf(dueDate));
                insertStmt.setInt(5, qty);
                insertStmt.executeUpdate();

                conn.close();
                JOptionPane.showMessageDialog(this, "Book borrowed successfully.");
                loadBooksFromDatabase(); // refresh
                applySearchFilter();

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Borrow failed: " + e.getMessage());
            }
        }
    }

    private void applySearchFilter() {
        String query = searchField.getText().trim().toLowerCase();
        String selectedGenre = genreFilter.getSelectedItem().toString().trim().toLowerCase();
        bookPanel.removeAll();
    
        for (int i = 0; i < allBooks.size(); i++) {
            Book b = allBooks.get(i);
            boolean matchesSearch = b.title.toLowerCase().contains(query) ||
                                    b.author.toLowerCase().contains(query);
            boolean matchesGenre = selectedGenre.equals("all") ||
                                   b.genre.toLowerCase().contains(selectedGenre);
    
            if (matchesSearch && matchesGenre) {
                bookPanel.add(allCards.get(i));
            }
        }
    
        bookPanel.revalidate();
        bookPanel.repaint();
    }
    
    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
    
        // Set colors based on mode
        Color bgColor = isDarkMode ? darkBg : lightBg;
        Color fgColor = isDarkMode ? darkText : lightText;
        Color sidebarBg = isDarkMode ? darkSidebar : lightSidebar;
        
        // Update sidebar appearance
        sidebar.setBackground(sidebarBg);
        for (Component c : sidebar.getComponents()) {
            if (c instanceof JLabel) {
                c.setForeground(fgColor);
            } else if (c instanceof JButton) {
                JButton btn = (JButton) c;
                btn.setBackground(isDarkMode ? new Color(60, 60, 70) : new Color(210, 210, 235));
                btn.setForeground(fgColor);
            }
        }
        
        // Update main background
        getContentPane().setBackground(bgColor);
        
        // Update header panel
        JPanel headerPanel = (JPanel)getContentPane().getComponent(1);
        headerPanel.setBackground(bgColor);
        for (Component c : headerPanel.getComponents()) {
            c.setForeground(fgColor);
            if (c instanceof JPanel) {
                c.setBackground(bgColor);
            }
        }
        
        // Update center panel (search and books)
        JPanel centerPanel = (JPanel)getContentPane().getComponent(2);
        centerPanel.setBackground(bgColor);
        
        // Update search filter panel
        JPanel searchFilterPanel = (JPanel)centerPanel.getComponent(0);
        searchFilterPanel.setBackground(bgColor);
        for (Component c : searchFilterPanel.getComponents()) {
            c.setForeground(fgColor);
            if (c instanceof JPanel) {
                JPanel panel = (JPanel)c;
                panel.setBackground(bgColor);
                for (Component inner : panel.getComponents()) {
                    if (!(inner instanceof JTextField) && !(inner instanceof JComboBox)) {
                        inner.setBackground(bgColor);
                        inner.setForeground(fgColor);
                    }
                }
            }
        }
        
        // Update book panel
        bookPanel.setBackground(bgColor);
        
        // Update all book cards
        for (JPanel card : allCards) {
            card.setBackground(bgColor);
            updateComponentsRecursively(card, bgColor, fgColor);
        }
        
        repaint();
    }
    
    // Helper method to update nested components
    private void updateComponentsRecursively(Container container, Color bg, Color fg) {
        for (Component c : container.getComponents()) {
            if (c instanceof JPanel) {
                c.setBackground(bg);
                updateComponentsRecursively((Container)c, bg, fg);
            } else if (c instanceof JLabel) {
                c.setForeground(fg);
            } else if (!(c instanceof JButton)) {
                c.setBackground(bg);
                c.setForeground(fg);
            }
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found.");
            return;
        }

        // For testing only - in real app, this would come from login page
        SwingUtilities.invokeLater(() -> new Test(1001, "Student One"));
    }
}

// Placeholder class - would be implemented in a real app
class AutoFineCalculatorNoLibrary extends JFrame {
    public AutoFineCalculatorNoLibrary() {
        setTitle("Feature Not Implemented");
        setSize(300, 150);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JLabel message = new JLabel("This feature is not implemented yet.", JLabel.CENTER);
        add(message);
        
        setVisible(true);
    }
}