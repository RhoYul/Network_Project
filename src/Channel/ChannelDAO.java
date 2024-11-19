package Channel;

import Database.DBUtil;
import Session.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
