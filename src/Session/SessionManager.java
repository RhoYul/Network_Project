package Session;

import Server.ClientHandler;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClientHandler> userHandlers = new ConcurrentHashMap<>();

    // Create a session
    public void createSession(String sessionId, String userId, ClientHandler handler) {
        sessions.put(sessionId, userId);
        userHandlers.put(userId, handler); // Map the user to their handler
    }

    // Retrieve user ID from session ID
    public String getUserId(String sessionId) {
        return sessions.get(sessionId); // Look up USER_ID using the session ID
    }

    // Remove a session
    public void removeSession(String sessionId) {
        String userId = sessions.remove(sessionId);
        if (userId != null) {
            userHandlers.remove(userId); // Remove the handler
            System.out.println("Session removed for userId: " + userId);
        } else {
            System.err.println("No session found for sessionId: " + sessionId);
        }
    }

    // Register a handler
    public void registerHandler(String userId, ClientHandler handler) {
        userHandlers.put(userId, handler);
    }

    // Retrieve a handler by user ID
    public ClientHandler getHandlerByUserId(String userId) {
        return userHandlers.get(userId);
    }

    // Remove a handler
    public void removeHandler(String userId) {
        userHandlers.remove(userId);
    }
}
