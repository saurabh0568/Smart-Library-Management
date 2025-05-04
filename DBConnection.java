import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    static Connection conn;

    // Get the connection to the database
    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                // Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Establish connection to the database
                conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/library", "root", "odd*357."
                );
            }
        } catch (Exception e) {
            System.out.println("Error while connecting to the database.");
            e.printStackTrace();
        }
        return conn;
    }

    // Close the database connection
    public static void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Error while closing the connection.");
            e.printStackTrace();
        }
    }
}
