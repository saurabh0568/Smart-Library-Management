import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BorrowedBooksUI extends JFrame {
    public JTable table;
    private DefaultTableModel model;
    private int studentId;

    public BorrowedBooksUI(int studentId) {
        System.out.println("Student ID: " + studentId);
        this.studentId = studentId;

        setTitle("Borrowed Books");
        setSize(1000, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Changed to DISPOSE_ON_CLOSE to not exit the entire application
        setLocationRelativeTo(null);

        model = new DefaultTableModel(new String[]{"Issue ID", "Title", "Author", "Genre", "Issue Date", "Due Date", "Reissue Count", "Action"}, 0);
        table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only "Action" column is editable (for the button)
            }
        };

        // Increase row height
        table.setRowHeight(30);

        // Increase font size for better readability
        Font font = new Font("Arial", Font.PLAIN, 16);
        table.setFont(font);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18)); // Bold header font with larger size

        // Increase column width for better spacing
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Issue ID
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Title
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Author
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Genre
        table.getColumnModel().getColumn(4).setPreferredWidth(150); // Issue Date
        table.getColumnModel().getColumn(5).setPreferredWidth(150); // Due Date
        table.getColumnModel().getColumn(6).setPreferredWidth(120); // Reissue Count
        table.getColumnModel().getColumn(7).setPreferredWidth(150); // Action (Reissue)

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        loadBorrowedBooks();
        setVisible(true); // Make the frame visible
    }

    private void loadBorrowedBooks() {
        model.setRowCount(0); // Clear existing

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            String query = """
                SELECT ib.issue_id, b.title, b.author, b.genre, ib.issue_date, ib.due_date, ib.reissue_count
                FROM issued_books ib
                JOIN books b ON ib.book_id = b.book_id
                WHERE ib.student_id = ?
            """;

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[]{
                    rs.getInt("issue_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getDate("issue_date"),
                    rs.getDate("due_date"),
                    rs.getInt("reissue_count"),
                    "Reissue"
                };
                model.addRow(row);
            }

            // Add Button Renderer & Editor for Reissue
            table.getColumn("Action").setCellRenderer(new ButtonRenderer());
            table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), this));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
    }

    public void reissueBook(int issueId, int currentCount) {
        if (currentCount >= 3) {
            JOptionPane.showMessageDialog(this, "You can't reissue more than 3 times.");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            String updateQuery = "UPDATE issued_books SET due_date = DATE_ADD(due_date, INTERVAL 7 DAY), reissue_count = reissue_count + 1 WHERE issue_id = ?";
            PreparedStatement ps = con.prepareStatement(updateQuery);
            ps.setInt(1, issueId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Book reissued successfully!");
                loadBorrowedBooks();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during reissue: " + e.getMessage());
        }
    }

    // Remove the main method since we'll be launching this from the Test class
}

// Custom button renderer
class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
    public ButtonRenderer() {
        setText("Reissue");
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
}

// Custom button editor
class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private BorrowedBooksUI parent;
    private int currentRow;

    public ButtonEditor(JCheckBox checkBox, BorrowedBooksUI parent) {
        super(checkBox);
        this.parent = parent;
        button = new JButton("Reissue");
        button.addActionListener(e -> {
            try {
                int issueId = Integer.parseInt(String.valueOf(parent.table.getValueAt(currentRow, 0)));
                int count = Integer.parseInt(String.valueOf(parent.table.getValueAt(currentRow, 6)));
                parent.reissueBook(issueId, count);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error parsing values from table: " + ex.getMessage());
            }
        });
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentRow = row;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return "Reissue";
    }
}