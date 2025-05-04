import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageLibrarians extends JFrame {
    JTextField nameField, emailField, passField, deleteIdField;
    JTextArea outputArea;

    public ManageLibrarians() {
        setTitle("Manage Librarians");
        setSize(600, 500);
        setLayout(new FlowLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(new JLabel("Name:"));
        nameField = new JTextField(20); add(nameField);

        add(new JLabel("Email:"));
        emailField = new JTextField(20); add(emailField);

        add(new JLabel("Password:"));
        passField = new JTextField(20); add(passField);

        JButton addBtn = new JButton("Add Librarian");
        JButton viewBtn = new JButton("View Librarians");
        JButton deleteBtn = new JButton("Delete Librarian");

        add(addBtn); add(viewBtn); add(deleteBtn);

        add(new JLabel("Librarian ID to delete:"));
        deleteIdField = new JTextField(10); add(deleteIdField);

        outputArea = new JTextArea(15, 50);
        add(new JScrollPane(outputArea));

        addBtn.addActionListener(e -> addLibrarian());
        viewBtn.addActionListener(e -> viewLibrarians());
        deleteBtn.addActionListener(e -> deleteLibrarian());

        setVisible(true);
    }

    private void addLibrarian() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passField.getText().trim();

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO librarians (name, email, password) VALUES (?, ?, ?)"
            );
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                outputArea.setText("✅ Librarian added successfully!");
            } else {
                outputArea.setText("❌ Failed to add librarian.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            outputArea.setText("❌ Error adding librarian.");
        }
    }

    private void viewLibrarians() {
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM librarians");

            outputArea.setText("📋 Librarian List:\n");
            while (rs.next()) {
                outputArea.append("ID: " + rs.getInt("librarian_id") +
                        ", Name: " + rs.getString("name") +
                        ", Email: " + rs.getString("email") + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            outputArea.setText("❌ Error fetching librarians.");
        }
    }

    private void deleteLibrarian() {
        int id = Integer.parseInt(deleteIdField.getText());

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM librarians WHERE librarian_id = ?");
            stmt.setInt(1, id);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                outputArea.setText("✅ Librarian deleted successfully.");
            } else {
                outputArea.setText("❌ Librarian not found.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            outputArea.setText("❌ Error deleting librarian.");
        }
    }

    public static void main(String[] args) {
        new ManageLibrarians();
    }
}
