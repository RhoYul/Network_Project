// 채널(메모)를 만들어서 프로그램을 운영하기 위해선 채널의 개설자 정보(ID 등등), 채널에 참여하고 있는 유저의 정보(ID 등등)가 필요한데, 데이터베이스에서 꺼내서 사용하기엔
// 멀티쓰레드에서 충돌이 일어날 수 있다고 해서 Session 방식을 사용했습니다.
// Session 방식은 간단하게 쿠키 시스템이라고 보시면 되는데, 로그인 된 사용자 각자의 정보를 각각 로컬 서버에 정보를 저장하게 되고,
// 그 정보를 꺼내쓰는 방식이라고 생각하시면 됩니다.

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
