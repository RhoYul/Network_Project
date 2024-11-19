package Client;

import java.io.*;
import java.net.*;

public class ClientSocketHandler {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientSocketHandler(String serverAddress, int serverPort) throws IOException {
        // 서버 연결
        this.socket = new Socket(serverAddress, serverPort);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // 서버로 요청 보내기
    public String sendRequest(String request) throws IOException {
        out.println(request); // 요청 전송
        return in.readLine(); // 서버 응답 반환
    }

    // 소켓 종료	
    public void close() throws IOException {
        if (socket != null) socket.close();
    }
}
