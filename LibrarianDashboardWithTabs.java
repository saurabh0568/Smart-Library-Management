import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LibrarianDashboardWithTabs extends JFrame {
    int librarian_id;
    String LibrarianName;

    public LibrarianDashboardWithTabs(int librarian_id, String name) {
        this.librarian_id = librarian_id;

        // Overwrite the name if fetched successfully from DB
        String fetchedName = fetchLibrarianNameFromDatabase(librarian_id);
        this.LibrarianName = (fetchedName != null) ? fetchedName : name;

        setTitle("Librarian Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("WELCOME " + LibrarianName, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 25, 25));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        gridPanel.setBackground(Color.WHITE);

        // Add feature buttons with new window launch
        gridPanel.add(createCardButton("Return Books & Calculate Fine", "↩", ReturnBooksUI.class));
        gridPanel.add(createCardButton("Add/View/Delete Books", "📚", BookManagement.class));
        gridPanel.add(createCardButton("Manage Student Records", "👥", StudentManagement.class));
        gridPanel.add(createCardButton("Issue Books to Students", "📖", BookIssueSystem.class));
        gridPanel.add(createCardButton("Request Approval", "✅", BookRequestApprovalSystem.class));
        gridPanel.add(createCardButton("View Issued & Overdue Books", "⏰", IssuedBooksUI.class));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("Arial", Font.BOLD, 16));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBackground(Color.RED);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(100, 40));
        logoutBtn.addActionListener(e -> System.exit(0));

        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(Color.WHITE);
        logoutPanel.add(logoutBtn);

        setLayout(new BorderLayout());
        add(titleLabel, BorderLayout.NORTH);
        add(gridPanel, BorderLayout.CENTER);
        add(logoutPanel, BorderLayout.SOUTH);

        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }

    private String fetchLibrarianNameFromDatabase(int librarianId) {
        String name = null;
        try {
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/library", "root", "odd*357.");

            String query = "SELECT name FROM librarians WHERE librarian_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, librarianId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                name = rs.getString("name");
            } else {
                JOptionPane.showMessageDialog(this, "Librarian with ID " + librarianId + " not found.");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage());
        }
        return name;
    }

    private JButton createCardButton(String text, String iconText, Class<? extends JFrame> windowClass) {
        String html = String.format("<html><center>" +
                "<div style='font-size:36px;'>%s</div>" +
                "<div style='font-size:16px; font-weight:bold;'>%s</div>" +
                "</center></html>", iconText, text);

        JButton button = new JButton(html);
        button.setPreferredSize(new Dimension(220, 120));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBackground(new Color(240, 248, 255));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        button.addActionListener(e -> openNewWindow(windowClass));
        return button;
    }

    private void openNewWindow(Class<? extends JFrame> windowClass) {
        try {
            JFrame window = windowClass.getDeclaredConstructor().newInstance();
            window.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to open window: " + windowClass.getSimpleName());
        }
    }

    public static void main(String[] args) {
        // Replace 1 and "Default Name" with actual data as needed
        SwingUtilities.invokeLater(() -> new LibrarianDashboardWithTabs(1, "Default Name"));
    }
}
