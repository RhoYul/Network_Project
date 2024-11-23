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

    // Sending request tod the server
    public String sendRequest(String request) throws IOException {
        out.println(request); // Sending request
        return in.readLine(); // Returning a response from the server
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
}