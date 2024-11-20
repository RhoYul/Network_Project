// 최초에 서버 연결을 담당하고, 로그인 화면을 실행하는 코드입니다. 코드 테스트 하실 때 이 Main 에서 디버깅 하시면 됩니다.

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
