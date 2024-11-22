// The part where the connection to the database (MySQL) and Java is established using JDBC

package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    // JDBC URL, USER, PASSWORD
    private static final String URL = "jdbc:mysql://localhost:3306/network_project?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    // Database connection
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
