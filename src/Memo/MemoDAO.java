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

    // Save a new memo
    public boolean saveMemo(MemoDTO memo, String sessionId) {
        String userId = sessionManager.getUserId(sessionId); // Get userId from session
        if (userId == null) {
            System.out.println("Session is invalid or expired.");
            return false;
        }

        String query = "INSERT INTO memo (CONTENT, CHANNEL_ID, USER_ID, CREATED_AT, UPDATED_AT) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, memo.getContent()); // Memo content
            pstmt.setInt(2, memo.getRoomId()); // Channel ID
            pstmt.setInt(3, Integer.parseInt(userId)); // User ID as integer
            pstmt.setTimestamp(4, new Timestamp(memo.getCreatedAt().getTime())); // Created timestamp
            pstmt.setTimestamp(5, new Timestamp(memo.getUpdatedAt().getTime())); // Updated timestamp

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.out.println("Error saving memo: " + e.getMessage());
            e.printStackTrace();
            return false;
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
