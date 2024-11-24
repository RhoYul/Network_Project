// Request to the server after establishing a connection.

package Client;

import java.io.*;
import java.net.*;

public class ClientSocketHandler {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientSocketHandler(String serverAddress, int serverPort) throws IOException {
        // Server connection
        this.socket = new Socket(serverAddress, serverPort);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    // Sending request to the server
    public String sendRequest(String request) throws IOException {
        System.out.println("Sending request to server: " + request);
        out.println(request);
        String response = in.readLine();
        System.out.println("Server response: " + response);
        return response;
    }

    // 서버로부터 응답 수신
 	// ClientSocketHandler.receiveResponse()
    public String receiveResponse() throws IOException {
        String response = in.readLine();
        System.out.println("Received response from server: " + response);
        return response;
    }
    
    // Socket closed	
    public void close() throws IOException {
        if (socket != null) socket.close();
    }
    
    // 세션 ID 포함 요청
    public String sendAddMemoRequest(int roomId, String memoContent, String sessionID) throws IOException {
        String request = "ADD_MEMO " + roomId + " SESSION_ID=" + sessionID + " " + memoContent;
        String response = sendRequest(request);
        System.out.println("Server response: " + response);

        if (response.startsWith("ADD_MEMO_SUCCESS")) {
            String memoId = response.split(" ")[1]; // 응답에서 memoId 추출
            System.out.println("Memo successfully added. Memo ID: " + memoId);
            return memoId;
        } else {
            System.out.println("Failed to add memo.");
            return null;
        }
    }

}