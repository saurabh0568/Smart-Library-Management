import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LibrarianManagement extends JFrame {
    private JTable librarianTable;
    private DefaultTableModel tableModel;
    private JPanel formPanel;
    private JTextField librarianIdField, nameField, emailField, phoneField;
    private JSpinner hireDateSpinner;
    private JButton saveButton;
    private JTextField searchField;
    private JButton searchButton;

    public LibrarianManagement() {
        setTitle("📚 Librarian Management");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel with Add Button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("➕ Add Librarian");
        topPanel.add(addButton);
        add(topPanel, BorderLayout.NORTH);

        searchField = new JTextField(20);
        searchButton = new JButton("🔍 Search");

        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"Librarian ID", "Name", "Email", "Phone", "Hire Date", "Edit", "Delete"}, 0);
        librarianTable = new JTable(tableModel);
        librarianTable.setRowHeight(30);
        loadLibrarians();

        // Add button renderers for edit/delete
        new ButtonColumn(librarianTable, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                editLibrarian(row);
            }
        }, 5);
        new ButtonColumn(librarianTable, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int row = Integer.parseInt(e.getActionCommand());
                deleteLibrarian(row);
            }
        }, 6);

        add(new JScrollPane(librarianTable), BorderLayout.CENTER);

        // Form Panel (Initially hidden)
        formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add New Librarian"));

        librarianIdField = new JTextField();
        nameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        hireDateSpinner = new JSpinner(new SpinnerDateModel());
        saveButton = new JButton("Save");

        searchButton.addActionListener(e -> searchLibrarians());

        formPanel.add(new JLabel("Librarian ID:"));
        formPanel.add(librarianIdField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Hire Date:"));
        formPanel.add(hireDateSpinner);
        formPanel.add(new JLabel(""));
        formPanel.add(saveButton);

        formPanel.setVisible(false);
        add(formPanel, BorderLayout.SOUTH);

        // Toggle form
        addButton.addActionListener(e -> formPanel.setVisible(!formPanel.isVisible()));

        // Save action
        saveButton.addActionListener(e -> addLibrarian());

        setVisible(true);
    }

    private void loadLibrarians() {
        tableModel.setRowCount(0);
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM librarians");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("librarian_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getDate("hire_date"),
                    "Edit", "Delete"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLibrarian() {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO librarians (librarian_id, name, email, phone_number, hire_date) VALUES (?, ?, ?, ?, ?)"
            );
            stmt.setInt(1, Integer.parseInt(librarianIdField.getText().trim()));
            stmt.setString(2, nameField.getText().trim());
            stmt.setString(3, emailField.getText().trim());
            stmt.setString(4, phoneField.getText().trim());
            stmt.setDate(5, new java.sql.Date(((java.util.Date) hireDateSpinner.getValue()).getTime()));
            stmt.executeUpdate();

            clearForm();
            loadLibrarians();
            JOptionPane.showMessageDialog(this, "✅ Librarian added!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Failed to add librarian.");
        }
    }

    private void deleteLibrarian(int row) {
        int librarianId = (int) tableModel.getValueAt(row, 0);
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM librarians WHERE librarian_id = ?");
            stmt.setInt(1, librarianId);
            stmt.executeUpdate();
            loadLibrarians();
            JOptionPane.showMessageDialog(this, "🗑️ Librarian deleted!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void editLibrarian(int row) {
        int librarianId = (int) tableModel.getValueAt(row, 0);
        String newEmail = JOptionPane.showInputDialog(this, "Enter new email:", tableModel.getValueAt(row, 2));
        if (newEmail == null || newEmail.trim().isEmpty()) return;

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE librarians SET email = ? WHERE librarian_id = ?");
            stmt.setString(1, newEmail);
            stmt.setInt(2, librarianId);
            stmt.executeUpdate();
            loadLibrarians();
            JOptionPane.showMessageDialog(this, "✅ Librarian email updated!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearForm() {
        librarianIdField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        hireDateSpinner.setValue(new java.util.Date());
        formPanel.setVisible(false);
    }

    private void searchLibrarians() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadLibrarians();  // Show all if search is empty
            return;
        }
    
        tableModel.setRowCount(0); // Clear table
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM librarians WHERE LOWER(name) LIKE ? OR CAST(librarian_id AS CHAR) LIKE ?"
            );
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
    
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("librarian_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone_number"),
                    rs.getDate("hire_date"),
                    "Edit", "Delete"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Failed to search.");
        }
    }

    public static void main(String[] args) {
        new LibrarianManagement();
    }
}
