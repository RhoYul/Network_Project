package Main;

import javax.swing.SwingUtilities;
import Client.ClientSocketHandler;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 서버 연결
                ClientSocketHandler clientSocketHandler = new ClientSocketHandler("localhost", 12345);

                // 로그인 화면 실행
                LoginFrame loginFrame = new LoginFrame(clientSocketHandler);
                loginFrame.setVisible(true);
            } catch (IOException e) {
                System.out.println("서버에 연결할 수 없습니다: " + e.getMessage());
            }
        });
    }
}
