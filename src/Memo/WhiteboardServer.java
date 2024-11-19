package Memo;

import java.io.*;
import java.net.*;
import java.util.*;

public class WhiteboardServer {
    private static final int PORT = 12345;
    private static Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("서버가 시작되었습니다...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();  // 새로운 스레드에서 클라이언트 처리
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 클라이언트에게 텍스트 메시지 브로드캐스트
    public static void broadcast(String message, ClientHandler sender) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                if (client != sender) {
                    client.sendMessage(message);  // 다른 클라이언트에게 메시지 전송
                }
            }
        }
    }

    public static void removeClient(ClientHandler handler) {
        clients.remove(handler);
        System.out.println("클라이언트 연결 종료됨.");
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("받은 메시지: " + message);
                WhiteboardServer.broadcast(message, this);  // 메시지를 다른 클라이언트에게 전송
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            WhiteboardServer.removeClient(this);  // 클라이언트 연결 종료
        }
    }

    public void sendMessage(String message) {
        out.println(message);  // 클라이언트에게 메시지 전송
    }
}
