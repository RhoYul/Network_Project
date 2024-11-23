package Server;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import Session.SessionManager;
import User.UserDAO;
import Channel.ChannelDAO;
import Memo.*;

public class ClientHandler extends Thread {
	private String sessionId;
    private Socket clientSocket;
    private UserDAO userDAO;
    private ChannelDAO channelDAO;
    private SessionManager sessionManager;
    private MemoDAO memoDAO;
    private BufferedReader in;
    private PrintWriter out;
    private static final Map<Integer, Set<String>> channelUsers = new HashMap<>();

    public ClientHandler(Socket clientSocket, UserDAO userDAO, SessionManager sessionManager, ChannelDAO channelDAO, MemoDAO memoDAO) {
        this.clientSocket = clientSocket;
        this.userDAO = userDAO;
        this.sessionManager = sessionManager;
        this.channelDAO = channelDAO;
        this.memoDAO = memoDAO;
    }
    
 // 세션 ID 반환
    public String getSessionId() {
        return sessionId;
    }

    // 세션 ID 설정
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // 클라이언트 요청 처리
            String request;
            while ((request = receiveRequest()) != null) {
                processRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 클라이언트 종료 시 세션 제거
            sessionManager.removeSession(getSessionId());
            closeConnection();
        }
    }

    // 요청 수신 메서드
    private String receiveRequest() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 응답 송신 메서드
    private void sendResponse(String response) {
        out.println(response);
    }

    // 클라이언트로 메시지 전송
    public void sendMessage(String message) {
        out.println(message);
    }

    // 연결 종료
    private void closeConnection() {
        try {
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 요청 처리
    private void processRequest(String request) {
        System.out.println("Received request: " + request);

        String[] parts = request.split(" ");
        String command = parts[0];
        String sessionID = extractSessionId(parts);

        switch (command) {
            case "LOGIN":
                handleLogin(parts);
                break;
            case "REGISTER":
                handleRegister(parts);
                break;
            case "CREATE_CHANNEL":
                handleCreateChannel(parts, sessionID);
                break;
            case "LIST_CHANNELS":
                handleListChannels(sessionID);
                break;
            case "JOIN_CHANNEL":
                handleJoinChannel(parts, sessionID);
                break;
            case "QUIT_CHANNEL":
            	handleLeaveChannel(parts, sessionID);
            	break;
            case "DELETE_CHANNEL":
                handleDeleteChannel(parts, sessionID);
                break;
            case "GET_MEMOS":
                handleGetMemos(parts, sessionID);
                break;
            case "ADD_MEMO":
                handleAddMemo(parts, sessionID);
                break;
            default:
                sendResponse("UNKNOWN_COMMAND");
        }
    }

    // 세션 ID 추출 메서드
    private String extractSessionId(String[] parts) {
        for (String part : parts) {
            if (part.startsWith("SESSION_ID=")) {
                return part.split("=")[1];
            }
        }
        return null;
    }

    // 채널 유저 관리
    private boolean addUserToChannel(int roomId, String userId) {
        synchronized (channelUsers) {
            channelUsers.putIfAbsent(roomId, new HashSet<>());
            boolean added = channelUsers.get(roomId).add(userId);
            System.out.println("유저 추가됨: " + userId + " -> 채널 " + roomId);
            System.out.println("현재 채널 유저 목록: " + channelUsers.get(roomId)); // 디버깅
            return added;
        }
    }

    private boolean removeUserFromChannel(int roomId, String userId) {
        synchronized (channelUsers) {
            Set<String> users = channelUsers.get(roomId);
            if (users != null) {
                boolean removed = users.remove(userId);
                if (users.isEmpty()) {
                    channelUsers.remove(roomId);
                }
                return removed;
            }
            return false;
        }
    }
    
    private Set<String> getUsersInChannel(int roomId) {
        synchronized (channelUsers) { // 동기화를 통해 안전하게 접근
            return channelUsers.getOrDefault(roomId, new HashSet<>());
        }
    }
    
    // 채널 참여 관리
    private void handleJoinChannel(String[] parts, String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        if (parts.length < 2) {
            sendResponse("INVALID_REQUEST");
            return;
        }

        String channelName = parts[1];
        String userId = sessionManager.getUserId(sessionID);

        if (userId == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        int roomId;
        try {
            // 채널 이름으로 roomId 가져오기
            roomId = channelDAO.getChannelIdFromName(channelName);
        } catch (Exception e) {
            sendResponse("INVALID_ROOM_ID");
            e.printStackTrace();
            return;
        }

        synchronized (channelUsers) {
            // 유저를 채널에 추가
            boolean success = addUserToChannel(roomId, userId);
            if (success) {
                // 현재 채널 참여자 목록 생성
                Set<String> participants = getUsersInChannel(roomId);
                String participantsList = String.join(",", participants);

                // 참여자 목록과 함께 응답 전송
                sendResponse("CHANNEL_JOINED " + channelName + " " + participantsList);

                // 다른 클라이언트들에게 브로드캐스트
                broadcastToChannel(roomId, "USER_JOINED " + userId);
             // ClientHandler.handleJoinChannel 내부에 디버깅 코드 추가
                System.out.println("현재 채널에 참가한 유저 목록: " + channelUsers.get(roomId));
            } else {
                sendResponse("ALREADY_IN_CHANNEL");
            }
        }
    }
    
    // Quit channel
    private void handleLeaveChannel(String[] parts, String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        if (parts.length < 2) {
            sendResponse("INVALID_REQUEST");
            return;
        }

        int roomId;
        try {
            roomId = Integer.parseInt(parts[1]); // roomId 파싱
        } catch (NumberFormatException e) {
            sendResponse("INVALID_ROOM_ID");
            return;
        }

        String userId = sessionManager.getUserId(sessionID); // 세션에서 유저 ID 가져오기
        if (userId == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        try {
            // 채널에서 유저 제거
            boolean removed = removeUserFromChannel(roomId, userId);
            if (removed) {
                // 유저 나감 성공 응답
                sendResponse("CHANNEL_LEFT " + roomId);

                // 채널에 남아있는 다른 유저들에게 브로드캐스트
                broadcastToChannel(roomId, "USER_LEFT " + userId);
            } else {
                // 채널에서 유저 제거 실패 응답
                sendResponse("LEAVE_CHANNEL_FAIL");
            }
        } catch (Exception e) {
            sendResponse("LEAVE_CHANNEL_FAIL");
            e.printStackTrace();
        }
    }

    //채널 내 유저들에게 방송
    private void broadcastToChannel(int roomId, String message) {
        synchronized (channelUsers) {
            Set<String> users = channelUsers.get(roomId);
            if (users != null) {
                for (String userId : users) {
                    ClientHandler handler = sessionManager.getHandlerByUserId(userId);
                    if (handler != null) {
                        handler.sendMessage(message);
                    }
                }
            }
        }
    }


    private void sendMessageToUser(String userId, String message) {
        ClientHandler handler = sessionManager.getHandlerByUserId(userId);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }

    // 로그인 처리
    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            sendResponse("LOGIN_FAIL");
            return;
        }

        String userId = parts[1];
        String passwd = parts[2];

        try {
            boolean authenticated = userDAO.authenticateUser(userId, passwd);
            if (authenticated) {
                String sessionId = generateSessionId();
                sessionManager.createSession(sessionId, userId, this); // 세션에 핸들러 등록
                sendResponse("LOGIN_SUCCESS " + sessionId);
            } else {
                sendResponse("LOGIN_FAIL");
            }
        } catch (Exception e) {
            sendResponse("LOGIN_FAIL");
            e.printStackTrace();
        }
    }

    // 회원가입 처리
    private void handleRegister(String[] parts) {
        if (parts.length < 5) {
            sendResponse("REGISTER_FAIL");
            return;
        }

        try {
            userDAO.insertUser(parts[1], parts[2], parts[3], parts[4]);
            sendResponse("REGISTER_SUCCESS");
        } catch (Exception e) {
            sendResponse("REGISTER_FAIL");
            e.printStackTrace();
        }
    }

    // 채널 생성
    private void handleCreateChannel(String[] parts, String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        String userId = sessionManager.getUserId(sessionID);
        if (userId == null || parts.length < 2) {
            sendResponse("CHANNEL_CREATE_FAIL");
            return;
        }

        try {
            channelDAO.createChannel(parts[1], userId);
            sendResponse("CHANNEL_CREATED " + parts[1]);
        } catch (Exception e) {
            sendResponse("CHANNEL_CREATE_FAIL");
            e.printStackTrace();
        }
    }
    
    // 채널 삭제
    private void handleDeleteChannel(String[] parts, String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        if (parts.length < 2) {
            sendResponse("INVALID_REQUEST");
            return;
        }

        String channelName = parts[1];
        String ownerId = sessionManager.getUserId(sessionID);

        try {
            // 채널 삭제 처리
            boolean deleted = channelDAO.deleteChannel(channelName, ownerId);
            if (deleted) {
                sendResponse("CHANNEL_DELETED " + channelName);

                // 채널에 남아 있는 유저들에게 알림
                int roomId = channelDAO.getChannelIdFromName(channelName);
                broadcastToChannel(roomId, "CHANNEL_DELETED");
            } else {
                sendResponse("CHANNEL_DELETE_FAIL");
            }
        } catch (Exception e) {
            sendResponse("CHANNEL_DELETE_FAIL");
            e.printStackTrace();
        }
    }



    // 채널 리스트 불러오기
    private void handleListChannels(String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        try {
            // 채널 목록 가져오기
            List<String> channelNames = channelDAO.getActiveChannelNames(); // DB에서 채널 가져오기
            sendResponse("CHANNEL_LIST " + String.join(",", channelNames)); // 응답 전송
        } catch (Exception e) {
            sendResponse("LIST_CHANNELS_FAIL");
            e.printStackTrace();
        }
    }


    // 메모 추가
    private void handleAddMemo(String[] parts, String sessionID) {
        if (sessionID == null || parts.length < 3) {
            sendResponse("INVALID_REQUEST");
            return;
        }

        int roomId;
        try {
            roomId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            sendResponse("INVALID_ROOM_ID");
            return;
        }

        String userId = sessionManager.getUserId(sessionID);
        String memoContent = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));

        try {
            memoDAO.addMemo(roomId, userId, memoContent);
            broadcastToChannel(roomId, "MEMO_UPDATE " + userId + ": " + memoContent);
            sendResponse("ADD_MEMO_SUCCESS");
        } catch (Exception e) {
            sendResponse("ADD_MEMO_FAIL");
            e.printStackTrace();
        }
    }

    
    private void handleGetMemos(String[] parts, String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        if (parts.length < 2) {
            sendResponse("INVALID_REQUEST");
            return;
        }

        int roomId;
        try {
            roomId = Integer.parseInt(parts[1]); // roomId 추출
        } catch (NumberFormatException e) {
            sendResponse("INVALID_ROOM_ID");
            return;
        }

        try {
            List<MemoDTO> memos = memoDAO.getMemosByRoom(roomId); // DB에서 메모 가져오기
            StringBuilder memoData = new StringBuilder();
            for (MemoDTO memo : memos) {
                memoData.append(memo.getUsername()).append(": ").append(memo.getContent()).append("\n");
            }
            sendResponse("MEMOS " + memoData.toString().trim()); // 메모 응답
        } catch (Exception e) {
            sendResponse("GET_MEMOS_FAIL");
            e.printStackTrace();
        }
    }


    // 세션 ID 생성
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
