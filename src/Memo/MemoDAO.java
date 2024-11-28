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

    // Add a new memo to the database
    public int addMemo(int channelId, String content) throws SQLException {
        String sql = "INSERT INTO memos (CHANNEL_ID, CONTENT, SAVED_AT) VALUES (?, ?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, channelId); // Channel ID
            stmt.setString(2, content); // Memo content
            stmt.executeUpdate(); // Execute the insertion

            // Retrieve the generated memo ID
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Return the generated memo ID
                } else {
                    throw new SQLException("Failed to retrieve memo ID.");
                }
            }
        }
    }

    // Get all memos for a specific channel (sorted by creation time)
    public List<MemoDTO> getMemosByRoom(int channelId) {
        List<MemoDTO> memoList = new ArrayList<>();
        String query = "SELECT * FROM memos WHERE CHANNEL_ID = ? ORDER BY CREATED_AT ASC";

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
        String query = "DELETE FROM memos WHERE ID = ?";

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

    // Save a backup of all memos for a specific channel
    public static void saveMemoBackup(int channelId, String memoContent) throws SQLException {
        String query = "INSERT INTO memos (CHANNEL_ID, CONTENT, SAVED_AT) VALUES (?, ?, NOW())";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, channelId); // Channel ID
            stmt.setString(2, memoContent); // Memo content
            stmt.executeUpdate(); // Execute the backup save
        }
    }

    // Retrieve a memo by its ID
    public MemoDTO getMemoById(int memoId) throws SQLException {
        String query = "SELECT * FROM memos WHERE ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, memoId); // Memo ID
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                MemoDTO memo = new MemoDTO();
                memo.setMemoId(rs.getInt("ID")); // Memo ID
                memo.setRoomId(rs.getInt("CHANNEL_ID")); // Channel ID
                memo.setContent(rs.getString("CONTENT")); // Memo content
                memo.setCreatedAt(rs.getTimestamp("SAVED_AT")); // Saved timestamp
                return memo;
            }
        }
        return null; // Return null if the memo does not exist
    }
}
