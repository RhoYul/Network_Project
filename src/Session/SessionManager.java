package Session;

import Server.ClientHandler;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ClientHandler> userHandlers = new ConcurrentHashMap<>();

    // 세션 생성
    public void createSession(String sessionId, String userId, ClientHandler handler) {
        sessions.put(sessionId, userId);
        userHandlers.put(userId, handler); // 사용자와 핸들러를 매핑
    }

    // 세션 조회
    public String getUserId(String sessionId) {
        return sessions.get(sessionId); // 세션 ID로 USER_ID 조회
    }

    // 세션 제거
    public void removeSession(String sessionId) {
        String userId = sessions.remove(sessionId);
        if (userId != null) {
            userHandlers.remove(userId); // 핸들러 제거
            System.out.println("Session removed for userId: " + userId);
        } else {
            System.err.println("No session found for sessionId: " + sessionId);
        }
    }


    // Handler 등록
    public void registerHandler(String userId, ClientHandler handler) {
        userHandlers.put(userId, handler);
    }

    // Handler 조회
    public ClientHandler getHandlerByUserId(String userId) {
        return userHandlers.get(userId);
    }

    // Handler 제거
    public void removeHandler(String userId) {
        userHandlers.remove(userId);
    }
}
