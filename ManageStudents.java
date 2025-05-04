import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageStudents extends JFrame {
    JTextField studentIdField;
    JTextArea outputArea;

    public ManageStudents() {
        setTitle("Manage Students");
        setSize(600, 500);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JButton viewBtn = new JButton("👀 View All Students");
        JButton activateBtn = new JButton("✅ Activate Student");
        JButton deactivateBtn = new JButton("🚫 Deactivate Student");

        studentIdField = new JTextField(10);
        outputArea = new JTextArea(20, 50);

        add(viewBtn); 
        add(new JLabel("Student ID:")); 
        add(studentIdField);
        add(activateBtn); 
        add(deactivateBtn);
        add(new JScrollPane(outputArea));

        viewBtn.addActionListener(e -> viewStudents());
        activateBtn.addActionListener(e -> toggleStudent(true));
        deactivateBtn.addActionListener(e -> toggleStudent(false));

        setVisible(true);
    }

    private void viewStudents() {
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");

            outputArea.setText("👩‍🎓 Student Records:\n\n");
            while (rs.next()) {
                outputArea.append("ID: " + rs.getInt("student_id") +
                        ", Name: " + rs.getString("name") +
                        ", Email: " + rs.getString("email") +
                        ", Active: " + rs.getBoolean("active") + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            outputArea.setText("❌ Error fetching students.");
        }
    }

    private void toggleStudent(boolean activate) {
        try (Connection conn = DBConnection.getConnection()) {
            int id = Integer.parseInt(studentIdField.getText());

            PreparedStatement stmt = conn.prepareStatement("UPDATE students SET active = ? WHERE student_id = ?");
            stmt.setBoolean(1, activate);
            stmt.setInt(2, id);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                outputArea.setText("✅ Student ID " + id + (activate ? " activated." : " deactivated."));
            } else {
                outputArea.setText("❌ Student not found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            outputArea.setText("❌ Error updating student.");
        }
    }

    public static void main(String[] args) {
        new ManageStudents();
    }
}
