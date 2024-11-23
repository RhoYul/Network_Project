// This is a collection of query statements that operate on the Channel table.
// createChannel -> A query to create a channel (a query that stores channel information in the database when a channel is created).

package Channel;

import Database.DBUtil;
import Session.SessionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ChannelDAO {
    private SessionManager sessionManager;

    public ChannelDAO(SessionManager sessionManager) {
    	if (sessionManager == null) {
            throw new IllegalArgumentException("SessionManager cannot be null");
        }
        this.sessionManager = sessionManager;
    }

    public void createChannel(String channelName, String sessionId) {
        // Retrieve USER_ID using the session ID
    	System.out.println("createChannel method called."); // Debugging output
    	System.out.println("Session ID: " + sessionId);// Debugging output
        String ownerId = sessionManager.getUserId(sessionId);
        if (ownerId == null) {
            System.out.println("Invalid session. Cannot create channel.");
            return;
        }
        System.out.println("Owner ID: " + ownerId); // Debugging output

        String query = "INSERT INTO channels (CHANNEL_NAME, OWNER_ID) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, channelName); // Channel name
            pstmt.setString(2, ownerId);     // USER_ID in session

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Channel created successfully!");
            }
        } catch (SQLException ex) {
            System.out.println("Error creating channel: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println("CHANNEL_NAME: " + channelName);
        System.out.println("OWNER_ID: " + ownerId);

    }
    
    // Get channel lists
    public List<String> getActiveChannelNames() {
        List<String> channelNames = new ArrayList<>();
        String query = "SELECT CHANNEL_NAME FROM channels";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                channelNames.add(rs.getString("CHANNEL_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return channelNames;
    }

    // delete channel
    public boolean deleteChannel(String channelName, String ownerId) {
        String query = "DELETE FROM channels WHERE CHANNEL_NAME = ? AND OWNER_ID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, channelName);
            pstmt.setString(2, ownerId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getChannelIdFromName(String channelName) throws SQLException {
        String query = "SELECT ID FROM channels WHERE CHANNEL_NAME = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, channelName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("ID"); // 채널 ID 반환
                } else {
                    throw new SQLException("Channel not found");
                }
            }
        }
    }

    
    public boolean isOwner(String channelName, String userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM channels WHERE CHANNEL_NAME = ? AND OWNER_ID = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, channelName);
            pstmt.setString(2, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // 해당 채널이 있고 소유자가 맞는 경우 true 반환
                }
            }
        }
        return false;
    }

}