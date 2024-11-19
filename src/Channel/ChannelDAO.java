package Channel;

import Database.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChannelDAO {
    public void createChannel(String channelName, String ownerId) {
        String query = "INSERT INTO Channels (channel_name, owner_id) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, channelName);
            pstmt.setString(2, ownerId);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Channel created successfully!");
            }
        } catch (SQLException ex) {
            System.out.println("Error creating channel: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
