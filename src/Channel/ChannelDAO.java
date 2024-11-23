//Channel 테이블에서 작동하는 query문 모음 이라고 보시면 됩니다. createChannel -> 채널을 개설하는 쿼리문(채널 개설 시 채널 정보를 데이터베이스에 저장하는 쿼리문)

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
        // 세션 ID를 통해 USER_ID 가져오기
    	System.out.println("createChannel method called."); // 디버깅용 출력
    	System.out.println("Session ID: " + sessionId); // 디버깅용 출력
        String ownerId = sessionManager.getUserId(sessionId);
        if (ownerId == null) {
            System.out.println("Invalid session. Cannot create channel.");
            return;
        }
        System.out.println("Owner ID: " + ownerId); // 디버깅용 출력

        String query = "INSERT INTO channels (CHANNEL_NAME, OWNER_ID) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, channelName); // 채널 이름
            pstmt.setString(2, ownerId);     // 세션에서 가져온 USER_ID

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

 // Connect with user
    public boolean addUserToChannel(String channelName, String userId) {
        String checkChannelQuery = "SELECT COUNT(*) FROM channels WHERE CHANNEL_NAME = ?";
        String checkUserQuery = "SELECT COUNT(*) FROM users WHERE USER_ID = ?";
        String insertQuery = "INSERT INTO channel_users (CHANNEL_NAME, USER_ID) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection()) {
            // 1. Check if the channel exists
            try (PreparedStatement checkChannelStmt = conn.prepareStatement(checkChannelQuery)) {
                checkChannelStmt.setString(1, channelName);
                try (ResultSet rs = checkChannelStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("Channel does not exist: " + channelName);
                        return false;
                    }
                }
            }

            // 2. Check if the user exists
            try (PreparedStatement checkUserStmt = conn.prepareStatement(checkUserQuery)) {
                checkUserStmt.setString(1, userId);
                try (ResultSet rs = checkUserStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        System.out.println("User does not exist: " + userId);
                        return false;
                    }
                }
            }

            // 3. Insert into channel_users
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setString(1, channelName);
                pstmt.setString(2, userId);
                pstmt.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error adding user to channel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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

}
