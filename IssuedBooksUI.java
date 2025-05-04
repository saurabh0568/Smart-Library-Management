import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class IssuedBooksUI extends JFrame {
    private int finePerDay; // Fetched from DB

    public IssuedBooksUI() {
        setTitle("Issued & Overdue Books");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Fetch fine_per_day
        finePerDay = fetchFinePerDay();

        String[] columns = {"Issue ID", "Book ID", "Book Title", "Student Name", "SAP ID", "Issue Date", "Due Date", "Quantity", "Notify", "Fine"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model) {
            public boolean isCellEditable(int row, int column) {
                return column == 8 || column == 9; // only Notify and Fine buttons
            }
        };

        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Button Renderers and Editors
        table.getColumn("Notify").setCellRenderer(new ButtonRenderer());
        table.getColumn("Fine").setCellRenderer(new ButtonRenderer());
        table.getColumn("Notify").setCellEditor(new ButtonEditor(new JCheckBox(), true));
        table.getColumn("Fine").setCellEditor(new ButtonEditor(new JCheckBox(), false));

        // Load Data
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            String query = """
                    SELECT ib.issue_id, ib.book_id, b.title AS book_title, s.name, s.sap_id, ib.issue_date, ib.due_date, ib.quantity
                    FROM issued_books ib
                    JOIN books b ON ib.book_id = b.book_id
                    JOIN students s ON ib.student_id = s.student_id
                    """;

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("issue_id"),
                        rs.getInt("book_id"),
                        rs.getString("book_title"),
                        rs.getString("name"),
                        rs.getString("sap_id"),
                        rs.getDate("issue_date").toString(),
                        rs.getDate("due_date").toString(),
                        rs.getInt("quantity"),
                        "Notify",
                        "Fine"
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }

        // Display Fine Per Day at the bottom
        JLabel fineLabel = new JLabel("Fine Per Day: ₹" + finePerDay);
        fineLabel.setFont(new Font("Arial", Font.BOLD, 16));
        fineLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(fineLabel, BorderLayout.SOUTH);
    }

    // Fetch fine_per_day from the DB
    private int fetchFinePerDay() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            String query = "SELECT fine_per_day FROM fine WHERE id = 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("fine_per_day");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to fetch fine per day.\n" + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        return 30; // Default fallback
    }

    // Button Renderer
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());

            LocalDate dueDate = LocalDate.parse(table.getValueAt(row, 6).toString());
            LocalDate today = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(today, dueDate);

            if (column == 8) { // Notify
                setEnabled(daysBetween <= 10 && daysBetween >= 0);
            } else if (column == 9) { // Fine
                setEnabled(daysBetween < 0);
            }

            return this;
        }
    }

    // Button Editor
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean isNotify;

        public ButtonEditor(JCheckBox checkBox, boolean isNotify) {
            super(checkBox);
            this.isNotify = isNotify;
            button = new JButton();
            button.setOpaque(true);
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                    int row, int column) {
            button.setText((value == null) ? "" : value.toString());

            // Clear previous listeners
            for (ActionListener al : button.getActionListeners()) {
                button.removeActionListener(al);
            }

            String bookTitle = table.getValueAt(row, 2).toString();
            String studentName = table.getValueAt(row, 3).toString();
            String sapId = table.getValueAt(row, 4).toString();
            String dueDateStr = table.getValueAt(row, 6).toString();
            int quantity = Integer.parseInt(table.getValueAt(row, 7).toString());

            LocalDate dueDate = LocalDate.parse(dueDateStr);
            LocalDate today = LocalDate.now();

            if (isNotify) {
                button.addActionListener(e -> {
                    String msg = "Reminder\n\nStudent: " + studentName + "\nSAP ID: " + sapId +
                            "\nBook: " + bookTitle + "\nDue Date: " + dueDate +
                            "\n\nPlease reissue or return the book.";

                    JOptionPane.showMessageDialog(null, msg, "Notify Student", JOptionPane.INFORMATION_MESSAGE);

                    // Insert notification into database
                    insertNotification(sapId, msg);
                });
            } else {
                button.addActionListener(e -> {
                    if (today.isAfter(dueDate)) {
                        long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                        int fine = (int) (overdueDays * quantity * finePerDay);
                        String msg = "Student: " + studentName + "\nSAP ID: " + sapId + "\nBook: " + bookTitle +
                                "\nDue Date: " + dueDate + "\nOverdue Days: " + overdueDays +
                                "\nBooks: " + quantity + "\n\nTotal Fine: ₹" + fine;

                        JOptionPane.showMessageDialog(null, msg, "Fine Details", JOptionPane.WARNING_MESSAGE);

                        // Insert fine notification into database
                        insertNotification(sapId, msg);
                    } else {
                        JOptionPane.showMessageDialog(null, "Book is not overdue.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }

            return button;
        }

        public Object getCellEditorValue() {
            return button.getText();
        }

        // Helper method to insert notification
        private void insertNotification(String sapId, String message) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
                String insert = "INSERT INTO notifications (sap_id, message) VALUES (?, ?)";
                PreparedStatement stmt = conn.prepareStatement(insert);
                stmt.setString(1, sapId);
                stmt.setString(2, message);
                stmt.executeUpdate();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to insert notification.\n" + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IssuedBooksUI().setVisible(true));
    }
}
