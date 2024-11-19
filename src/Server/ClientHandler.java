package Server;

import java.io.*;
import java.net.*;
import Session.SessionManager;
import User.UserDAO;
import Channel.ChannelDAO;

class ClientHandler extends Thread {
    private Socket clientSocket;
    private UserDAO userDAO;
    private ChannelDAO channelDAO;
    private SessionManager sessionManager;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket clientSocket, UserDAO userDAO, SessionManager sessionManager, ChannelDAO channelDAO) {
        this.clientSocket = clientSocket;
        this.userDAO = userDAO;
        this.sessionManager = sessionManager;
        this.channelDAO = channelDAO; // 전달된 ChannelDAO 사용
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
    
    // 고유한 세션 ID 생성
    private String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }
}
