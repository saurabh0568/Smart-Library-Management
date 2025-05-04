import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BookManagement extends JFrame {
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JPanel formPanel;
    private JTextField titleField, authorField, genreField, isbnField;
    private JButton saveButton;

    public BookManagement() {
        setTitle("📚 Book Management System");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("➕ Add Book");
        topPanel.add(addButton);
        add(topPanel, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Genre", "ISBN", "Available", "Edit", "Delete"}, 0);
        bookTable = new JTable(tableModel);
        bookTable.setRowHeight(30);
        loadBooks();

        // Add Edit/Delete button renderers
        Action editAction = new AbstractAction("✏️") {
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                editBook(row);
            }
        };

        Action deleteAction = new AbstractAction("🗑️") {
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                deleteBook(row);
            }
        };

        new ButtonColumn(bookTable, editAction, 6);
        new ButtonColumn(bookTable, deleteAction, 7);

        add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // Form Panel (initially hidden)
        formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Book"));
        titleField = new JTextField();
        authorField = new JTextField();
        genreField = new JTextField();
        isbnField = new JTextField();
        saveButton = new JButton("Save");

        formPanel.add(new JLabel("Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Author:"));
        formPanel.add(authorField);
        formPanel.add(new JLabel("Genre:"));
        formPanel.add(genreField);
        formPanel.add(new JLabel("ISBN:"));
        formPanel.add(isbnField);
        formPanel.add(new JLabel(""));
        formPanel.add(saveButton);

        formPanel.setVisible(false);
        add(formPanel, BorderLayout.SOUTH);

        // Show/Hide form
        addButton.addActionListener(e -> formPanel.setVisible(!formPanel.isVisible()));

        // Save action
        saveButton.addActionListener(e -> addBook());

        setVisible(true);
    }

    private void loadBooks() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM books");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getString("isbn"),
                    rs.getInt("copies_available"),
                    "Edit", "Delete"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addBook() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO books (title, author, genre, isbn, copies_available) VALUES (?, ?, ?, ?, 5)"
            );
            stmt.setString(1, titleField.getText().trim());
            stmt.setString(2, authorField.getText().trim());
            stmt.setString(3, genreField.getText().trim());
            stmt.setString(4, isbnField.getText().trim());
            stmt.executeUpdate();

            clearForm();
            loadBooks();
            JOptionPane.showMessageDialog(this, "✅ Book added!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Failed to add book.");
        }
    }

    private void deleteBook(int row) {
        int bookId = (int) tableModel.getValueAt(row, 0);
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM books WHERE book_id = ?");
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            loadBooks();
            JOptionPane.showMessageDialog(this, "🗑️ Book deleted!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void editBook(int row) {
        int bookId = (int) tableModel.getValueAt(row, 0);
        String newAvailable = JOptionPane.showInputDialog(this, "Enter new available copies:", tableModel.getValueAt(row, 5));
        if (newAvailable == null || newAvailable.isEmpty()) return;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE books SET copies_available = ? WHERE book_id = ?");
            stmt.setInt(1, Integer.parseInt(newAvailable));
            stmt.setInt(2, bookId);
            stmt.executeUpdate();
            loadBooks();
            JOptionPane.showMessageDialog(this, "✅ Book updated!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        titleField.setText("");
        authorField.setText("");
        genreField.setText("");
        isbnField.setText("");
        formPanel.setVisible(false);
    }

    public static void main(String[] args) {
        new BookManagement();
    }
}
