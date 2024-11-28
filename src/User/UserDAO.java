// Collection of query methods for user operations like login, registration, etc.

package User;

import Database.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    // Query to insert user data into the database during registration
    public void insertUser(String userId, String passwd, String userName, String email) {
        String query = "INSERT INTO Users (USER_ID, PASSWD, USER_NAME, EMAIL) VALUES (?, ?, ?, ?)"; // Query

        try (Connection conn = DBUtil.getConnection(); // Connect to the database
             PreparedStatement pstmt = conn.prepareStatement(query)) { // Prepare the query with parameters

            pstmt.setString(1, userId); // Set parameters
            pstmt.setString(2, passwd);
            pstmt.setString(3, userName);
            pstmt.setString(4, email);

            int rowsInserted = pstmt.executeUpdate(); // Execute the query and apply changes
            if (rowsInserted > 0) { // If at least one row is updated, insertion was successful
                System.out.println("User inserted successfully!");
            }

        } catch (SQLException ex) {
            System.out.println("Error inserting user: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Query to authenticate a user in the database
    public boolean authenticateUser(String userId, String passwd) {
        String query = "SELECT * FROM Users WHERE USER_ID = ? AND PASSWD = ?"; // Query

        try (Connection conn = DBUtil.getConnection(); // Connect to the database
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userId); // Set parameters
            pstmt.setString(2, passwd);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Return true if a result is found
            }

        } catch (SQLException ex) {
            System.out.println("Authentication failed: " + ex.getMessage());
            ex.printStackTrace();
        }

        return false;
    }
}
