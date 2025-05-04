import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Notifications extends JFrame {
    private int sapId;
    private DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Message", "Action"}, 0);
    private JTable table = new JTable(model);
    private JLabel countLabel = new JLabel("Loading notifications...");

    public Notifications(int studentId, int sapId) {
        this.sapId = sapId;
        setTitle("Notifications");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JLabel titleLabel = new JLabel("Your Notifications");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        countLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        header.add(titleLabel, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);

        // Table setup
        table.setRowHeight(40);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 16));
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));
        table.getColumnModel().getColumn(1).setCellRenderer(new MultiLineRenderer());

        // Layout
        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh Notifications");
        refresh.addActionListener(e -> loadNotifications());
        JPanel bottom = new JPanel();
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);

        loadNotifications();
        setVisible(true);
    }

    private void loadNotifications() {
        model.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            PreparedStatement stmt = conn.prepareStatement("SELECT notification_id, message FROM notifications WHERE sap_id = ? ORDER BY notification_id DESC");
            stmt.setInt(1, sapId);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("notification_id"), rs.getString("message"), "Mark Read"});
                count++;
            }

            if (count == 0) model.addRow(new Object[]{"-", "No notifications to display.", ""});
            countLabel.setText("You have " + count + " notification(s)");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading notifications:\n" + e.getMessage());
        }
    }

    private void deleteNotification(int id) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM notifications WHERE notification_id = ?");
            stmt.setInt(1, id);
            if (stmt.executeUpdate() > 0) {
                JOptionPane.showMessageDialog(this, "Notification marked as read");
                loadNotifications();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting notification:\n" + e.getMessage());
        }
    }

    // Multi-line message renderer
    static class MultiLineRenderer extends JTextArea implements TableCellRenderer {
        public MultiLineRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setFont(new Font("Arial", Font.PLAIN, 14));
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            setText(value == null ? "" : value.toString());
            setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            table.setRowHeight(row, getPreferredSize().height);
            return this;
        }
    }

    // Button Renderer
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    // Button Editor
    class ButtonEditor extends DefaultCellEditor {
        private JButton button = new JButton();
        private int currentRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            currentRow = row;
            button.setText(value == null ? "" : value.toString());
            return button;
        }

        public Object getCellEditorValue() {
            String label = button.getText();
            if ("Mark Read".equals(label)) {
                Object idObj = table.getValueAt(currentRow, 0);
                if (idObj != null && !"-".equals(idObj.toString())) {
                    try {
                        deleteNotification(Integer.parseInt(idObj.toString()));
                    } catch (NumberFormatException ignored) {}
                }
            }
            return label;
        }
    }

    public static void createSampleNotifications(int sapId) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "odd*357.")) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO notifications (sap_id, message) VALUES (?, ?)");
            String[] messages = {
                "Welcome to the library system!",
                "Your book 'Java Programming' is due tomorrow.",
                "The library will be closed for maintenance on Sunday."
            };
            for (String msg : messages) {
                stmt.setInt(1, sapId);
                stmt.setString(2, msg);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createSampleNotifications(1001);
            new Notifications(1, 1001);
        });
    }
}
