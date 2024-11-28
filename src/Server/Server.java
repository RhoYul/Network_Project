package Server;

import java.io.*;
import java.net.*;
import User.UserDAO;
import Session.SessionManager;
import Channel.ChannelDAO;
import Memo.MemoDAO;

public class Server {
    private static final int PORT = 12345; // Server port number
    private UserDAO userDAO;              // Data Access Object for user operations
    private ChannelDAO channelDAO;        // Data Access Object for channel operations
    private SessionManager sessionManager; // Manages user sessions
    private MemoDAO memoDAO;              // Data Access Object for memo operations

    // Constructor: Initialize DAO and session manager
    public Server() {
        this.sessionManager = new SessionManager();           // Initialize SessionManager
        this.userDAO = new UserDAO();                         // Initialize UserDAO
        this.channelDAO = new ChannelDAO(sessionManager);     // Initialize ChannelDAO
        this.memoDAO = new MemoDAO(sessionManager);           // Initialize MemoDAO
    }

    // Start the server
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Listening on port: " + PORT);

            while (true) {
                // Accept incoming client connections
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Start a new thread for each client
                new ClientHandler(clientSocket, userDAO, sessionManager, channelDAO, memoDAO).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main method: Entry point for the server
    public static void main(String[] args) {
        new Server().start();
    }
}
