package Memo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Database.DBUtil;
import Session.SessionManager;

public class MemoDAO {

    private Connection conn;
    private SessionManager sessionManager; // The sessionManager should be initialized here

    // Constructor: Initialize the DB connection and session manager
    public MemoDAO(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        try {
            this.conn = DBUtil.getConnection(); // Get a new DB connection for each instance
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // add new memo
    public void addMemo(int channelId, String userId, String content) throws SQLException {
        String sql = "INSERT INTO memo (CONTENT, CHANNEL_ID, USER_ID, CREATED_AT, UPDATED_AT) VALUES (?, ?, ?, NOW(), NOW())";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, content);      // CONTENT
            stmt.setInt(2, channelId);       // CHANNEL_ID
            stmt.setString(3, userId);       // USER_ID
            stmt.executeUpdate();            // 쿼리 실행
        }
    }

    // Get all memos for a specific room (sorted by creation time)
    public List<MemoDTO> getMemosByRoom(int channelId) {
        List<MemoDTO> memoList = new ArrayList<>();
        String query = "SELECT * FROM memo WHERE CHANNEL_ID = ? ORDER BY CREATED_AT ASC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, channelId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MemoDTO memo = new MemoDTO();
                memo.setMemoId(rs.getInt("ID")); // Memo ID
                memo.setRoomId(rs.getInt("CHANNEL_ID")); // Channel ID
                memo.setContent(rs.getString("CONTENT")); // Memo content
                memo.setUsername(rs.getString("USER_ID")); // User ID
                memo.setCreatedAt(rs.getTimestamp("CREATED_AT")); // Created timestamp
                memo.setUpdatedAt(rs.getTimestamp("UPDATED_AT")); // Updated timestamp

                memoList.add(memo);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving memos: " + e.getMessage());
            e.printStackTrace();
        }
        return memoList;
    }

    // Delete a memo by its ID
    public boolean deleteMemo(int memoId) {
        String query = "DELETE FROM memo WHERE ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, memoId); // Memo ID
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error deleting memo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update a memo's content
    public boolean updateMemo(int memoId, String content) {
        String query = "UPDATE memo SET CONTENT = ?, UPDATED_AT = ? WHERE ID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, content); // New memo content
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis())); // Updated timestamp
            pstmt.setInt(3, memoId); // Memo ID

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Error updating memo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
