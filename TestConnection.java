import java.sql.Connection;

public class TestConnection {
    public static void main(String[] args) {
        // Test the database connection
        Connection conn = DBConnection.getConnection();
        if (conn != null) {
            System.out.println("Connection successful!");
        } else {
            System.out.println("Failed to connect.");
        }

        // Optionally, close the connection after testing
        DBConnection.closeConnection();
    }
}
