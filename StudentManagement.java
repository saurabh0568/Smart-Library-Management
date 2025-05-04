import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentManagement extends JFrame {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JPanel formPanel;
    private JTextField sapIdField, nameField, emailField, courseField;
    private JSpinner yearSpinner;
    private JButton saveButton;
    private JTextField searchField;
    private JButton searchButton;


    public StudentManagement() {
        setTitle("🎓 Student Management");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel with Add Button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("➕ Add Student");
        topPanel.add(addButton);
        add(topPanel, BorderLayout.NORTH);

        searchField = new JTextField(20);
        searchButton = new JButton("🔍 Search");

        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);


        // Table setup
        tableModel = new DefaultTableModel(new String[]{"Sap_ID", "Name", "Email", "Course", "Year", "Edit", "Delete"}, 0);
        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(30);
        loadStudents();

        // Add button renderers for edit/delete
        new ButtonColumn(studentTable, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                editStudent(row);
            }
        }, 5);
        new ButtonColumn(studentTable, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                deleteStudent(row);
            }
        }, 6);

        add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // Form Panel (Initially hidden)
        formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Student"));

        sapIdField = new JTextField();
        nameField = new JTextField();
        emailField = new JTextField();
        courseField = new JTextField();
        yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        saveButton = new JButton("Save");

        searchButton.addActionListener(e -> searchStudents());


        formPanel.add(new JLabel("SAP ID:"));
        formPanel.add(sapIdField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Course:"));
        formPanel.add(courseField);
        formPanel.add(new JLabel("Year:"));
        formPanel.add(yearSpinner);
        formPanel.add(new JLabel(""));
        formPanel.add(saveButton);

        formPanel.setVisible(false);
        add(formPanel, BorderLayout.SOUTH);

        // Toggle form
        addButton.addActionListener(e -> formPanel.setVisible(!formPanel.isVisible()));

        // Save action
        saveButton.addActionListener(e -> addStudent());

        setVisible(true);
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM students");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("sap_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("course"),
                    rs.getInt("year"),
                    "Edit", "Delete"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addStudent() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO students (sap_id, name, email, course, year) VALUES (?, ?, ?, ?, ?)"
            );
            stmt.setInt(1, Integer.parseInt(sapIdField.getText().trim()));
            stmt.setString(2, nameField.getText().trim());
            stmt.setString(3, emailField.getText().trim());
            stmt.setString(4, courseField.getText().trim());
            stmt.setInt(5, (int) yearSpinner.getValue());
            stmt.executeUpdate();

            clearForm();
            loadStudents();
            JOptionPane.showMessageDialog(this, "✅ Student added!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Failed to add student.");
        }
    }

    private void deleteStudent(int row) {
        int sapId = (int) tableModel.getValueAt(row, 0);
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM students WHERE sap_id = ?");
            stmt.setInt(1, sapId);
            stmt.executeUpdate();
            loadStudents();
            JOptionPane.showMessageDialog(this, "🗑️ Student deleted!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void editStudent(int row) {
        int sapId = (int) tableModel.getValueAt(row, 0);
        String newEmail = JOptionPane.showInputDialog(this, "Enter new email:", tableModel.getValueAt(row, 2));
        if (newEmail == null || newEmail.trim().isEmpty()) return;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE students SET email = ? WHERE sap_id = ?");
            stmt.setString(1, newEmail);
            stmt.setInt(2, sapId);
            stmt.executeUpdate();
            loadStudents();
            JOptionPane.showMessageDialog(this, "✅ Student email updated!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        sapIdField.setText("");
        nameField.setText("");
        emailField.setText("");
        courseField.setText("");
        yearSpinner.setValue(1);
        formPanel.setVisible(false);
    }

    private void searchStudents() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadStudents();  // Show all if search is empty
            return;
        }
    
        tableModel.setRowCount(0); // Clear table
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM students WHERE LOWER(name) LIKE ? OR CAST(sap_id AS CHAR) LIKE ?"
            );
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
    
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("sap_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("course"),
                    rs.getInt("year"),
                    "Edit", "Delete"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Failed to search.");
        }
    }
    

    public static void main(String[] args) {
        new StudentManagement();
    }
}
