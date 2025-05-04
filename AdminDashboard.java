import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {

        setTitle("Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("WELCOME ADMIN", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 25, 25));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        gridPanel.setBackground(Color.WHITE);

        // Add feature buttons with new window launch
        gridPanel.add(createCardButton("Add/View/Delete Librarians", "👤", LibrarianManagement.class));
        gridPanel.add(createCardButton("Available book list", "📚", Bookl.class));
        gridPanel.add(createCardButton("Borrowed books list", "📖", BorrowedBooksl.class));
        gridPanel.add(createCardButton("Fine Report", "💰", FineAndReturnApp.class));
        gridPanel.add(createCardButton("User Account Management", "👤", StudentManager.class));

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
        SwingUtilities.invokeLater(() -> new AdminDashboard());
    }
}
