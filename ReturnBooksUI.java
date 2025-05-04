import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReturnBooksUI extends JFrame {

    private double finePerDay = 30.0; // Default, will be overwritten from DB

    public ReturnBooksUI() {
        setTitle("Return Issued Books");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Table setup
        String[] columns = {"Issue ID", "Book ID", "Student ID", "Due Date", "Quantity", "Return"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model) {
            public boolean isCellEditable(int row, int col) {
                return col == 5;
            }
        };
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Get fine per day from DB
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            Statement fineStmt = conn.createStatement();
            ResultSet fineRs = fineStmt.executeQuery("SELECT fine_per_day FROM fine WHERE id = 1");
            if (fineRs.next()) {
                finePerDay = fineRs.getDouble("fine_per_day");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DB Error (fine): " + e.getMessage());
        }

        // Label for fine_per_day at the bottom
        JLabel fineLabel = new JLabel("Fine per day: ₹" + finePerDay);
        fineLabel.setFont(new Font("Arial", Font.BOLD, 14));
        fineLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(fineLabel, BorderLayout.SOUTH);

        // Load data from issued_books
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            String query = "SELECT issue_id, book_id, student_id, due_date, quantity FROM issued_books";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getInt("book_id"),
                        rs.getInt("student_id"),
                        rs.getDate("due_date").toString(),
                        rs.getInt("quantity"),
                        "Return"
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DB Error (issued_books): " + e.getMessage());
        }

        table.getColumn("Return").setCellRenderer(new ButtonRenderer());
        table.getColumn("Return").setCellEditor(new ReturnButtonEditor(new JCheckBox(), model));
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
                int studentId = Integer.parseInt(table.getValueAt(row, 2).toString());
                LocalDate dueDate = LocalDate.parse(table.getValueAt(row, 3).toString());
                int quantity = Integer.parseInt(table.getValueAt(row, 4).toString());

                LocalDate today = LocalDate.now();
                long overdueDays = ChronoUnit.DAYS.between(dueDate, today);

                if (overdueDays <= 0) {
                    int choice = JOptionPane.showConfirmDialog(null,
                            "Book returned on time.\nNo fine applicable.\nDo you want to proceed?",
                            "Return Confirmation", JOptionPane.OK_CANCEL_OPTION);

                    if (choice == JOptionPane.OK_OPTION) {
                        insertReturnRecord(issueId, today, 0.00);
                        updateBookQuantity(bookId, quantity);
                        deleteIssuedBook(issueId);
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(null, "Book returned successfully.");
                    }
                } else {
                    double fine = overdueDays * quantity * finePerDay;

                    JPanel panel = new JPanel(new BorderLayout());
                    JLabel info = new JLabel("<html><b>Overdue:</b> " + overdueDays + " day(s)<br>" +
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
                        insertReturnRecord(issueId, today, fine);
                        updateBookQuantity(bookId, quantity);
                        deleteIssuedBook(issueId);
                        model.removeRow(row);
                        JOptionPane.showMessageDialog(null, "Book returned with fine ₹" + fine);
                    }
                }
            }
            isPushed = false;
            return "Return";
        }

        private void insertReturnRecord(int issueId, LocalDate returnDate, double fine) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
                String query = "INSERT INTO returns (issue_id, return_date, fine) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, issueId);
                stmt.setDate(2, Date.valueOf(returnDate));
                stmt.setDouble(3, fine);
                stmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error saving return info: " + e.getMessage());
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

        private void deleteIssuedBook(int issueId) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
                String query = "DELETE FROM issued_books WHERE issue_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, issueId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error deleting issued record: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReturnBooksUI().setVisible(true));
    }
}
