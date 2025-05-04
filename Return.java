import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Return extends JFrame {
    private int studentId;
    private int finePerDay = 30; // default, overwritten from DB
    private JLabel fineLabel;

    public Return(int studentId) {
        this.studentId = studentId;

        setTitle("Return Issued Books - Student ID: " + studentId);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Fetch fine per day from DB
        fetchFinePerDay();

        String[] columns = {"Issue ID", "Book ID", "Book Title", "Due Date", "Quantity", "Return"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model) {
            public boolean isCellEditable(int row, int col) {
                return col == 5;
            }
        };

        table.setRowHeight(30);
        Font font = new Font("Arial", Font.PLAIN, 16);
        table.setFont(font);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 18));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(250);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        table.getColumn("Return").setCellRenderer(new ButtonRenderer());
        table.getColumn("Return").setCellEditor(new ReturnButtonEditor(new JCheckBox(), model));

        // Fine info at bottom
        fineLabel = new JLabel("  ₹" + finePerDay + " fine per day for overdue books");
        fineLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(fineLabel, BorderLayout.SOUTH);

        loadStudentBooks();
    }

    private void fetchFinePerDay() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            String query = "SELECT fine_per_day FROM fine WHERE id = 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                finePerDay = rs.getInt("fine_per_day");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching fine rate: " + e.getMessage());
        }
    }

    private void loadStudentBooks() {
        DefaultTableModel model = (DefaultTableModel) ((JTable) ((JScrollPane) getContentPane().getComponent(0)).getViewport().getView()).getModel();
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            String query = """
                SELECT ib.issue_id, ib.book_id, b.title, ib.due_date, ib.quantity 
                FROM issued_books ib
                JOIN books b ON ib.book_id = b.book_id
                WHERE ib.student_id = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getDate("due_date").toString(),
                        rs.getInt("quantity"),
                        "Return"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("Return");
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ReturnButtonEditor extends DefaultCellEditor {
        private JButton button;
        private DefaultTableModel model;
        private JTable table;
        private boolean isPushed;

        public ReturnButtonEditor(JCheckBox checkBox, DefaultTableModel model) {
            super(checkBox);
            this.model = model;
            button = new JButton("Return");
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {
            this.table = table;
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                int row = table.getSelectedRow();
                int issueId = Integer.parseInt(table.getValueAt(row, 0).toString());
                int bookId = Integer.parseInt(table.getValueAt(row, 1).toString());
                String bookTitle = table.getValueAt(row, 2).toString();
                LocalDate dueDate = LocalDate.parse(table.getValueAt(row, 3).toString());
                int quantity = Integer.parseInt(table.getValueAt(row, 4).toString());

                LocalDate today = LocalDate.now();
                long overdueDays = ChronoUnit.DAYS.between(dueDate, today);

                if (overdueDays <= 0) {
                    int choice = JOptionPane.showConfirmDialog(null,
                            "Book \"" + bookTitle + "\" returned on time.\nNo fine applicable.\nDo you want to proceed?",
                            "Return Confirmation", JOptionPane.OK_CANCEL_OPTION);

                    if (choice == JOptionPane.OK_OPTION) {
                        insertReturnRecord(issueId, 0);
                        deleteIssuedBook(issueId);
                        updateBookQuantity(bookId, quantity);
                        model.removeRow(row);
                    }
                } else {
                    int fine = (int) (overdueDays * quantity * finePerDay);

                    JPanel panel = new JPanel(new BorderLayout());
                    JLabel info = new JLabel("<html><b>Book:</b> " + bookTitle + "<br>" +
                            "<b>Overdue:</b> " + overdueDays + " day(s)<br>" +
                            "<b>Quantity:</b> " + quantity + "<br><b>Total Fine:</b> ₹" + fine + "</html>");
                    panel.add(info, BorderLayout.NORTH);

                    try {
                        ImageIcon qr = new ImageIcon("qr.png");
                        JLabel qrLabel = new JLabel(qr);
                        panel.add(qrLabel, BorderLayout.CENTER);
                    } catch (Exception ex) {
                        panel.add(new JLabel("QR code not found."), BorderLayout.CENTER);
                    }

                    int choice = JOptionPane.showConfirmDialog(null, panel, "Fine Payment", JOptionPane.OK_CANCEL_OPTION);

                    if (choice == JOptionPane.OK_OPTION) {
                        insertReturnRecord(issueId, fine);
                        deleteIssuedBook(issueId);
                        updateBookQuantity(bookId, quantity);
                        model.removeRow(row);
                    }
                }
            }
            isPushed = false;
            return "Return";
        }

        private void insertReturnRecord(int issueId, int fine) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
                String query = "INSERT INTO returns (issue_id, return_date, fine) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, issueId);
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                stmt.setInt(3, fine);
                stmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error inserting into returns table: " + e.getMessage());
            }
        }

        private void deleteIssuedBook(int issueId) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
                String query = "DELETE FROM issued_books WHERE issue_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, issueId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error deleting book: " + e.getMessage());
            }
        }

        private void updateBookQuantity(int bookId, int quantity) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
                String query = "UPDATE books SET copies_available = copies_available + ? WHERE book_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, quantity);
                stmt.setInt(2, bookId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error updating book quantity: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        String input = JOptionPane.showInputDialog("Enter Student ID:");
        if (input != null && !input.isEmpty()) {
            try {
                int studentId = Integer.parseInt(input);
                SwingUtilities.invokeLater(() -> new Return(studentId).setVisible(true));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid Student ID");
            }
        }
    }
}
