import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReturnBook extends JFrame {
    JTextField studentIdField, bookIdField;
    JTextArea resultArea;

    public ReturnBook() {
        setTitle("Return Book");
        setSize(500, 400);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        add(new JLabel("Student ID:"));
        studentIdField = new JTextField(20); add(studentIdField);

        add(new JLabel("Book ID:"));
        bookIdField = new JTextField(20); add(bookIdField);

        JButton returnBtn = new JButton("Return Book");
        add(returnBtn);

        resultArea = new JTextArea(10, 40);
        add(new JScrollPane(resultArea));

        returnBtn.addActionListener(e -> returnBook());

        setVisible(true);
    }

    private void returnBook() {
        int studentId = Integer.parseInt(studentIdField.getText());
        int bookId = Integer.parseInt(bookIdField.getText());

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement fetchIssued = conn.prepareStatement(
                "SELECT issue_id, due_date FROM issued_books WHERE student_id = ? AND book_id = ? AND returned = FALSE"
            );
            fetchIssued.setInt(1, studentId);
            fetchIssued.setInt(2, bookId);
            ResultSet rs = fetchIssued.executeQuery();

            if (!rs.next()) {
                resultArea.setText("❌ No active issued book found for this student and book.");
                return;
            }

            int issueId = rs.getInt("issue_id");
            LocalDate dueDate = rs.getDate("due_date").toLocalDate();
            LocalDate returnDate = LocalDate.now();

            long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
            double fine = daysLate > 0 ? daysLate * 2.0 : 0.0;

            // Mark as returned
            PreparedStatement updateIssue = conn.prepareStatement(
                "UPDATE issued_books SET returned = TRUE WHERE issue_id = ?"
            );
            updateIssue.setInt(1, issueId);
            updateIssue.executeUpdate();

            // Record in returns
            PreparedStatement recordReturn = conn.prepareStatement(
                "INSERT INTO returns (issue_id, return_date, fine) VALUES (?, ?, ?)"
            );
            recordReturn.setInt(1, issueId);
            recordReturn.setDate(2, Date.valueOf(returnDate));
            recordReturn.setDouble(3, fine);
            recordReturn.executeUpdate();

            // Update copies_available
            PreparedStatement updateStock = conn.prepareStatement(
                "UPDATE books SET copies_available = copies_available + 1 WHERE book_id = ?"
            );
            updateStock.setInt(1, bookId);
            updateStock.executeUpdate();

            resultArea.setText("✅ Book returned successfully.\nFine: ₹" + fine);
        } catch (Exception ex) {
            ex.printStackTrace();
            resultArea.setText("❌ Error while returning the book.");
        }
    }
    public static void main(String args[]){
        new ReturnBook();
    }
}
