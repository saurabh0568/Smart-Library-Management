import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class BorrowedBooksl extends JFrame {
    private JTable borrowedBooksTable;
    private DefaultTableModel tableModel;
    private JLabel totalBorrowedBooksLabel;
    private JLabel finePerDayLabel;  // New label for fine
    private JTextField searchField;

    // Database connection class
    public static class DBConnection {
        private static final String URL = "jdbc:mysql://localhost:3306/library";
        private static final String USER = "root";
        private static final String PASSWORD = "odd*357.";

        public static Connection getConnection() {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                return DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (Exception e) {
                System.out.println("Error while connecting to the database.");
                e.printStackTrace();
                return null;
            }
        }
    }

    public BorrowedBooksl() {
        setTitle("📚 Borrowed Books List");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Search field setup
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        searchField.setToolTipText("Search by Student Name, SAP ID, or Book Title");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterBorrowedBooks(searchField.getText());
            }
        });

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        add(searchPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{
                "Issue ID", "Student Name", "SAP ID", "Book Title", "Issue Date", "Due Date", "Quantity"}, 0);
        borrowedBooksTable = new JTable(tableModel);
        borrowedBooksTable.setEnabled(false);
        borrowedBooksTable.setRowHeight(25);
        add(new JScrollPane(borrowedBooksTable), BorderLayout.CENTER);

        // Bottom panel with total books and fine label
        totalBorrowedBooksLabel = new JLabel("Total Borrowed Books: 0");
        totalBorrowedBooksLabel.setFont(new Font("Arial", Font.BOLD, 14));

        finePerDayLabel = new JLabel("Fine per Day: ₹0");
        finePerDayLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(totalBorrowedBooksLabel);
        bottomPanel.add(Box.createHorizontalStrut(30)); // space between labels
        bottomPanel.add(finePerDayLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        loadBorrowedBooks();
        setVisible(true);
    }

    private void loadBorrowedBooks() {
        tableModel.setRowCount(0);
        int totalBorrowed = 0;

        // Load issued books
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT ib.issue_id, s.name AS student_name, s.sap_id, b.title AS book_title, " +
                             "ib.issue_date, ib.due_date, ib.quantity " +
                             "FROM issued_books ib " +
                             "JOIN students s ON ib.student_id = s.student_id " +
                             "JOIN books b ON ib.book_id = b.book_id")) {

            while (rs.next()) {
                totalBorrowed += rs.getInt("quantity");
                tableModel.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getString("student_name"),
                        rs.getString("sap_id"),
                        rs.getString("book_title"),
                        rs.getDate("issue_date"),
                        rs.getDate("due_date"),
                        rs.getInt("quantity")
                });
            }

            totalBorrowedBooksLabel.setText("Total Borrowed Books: " + totalBorrowed);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ Error loading borrowed books: " + e.getMessage());
            e.printStackTrace();
        }

        // Load fine per day
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet fineRs = stmt.executeQuery("SELECT fine_per_day FROM fine LIMIT 1")) {

            if (fineRs.next()) {
                double finePerDay = fineRs.getDouble("fine_per_day");
                finePerDayLabel.setText("Fine per Day: ₹" + finePerDay);
            }

        } catch (Exception e) {
            finePerDayLabel.setText("Fine per Day: ₹Error");
            e.printStackTrace();
        }
    }

    // Filter function
    private void filterBorrowedBooks(String query) {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        borrowedBooksTable.setRowSorter(sorter);

        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 1, 2, 3)); // Name, SAP ID, Book Title
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BorrowedBooksl::new);
    }
}
