import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BookRequestApprovalSystem extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    public BookRequestApprovalSystem() {
        setTitle("Book Request Approval System");
        setSize(1100, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        model = new DefaultTableModel(new String[]{
            "Request ID", "Title", "Author", "Genre", "Publisher", "Requester Name", "Request Date", "SAP ID", "Approve", "Disapprove"
        }, 0) {
            public boolean isCellEditable(int row, int column) {
                return column == 8 || column == 9; // Only buttons are editable
            }
        };

        table = new JTable(model);
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        loadRequests();
        addButtonsToTable();
    }

    private void loadRequests() {
        model.setRowCount(0);
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM book_requests")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("request_id"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("genre"),
                    rs.getString("publisher"),
                    rs.getString("requester_name"),
                    rs.getTimestamp("request_date"),
                    rs.getString("sap_id"),
                    "Approve",
                    "Disapprove"
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addButtonsToTable() {
        table.getColumn("Approve").setCellRenderer(new ButtonRenderer());
        table.getColumn("Approve").setCellEditor(new ButtonEditor(new JCheckBox(), true));
        table.getColumn("Disapprove").setCellRenderer(new ButtonRenderer());
        table.getColumn("Disapprove").setCellEditor(new ButtonEditor(new JCheckBox(), false));
    }

    private Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/library";
        String user = "root";
        String pass = "odd*357.";
        return DriverManager.getConnection(url, user, pass);
    }

    // Renderer
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    // Editor
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isApprove;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox, boolean isApprove) {
            super(checkBox);
            this.isApprove = isApprove;
            button = new JButton();
            button.setOpaque(true);

            button.addActionListener(e -> {
                fireEditingStopped();
                processRequest(selectedRow, isApprove);
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            selectedRow = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            return button;
        }
    }

    private void processRequest(int row, boolean approved) {
        int requestId = Integer.parseInt(model.getValueAt(row, 0).toString());
        String title = model.getValueAt(row, 1).toString();
        String sapId = model.getValueAt(row, 7).toString();

        String message = approved
            ? "✅ Your request for the book \"" + title + "\" has been approved."
            : "❌ Your request for the book \"" + title + "\" has been disapproved.";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement notifyStmt = conn.prepareStatement(
                    "INSERT INTO notifications (sap_id, message) VALUES (?, ?)");
                PreparedStatement deleteStmt = conn.prepareStatement(
                    "DELETE FROM book_requests WHERE request_id = ?")
            ) {
                notifyStmt.setString(1, sapId);
                notifyStmt.setString(2, message);
                notifyStmt.executeUpdate();

                deleteStmt.setInt(1, requestId);
                deleteStmt.executeUpdate();

                conn.commit();
                model.removeRow(row);
                JOptionPane.showMessageDialog(this, "Request " + (approved ? "approved" : "disapproved") + " and notification sent.");
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to process the request.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC driver not found.");
            return;
        }

        SwingUtilities.invokeLater(() -> new BookRequestApprovalSystem().setVisible(true));
    }
}
