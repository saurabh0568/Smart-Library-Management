import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentManager extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private Connection conn;

    public StudentManager() {
        setTitle("Student Account Manager");
        setSize(1000, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[]{"ID", "Name", "SAP ID", "Email", "Course", "Year", "Active", "Action"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 7;
            }

            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) return Boolean.class;
                return String.class;
            }
        };

        table = new JTable(model) {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                Boolean isActive = (Boolean) getValueAt(row, 6);
                if (isActive != null && !isActive) {
                    c.setBackground(Color.LIGHT_GRAY);
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };

        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        connectDB();
        loadData();

        setVisible(true);
    }

    private void connectDB() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DB Connection Error: " + e.getMessage());
        }
    }

    private void loadData() {
        model.setRowCount(0);
        try {
            // Load active students (excluding password)
            PreparedStatement stmt = conn.prepareStatement("SELECT student_id, name, sap_id, email, course, year FROM students");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("student_id"),
                    rs.getString("name"),
                    rs.getString("sap_id"),
                    rs.getString("email"),
                    rs.getString("course"),
                    rs.getInt("year"),
                    true,
                    "Deactivate"
                });
            }

            // Load inactive students (excluding password)
            stmt = conn.prepareStatement("SELECT student_id, name, sap_id, email, course, year FROM inactive_students");
            rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("student_id"),
                    rs.getString("name"),
                    rs.getString("sap_id"),
                    rs.getString("email"),
                    rs.getString("course"),
                    rs.getInt("year"),
                    false,
                    "Activate"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void toggleStatus(int row) {
        try {
            int id = (int) model.getValueAt(row, 0);
            String name = (String) model.getValueAt(row, 1);
            String sap = (String) model.getValueAt(row, 2);
            String email = (String) model.getValueAt(row, 3);
            String course = (String) model.getValueAt(row, 4);
            int year = (int) model.getValueAt(row, 5);
            boolean isActive = (Boolean) model.getValueAt(row, 6);

            if (isActive) {
                // Move to inactive_students
                PreparedStatement insert = conn.prepareStatement("INSERT INTO inactive_students (student_id, name, sap_id, email, course, year) VALUES (?, ?, ?, ?, ?, ?)");
                insert.setInt(1, id);
                insert.setString(2, name);
                insert.setString(3, sap);
                insert.setString(4, email);
                insert.setString(5, course);
                insert.setInt(6, year);
                insert.executeUpdate();

                PreparedStatement delete = conn.prepareStatement("DELETE FROM students WHERE student_id = ?");
                delete.setInt(1, id);
                delete.executeUpdate();
            } else {
                // Move to students
                PreparedStatement insert = conn.prepareStatement("INSERT INTO students (student_id, name, sap_id, email, course, year) VALUES (?, ?, ?, ?, ?, ?)");
                insert.setInt(1, id);
                insert.setString(2, name);
                insert.setString(3, sap);
                insert.setString(4, email);
                insert.setString(5, course);
                insert.setInt(6, year);
                insert.executeUpdate();

                PreparedStatement delete = conn.prepareStatement("DELETE FROM inactive_students WHERE student_id = ?");
                delete.setInt(1, id);
                delete.executeUpdate();
            }

            loadData();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error toggling status: " + e.getMessage());
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                toggleStatus(row);
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            return button;
        }

        public Object getCellEditorValue() {
            return label;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentManager::new);
    }
}
