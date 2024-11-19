package Memo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class WhiteboardClient {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 12345;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JTextArea textArea;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WhiteboardClient().start());
    }

    public void start() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // GUI 초기화
            JFrame frame = new JFrame("공동 메모장");
            frame.setLayout(new BorderLayout());
            textArea = new JTextArea();
            textArea.setEditable(true);
            frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
            frame.setSize(600, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            // 텍스트 입력에 대한 키보드 리스너 추가
            textArea.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    String text = textArea.getText();
                    out.println(text);  // 서버에 텍스트 보내기
                }
            });

            // 서버로부터 텍스트 데이터를 수신하여 화면에 표시
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        textArea.setText(message);  // 다른 클라이언트가 보낸 텍스트 반영
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
