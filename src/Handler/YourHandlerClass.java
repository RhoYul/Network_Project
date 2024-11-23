package Handler;

import Memo.MemoDAO;
import Session.SessionManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class YourHandlerClass {
    private PrintWriter out;  // Define this as an instance variable
    private SessionManager sessionManager;  // SessionManager instance

    // Initialize the sessionManager and PrintWriter in the constructor
    public YourHandlerClass(Socket socket, SessionManager sessionManager) {
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.sessionManager = sessionManager;  // Initialize sessionManager
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handle the memo update request
    private void handleUpdateMemo(String[] parts, String sessionId) {
        if (sessionId == null) {
            out.println("NOT_LOGGED_IN");
            return;
        }

        String userId = sessionManager.getUserId(sessionId);  // Get userId from session
        if (userId == null) {
            out.println("NOT_LOGGED_IN");
            return;
        }

        if (parts.length < 3) {
            out.println("MEMO_UPDATE_FAIL");
            return;
        }

        int memoId = Integer.parseInt(parts[1]);
        String content = parts[2];

        try {
            // Create a MemoDAO instance and pass sessionManager to the constructor
            MemoDAO memoDAO = new MemoDAO(sessionManager);  
            boolean success = memoDAO.updateMemo(memoId, content);  // Update the memo content
            if (success) {
                out.println("MEMO_UPDATED " + memoId);
            } else {
                out.println("MEMO_UPDATE_FAIL");
            }
        } catch (Exception e) {
            out.println("MEMO_UPDATE_FAIL");
            e.printStackTrace();
        }
    }

    // Other methods for handling requests can be added here
}
