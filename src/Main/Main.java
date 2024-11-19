package Main;

import User.UserDAO;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(userDAO);
            loginFrame.setVisible(true);
        }); // LoginFrame을 불러오는 부분
    }
}
