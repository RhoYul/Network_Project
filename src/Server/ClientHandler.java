package Server;

import java.io.*;
import java.net.*;
import User.UserDAO;

class ClientHandler extends Thread {
    private Socket clientSocket;
    private UserDAO userDAO;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket clientSocket, UserDAO userDAO) {
        this.clientSocket = clientSocket;
        this.userDAO = userDAO;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request;
            while ((request = in.readLine()) != null) {
                String[] parts = request.split(" ");
                String command = parts[0];

                if (command.equals("LOGIN")) { // 로그인 처리
                    String userId = parts[1];
                    String passwd = parts[2];

                    boolean authenticated = userDAO.authenticateUser(userId, passwd);
                    if (authenticated) {
                        out.println("LOGIN_SUCCESS");
                    } else {
                        out.println("LOGIN_FAIL");
                    }
                } else if (command.equals("REGISTER")) { // 회원가입 처리
                    try {
                        // 요청 데이터 분리
                        String userId = parts[1];
                        String passwd = parts[2];
                        String userName = parts[3];
                        String email = parts[4];

                        // 데이터베이스에 회원정보 삽입
                        userDAO.insertUser(userId, passwd, userName, email);

                        // 성공 메시지 전송
                        out.println("REGISTER_SUCCESS");
                    } catch (Exception e) {
                        // 실패 메시지 전송
                        out.println("REGISTER_FAIL");
                        e.printStackTrace(); // 서버 로그 출력
                    }
                } else if (command.equals("CREATE_CHANNEL")) { // 채널 생성 처리
                    String channelName = parts[1];
                    // 채널 생성 로직 (DB 연동)
                    out.println("CHANNEL_CREATED " + channelName);
                } else {
                    out.println("UNKNOWN_COMMAND");
                }
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
}
