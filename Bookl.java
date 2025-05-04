import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Bookl extends JFrame {
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JLabel totalBooksLabel;
    private JTextField searchField; // Search text field

    // Database connection class
    public static class DBConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/library";
        private static final String USER = "root";
        private static final String PASSWORD = "odd*357.";

        public static Connection getConnection() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver"); // Load the driver
                return DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (Exception e) {
                System.out.println("Error while connecting to the database.");
                e.printStackTrace();
                return null;
            }
        }
    }

    public Bookl() {
        setTitle("📚 Book List");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Search field setup
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setToolTipText("Search by Title, Author, or Genre");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterBooks(searchField.getText());
            }
        });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Genre", "ISBN", "Available Copies"}, 0);
        bookTable = new JTable(tableModel);
        bookTable.setEnabled(false);  // Disable editing
        bookTable.setRowHeight(25);
        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // Total books label
        totalBooksLabel = new JLabel("Total Copies: 0");
        totalBooksLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(totalBooksLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        loadBooks();
        setVisible(true);
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        int totalCopies = 0;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {

            while (rs.next()) {
                int copies = rs.getInt("copies_available");
                totalCopies += copies;
                tableModel.addRow(new Object[]{
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getString("isbn"),
                    copies
                });
            }
            totalBooksLabel.setText("Total Copies: " + totalCopies);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading books: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Function to filter books based on search input
    private void filterBooks(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        bookTable.setRowSorter(sorter);

        // Apply filter based on multiple columns (title, author, genre)
        if (query.isEmpty()) {
            sorter.setRowFilter(null);  // Show all rows when search is empty
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 2, 3));  // 1 = Title, 2 = Author, 3 = Genre
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Bookl::new); // Fixed class reference
    }
}
