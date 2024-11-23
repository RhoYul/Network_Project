// Request Handler 라고 보시면 됩니다. 각종 Request를 개별적인 Thread에서 처리하기 위한 코드라고 보시면 됩니다.

package Server;

import java.io.*;
import java.net.*;
import java.util.List;
import Session.SessionManager;
import User.UserDAO;
import Channel.ChannelDAO;
import Memo.*;

class ClientHandler extends Thread {
    private Socket clientSocket;
    private UserDAO userDAO;
    private ChannelDAO channelDAO;
    private SessionManager sessionManager;
    private BufferedReader in;
    private PrintWriter out;
    private MemoDAO memoDAO;

    public ClientHandler(Socket clientSocket, UserDAO userDAO, SessionManager sessionManager, ChannelDAO channelDAO, MemoDAO memoDAO) {
        this.clientSocket = clientSocket;
        this.userDAO = userDAO;
        this.sessionManager = sessionManager;
        this.channelDAO = channelDAO;
        this.memoDAO = memoDAO; // MemoDAO 추가
    }


    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request;
            while ((request = in.readLine()) != null) {
                processRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 요청 처리
    private void processRequest(String request) {
    	System.out.println("Received request: " + request); // 요청 로그 확인
        String[] parts = request.split(" ");
        String command = parts[0];
        String sessionID = null;
        
     // 세션 ID 추출
        for (String part : parts) {
            if (part.startsWith("SESSION_ID=")) {
                sessionID = part.split("=")[1];
                break;
            }
        }
        switch (command) {
            case "LOGIN":
                handleLogin(parts);
                break;
            case "REGISTER":
                handleRegister(parts);
                break;
            case "CREATE_CHANNEL":
            	if (parts.length >= 2) {
                    String channelName = parts[1];
                    channelDAO.createChannel(channelName, sessionID); // 호출 여부 확인
                } else {
                    System.out.println("Invalid CREATE_CHANNEL request.");
                }
                break;
            case "LIST_CHANNELS":
                handleListChannels(sessionID);
                break;
            case "JOIN_CHANNEL":
                handleJoinChannel(parts, sessionID);
                break;
            case "DELETE_CHANNEL":
                handleDeleteChannel(parts, sessionID);
                break;
            case "GET_MEMOS":
            	handleGetMemos(parts, sessionID);
            	break;
            default:
                out.println("UNKNOWN_COMMAND");
        }
    }

    // 로그인 처리
    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            out.println("LOGIN_FAIL");
            return;
        }

        String userId = parts[1];
        String passwd = parts[2];

        try {
            boolean authenticated = userDAO.authenticateUser(userId, passwd);
            if (authenticated) {
                String sessionId = generateSessionId();
                sessionManager.createSession(sessionId, userId); // 세션 생성
                out.println("LOGIN_SUCCESS " + sessionId); // sessionId를 포함
            } else {
                out.println("LOGIN_FAIL");
            }

        } catch (Exception e) {
            out.println("LOGIN_FAIL");
            e.printStackTrace();
        }
    }

    // 회원가입 처리
    private void handleRegister(String[] parts) {
        if (parts.length < 5) {
            out.println("REGISTER_FAIL");
            return;
        }

        String userId = parts[1];
        String passwd = parts[2];
        String userName = parts[3];
        String email = parts[4];

        try {
            userDAO.insertUser(userId, passwd, userName, email);
            out.println("REGISTER_SUCCESS");
        } catch (Exception e) {
            out.println("REGISTER_FAIL");
            e.printStackTrace();
        }
    }

    // 채널 생성 처리
    private void handleCreateChannel(String[] parts, String sessionID) {
    	if (sessionID == null) {
            out.println("NOT_LOGGED_IN");
            return;
        }
    	
    	String userID = sessionManager.getUserId(sessionID); // 세션에서 USER_ID 가져오기
        if (userID == null) {
            out.println("NOT_LOGGED_IN");
            return;
        }
    	
    	if (parts.length < 2) {
            out.println("CHANNEL_CREATE_FAIL");
            return;
        }

    	String channelName = parts[1];
        try {
			channelDAO.createChannel(channelName, userID); // DB에 채널 생성
            out.println("CHANNEL_CREATED " + channelName);
        } catch (Exception e) {
            out.println("CHANNEL_CREATE_FAIL");
            e.printStackTrace();
        }
    }
    
    // Handle Channel List
    private void handleListChannels(String sessionID) {
        if (sessionID == null) {
            out.println("NOT_LOGGED_IN");
            return;
        }

        try {
            List<String> channelNames = channelDAO.getActiveChannelNames();
            out.println("CHANNEL_LIST " + String.join(",", channelNames));
        } catch (Exception e) {
            out.println("LIST_CHANNELS_FAIL");
            e.printStackTrace();
        }
    }
    
    // Handle joining channel
    private void handleJoinChannel(String[] parts, String sessionID) {
        if (sessionID == null) {
            out.println("NOT_LOGGED_IN");
            return;
        }

        String channelName = parts[1];
        String userId = sessionManager.getUserId(sessionID);
        if (userId == null) {
            out.println("NOT_LOGGED_IN");
            return;
        }

        try {
            boolean success = channelDAO.addUserToChannel(channelName, userId);
            if (success) {
                out.println("CHANNEL_JOINED " + channelName);
            } else {
                out.println("CHANNEL_JOIN_FAIL");
            }
        } catch (Exception e) {
            out.println("CHANNEL_JOIN_FAIL");
            e.printStackTrace();
        }
    }

    // Handle deleting channel
    private void handleDeleteChannel(String[] parts, String sessionID) {
        if (sessionID == null) {
            out.println("NOT_LOGGED_IN");
            return;
        }

        String channelName = parts[1];
        String ownerId = sessionManager.getUserId(sessionID);

        try {
            boolean deleted = channelDAO.deleteChannel(channelName, ownerId);
            if (deleted) {
                out.println("CHANNEL_DELETED " + channelName);
            } else {
                out.println("CHANNEL_DELETE_FAIL");
            }
        } catch (Exception e) {
            out.println("CHANNEL_DELETE_FAIL");
            e.printStackTrace();
        }
    }

 // 메모 조회 요청 처리
    private void handleGetMemos(String[] parts, String sessionID) {
        if (sessionID == null) {
            out.println("NOT_LOGGED_IN");
            return;
        }

        if (parts.length < 2) {
            out.println("INVALID_REQUEST");
            return;
        }

        int roomId;
        try {
            roomId = Integer.parseInt(parts[1]); // roomId 추출
        } catch (NumberFormatException e) {
            out.println("INVALID_ROOM_ID");
            return;
        }

        try {
            List<MemoDTO> memos = memoDAO.getMemosByRoom(roomId);
            StringBuilder memoData = new StringBuilder();

            for (MemoDTO memo : memos) {
                memoData.append(memo.getUsername()).append(": ").append(memo.getContent()).append("\n");
            }

            out.println("MEMOS " + memoData.toString().trim());
        } catch (Exception e) {
            out.println("GET_MEMOS_FAIL");
            e.printStackTrace();
        }
    }
    
    // 고유한 세션 ID 생성
    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }
}
