package Session;

import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private final ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();

    // 세션 생성
    public void createSession(String sessionId, String userId) {
        sessions.put(sessionId, userId);
    }

    // 세션 조회
    public String getUserId(String sessionId) {
        return sessions.get(sessionId); // 세션 ID로 USER_ID 조회
    }

    // 세션 제거
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
}
