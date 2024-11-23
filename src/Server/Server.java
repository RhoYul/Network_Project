package Server;

import java.io.*;
import java.net.*;
import User.UserDAO;
import Session.SessionManager;
import Channel.ChannelDAO;
import Memo.MemoDAO;

public class Server {
    private static final int PORT = 12345; // 서버 포트 번호
    private UserDAO userDAO;              // DB 접근 객체
    private ChannelDAO channelDAO;
    private SessionManager sessionManager;
    private MemoDAO memoDAO;              // MemoDAO 추가

    public Server() {
        this.sessionManager = new SessionManager(); // sessionManager 먼저 초기화
        this.userDAO = new UserDAO();               // UserDAO 초기화
        this.channelDAO = new ChannelDAO(sessionManager); // ChannelDAO 초기화
        this.memoDAO = new MemoDAO(sessionManager); // MemoDAO 초기화
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("서버가 시작되었습니다. 포트: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("클라이언트 연결됨: " + clientSocket.getInetAddress());
                // MemoDAO 전달
                new ClientHandler(clientSocket, userDAO, sessionManager, channelDAO, memoDAO).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
