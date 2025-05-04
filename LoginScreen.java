import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginScreen extends JFrame {
    JTextField userField;
    JPasswordField passField;
    JComboBox<String> roleBox;
    JLabel resultLabel;
    Image backgroundImage;

    public LoginScreen() {
        setTitle("Smart Library - Login");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load background image
        backgroundImage = Toolkit.getDefaultToolkit().createImage("lib.png");

        JPanel contentPanel = new BackgroundPanel();
        contentPanel.setLayout(new GridBagLayout());
        add(contentPanel);

        // Login form panel
        JPanel loginPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        loginPanel.setOpaque(false);
        loginPanel.setPreferredSize(new Dimension(400, 300));

        roleBox = new JComboBox<>(new String[]{"Admin", "Librarian", "Student"});
        userField = new JTextField();
        passField = new JPasswordField();
        resultLabel = new JLabel(" ", SwingConstants.CENTER);
        resultLabel.setForeground(Color.WHITE);

        loginPanel.add(new JLabel("Role:"));
        loginPanel.add(roleBox);
        loginPanel.add(new JLabel("Sap id:"));
        loginPanel.add(userField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);
        JButton loginBtn = new JButton("Login");
        JButton forgotBtn = new JButton("Forgot Password");

        buttonPanel.add(loginBtn);
        buttonPanel.add(forgotBtn);

        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BorderLayout(10, 10));
        centerPanel.add(loginPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        centerPanel.add(resultLabel, BorderLayout.NORTH);

        JLabel headingLabel = new JLabel("Welcome to D.P.K.S Library", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Serif", Font.BOLD, 52));
        headingLabel.setForeground(Color.BLACK);
        headingLabel.setOpaque(false);

        Box mainBox = Box.createVerticalBox();
        mainBox.setOpaque(false);
        mainBox.add(Box.createVerticalStrut(40));
        mainBox.add(headingLabel);
        mainBox.add(Box.createVerticalStrut(30));
        mainBox.add(centerPanel);

        contentPanel.add(mainBox);

        // Action listeners
        loginBtn.addActionListener(e -> login());
        forgotBtn.addActionListener(e -> openForgotDialog());

        setVisible(true);
    }

    class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    private void login() {
        String role = (String) roleBox.getSelectedItem();
        String username = userField.getText().trim();
        String password = new String(passField.getPassword()).trim();

        String table = "", idField = "", nameField = "", emailField = "";

        if (role.equals("Admin")) {
            table = "admins";
            idField = "admin_id";
            nameField = "username";
            emailField = "username";
        } else if (role.equals("Librarian")) {
            table = "librarians";
            idField = "librarian_id";
            nameField = "name";
            emailField = "librarian_id";
        } else {
            table = "students";
            idField = "sap_id";
            nameField = "name";
            emailField = "sap_id";
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT " + idField + ", " + nameField + " FROM " + table + " WHERE " + emailField + " = ? AND password = ?"
            );
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt(idField);
                String name = rs.getString(nameField);
                resultLabel.setText("✅ Welcome, " + name + " (" + role + ")");
                this.dispose();

                switch (role) {
                    case "Admin":
                        new AdminDashboard();
                        break;
                    case "Librarian":
                        new LibrarianDashboardWithTabs(id, name);
                        break;
                    case "Student":
                        new Test(id, name);
                        break;
                }
            } else {
                resultLabel.setText("❌ Invalid credentials.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            resultLabel.setText("❌ Login error.");
        }
    }

    private void openForgotDialog() {
        JDialog dialog = new JDialog(this, "Forgot Password", true);
        dialog.setSize(350, 200);
        dialog.setLayout(new GridLayout(4, 2, 5, 5));

        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "Librarian", "Student"});
        JTextField emailField = new JTextField();
        JTextField newPassField = new JTextField();
        JLabel message = new JLabel(" ", SwingConstants.CENTER);

        dialog.add(new JLabel("Role:"));
        dialog.add(roleBox);
        dialog.add(new JLabel("Email/Username:"));
        dialog.add(emailField);
        dialog.add(new JLabel("New Password:"));
        dialog.add(newPassField);

        JButton resetBtn = new JButton("Reset Password");
        dialog.add(resetBtn);
        dialog.add(message);

        resetBtn.addActionListener(e -> {
            String role = (String) roleBox.getSelectedItem();
            String email = emailField.getText().trim();
            String newPass = newPassField.getText().trim();

            String table = "", emailCol = "";

            if (role.equals("Admin")) {
                table = "admins";
                emailCol = "username";
            } else if (role.equals("Librarian")) {
                table = "librarians";
                emailCol = "email";
            } else {
                table = "students";
                emailCol = "sap_id";
            }

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE " + table + " SET password = ? WHERE " + emailCol + " = ?"
                );
                stmt.setString(1, newPass);
                stmt.setString(2, email);
                int updated = stmt.executeUpdate();
                message.setText(updated > 0 ? "✅ Password updated." : "❌ User not found.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                message.setText("❌ Error updating password.");
            }
        });

        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
