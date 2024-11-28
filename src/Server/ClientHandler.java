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
    
    // Return session ID
    public String getSessionId() {
        return sessionId;
    }

    // Set session ID
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Handle client requests
            String request;
            while ((request = receiveRequest()) != null) {
                processRequest(request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Remove session when the client disconnects
            sessionManager.removeSession(getSessionId());
            closeConnection();
        }
    }

    // Method to receive requests
    private String receiveRequest() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to send response
    private void sendResponse(String response) {
        out.println(response);
    }

    // Send message to the client
    public void sendMessage(String message) {
        out.println(message);
    }

    // Close connection
    private void closeConnection() {
        try {
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Process client request
    private void processRequest(String request) {
        System.out.println("Received request: " + request);

        String[] parts = request.split(" ", 5); // Split into a maximum of 5 parts
        String command = parts[0];
        sessionId = extractSessionId(parts);

        switch (command) {
            case "PUSH_MEMO":
                handlePushMemo(parts, sessionId);
                break;
            case "LOGIN":
                handleLogin(parts);
                break;
            case "REGISTER":
                handleRegister(parts);
                break;
            case "CREATE_CHANNEL":
                handleCreateChannel(parts, sessionId);
                break;
            case "LIST_CHANNELS":
                handleListChannels(sessionId);
                break;
            case "JOIN_CHANNEL":
                handleJoinChannel(parts, sessionId);
                break;
            case "QUIT_CHANNEL":
                handleLeaveChannel(parts, sessionId);
                break;
            case "DELETE_CHANNEL":
                handleDeleteChannel(parts, sessionId);
                break;
            case "GET_MEMOS":
                handleGetMemos(parts, sessionId);
                break;
            case "ADD_MEMO":
                handleAddMemo(parts, sessionId);
                break;
            case "SAVE_MEMO":
                handleSaveMemo(parts, sessionId);
                break;
            default:
                sendResponse("UNKNOWN_COMMAND");
        }
    }

    // Extract session ID from the request
    private String extractSessionId(String[] parts) {
        for (String part : parts) {
            if (part.startsWith("SESSION_ID=")) {
                return part.split("=")[1];
            }
        }
        return null;
    }

    // Manage channel users
    private boolean addUserToChannel(int roomId, String userId) {
        synchronized (channelUsers) {
            channelUsers.putIfAbsent(roomId, new HashSet<>());
            boolean added = channelUsers.get(roomId).add(userId);
            System.out.println("User added: " + userId + " -> Channel " + roomId);
            System.out.println("Current channel user list: " + channelUsers.get(roomId)); // Debugging
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
        synchronized (channelUsers) {
            return channelUsers.getOrDefault(roomId, new HashSet<>());
        }
    }
    
    // Handle channel join
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
            roomId = channelDAO.getChannelIdFromName(channelName);
        } catch (Exception e) {
            sendResponse("INVALID_ROOM_ID");
            e.printStackTrace();
            return;
        }

        synchronized (channelUsers) {
            boolean success = addUserToChannel(roomId, userId);
            if (success) {
                Set<String> participants = getUsersInChannel(roomId);
                String participantsList = String.join(",", participants);
                sendResponse("CHANNEL_JOINED " + channelName + " " + participantsList);
                broadcastToChannel(roomId, "USER_JOINED " + userId);
                System.out.println("Current users in the channel: " + channelUsers.get(roomId));
            } else {
                sendResponse("ALREADY_IN_CHANNEL");
            }
        }
    }

    // Handle leaving a channel
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
            roomId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            sendResponse("INVALID_ROOM_ID");
            return;
        }

        String userId = sessionManager.getUserId(sessionID);
        if (userId == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        try {
            boolean removed = removeUserFromChannel(roomId, userId);
            if (removed) {
                sendResponse("CHANNEL_LEFT " + roomId);
                broadcastToChannel(roomId, "USER_LEFT " + userId);
            } else {
                sendResponse("LEAVE_CHANNEL_FAIL");
            }
        } catch (Exception e) {
            sendResponse("LEAVE_CHANNEL_FAIL");
            e.printStackTrace();
        }
    }

    // Broadcast message to users in a channel
    private void broadcastToChannel(int roomId, String message) {
        synchronized (channelUsers) {
            Set<String> users = channelUsers.get(roomId);
            if (users == null || users.isEmpty()) {
                System.out.println("No users in target channel (Room ID: " + roomId + ").");
                return;
            }

            for (String userId : users) {
                ClientHandler handler = sessionManager.getHandlerByUserId(userId);
                if (handler != null) {
                    System.out.println("Broadcasting to user: " + userId + " | Message: " + message);
                    handler.sendMessage(message);
                } else {
                    System.out.println("Handler not found for user: " + userId);
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

    // Handle login
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
                sessionManager.createSession(sessionId, userId, this);
                sendResponse("LOGIN_SUCCESS " + sessionId);
            } else {
                sendResponse("LOGIN_FAIL");
            }
        } catch (Exception e) {
            sendResponse("LOGIN_FAIL");
            e.printStackTrace();
        }
    }

    // Handle registration
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

    // Handle channel creation
    private void handleCreateChannel(String[] parts, String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        String userId = sessionManager.getUserId(sessionID);
        if (userId == null) {
            sendResponse("CHANNEL_CREATE_FAIL");
            return;
        }

        try {
            channelDAO.createChannel(parts[1], sessionID);
            sendResponse("CHANNEL_CREATED " + parts[1]);
        } catch (Exception e) {
            sendResponse("CHANNEL_CREATE_FAIL");
            e.printStackTrace();
        }
    }

    // Handle channel deletion
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
            boolean deleted = channelDAO.deleteChannel(channelName, ownerId);
            if (deleted) {
                sendResponse("CHANNEL_DELETED " + channelName);
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

    // Handle channel listing
    private void handleListChannels(String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        try {
            List<String> channelNames = channelDAO.getActiveChannelNames();
            sendResponse("CHANNEL_LIST " + String.join(",", channelNames));
        } catch (Exception e) {
            sendResponse("LIST_CHANNELS_FAIL");
            e.printStackTrace();
        }
    }

    // Add a memo
    private void handleAddMemo(String[] parts, String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        try {
            int roomId = Integer.parseInt(parts[1]);
            String memoContent = String.join(" ", Arrays.copyOfRange(parts, 3, parts.length));
            String userId = sessionManager.getUserId(sessionID);

            if (userId == null) {
                sendResponse("NOT_LOGGED_IN");
                return;
            }

            int memoId = memoDAO.addMemo(roomId, memoContent);
            String broadcastMessage = "MEMO_UPDATE " + memoContent;
            broadcastToChannel(roomId, broadcastMessage);
            sendResponse("ADD_MEMO_SUCCESS " + memoId);
        } catch (NumberFormatException e) {
            sendResponse("INVALID_ROOM_ID");
            e.printStackTrace();
        } catch (SQLException e) {
            sendResponse("ADD_MEMO_FAIL");
            e.printStackTrace();
        }
    }
    
    // Push a memo
    private void handlePushMemo(String parts[], String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        try {
            int targetRoomId = Integer.parseInt(parts[1]);
            String memoContent = String.join(" ", Arrays.copyOfRange(parts, 3, parts.length));
            String userId = sessionManager.getUserId(sessionID);

            if (userId == null) {
                sendResponse("NOT_LOGGED_IN");
                return;
            }

            int memoId = memoDAO.addMemo(targetRoomId, memoContent);
            String broadcastMessage = "RECEIVED_MEMO " + memoContent;
            broadcastToChannel(targetRoomId, broadcastMessage);
            sendResponse("PUSH_MEMO_SUCCESS " + memoId);
        } catch (NumberFormatException e) {
            sendResponse("INVALID_ROOM_ID");
            e.printStackTrace();
        } catch (SQLException e) {
            sendResponse("PUSH_MEMO_FAIL");
            e.printStackTrace();
        }
    }
   
    // Retrieve memos
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
            roomId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            sendResponse("INVALID_ROOM_ID");
            return;
        }

        try {
            List<MemoDTO> memos = memoDAO.getMemosByRoom(roomId);
            StringBuilder memoData = new StringBuilder();
            for (MemoDTO memo : memos) {
                memoData.append(memo.getUsername()).append(": ").append(memo.getContent()).append("\n");
            }
            sendResponse("MEMOS " + memoData.toString().trim());
        } catch (Exception e) {
            sendResponse("GET_MEMOS_FAIL");
            e.printStackTrace();
        }
    }

    // Save a memo
    private void handleSaveMemo(String[] parts, String sessionID) {
        if (sessionID == null) {
            sendResponse("NOT_LOGGED_IN");
            return;
        }

        if (parts.length < 3) {
            sendResponse("INVALID_REQUEST");
            return;
        }

        try {
            int roomId = Integer.parseInt(parts[1]);
            String memoContent = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));
            memoDAO.saveMemoBackup(roomId, memoContent);
            sendResponse("SAVE_MEMO_SUCCESS");
        } catch (NumberFormatException e) {
            sendResponse("INVALID_ROOM_ID");
            e.printStackTrace();
        } catch (SQLException e) {
            sendResponse("SAVE_MEMO_FAIL");
            e.printStackTrace();
        } catch (Exception e) {
            sendResponse("SAVE_MEMO_FAIL");
            e.printStackTrace();
        }
    }

    // Generate a session ID
    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
