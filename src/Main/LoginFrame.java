// 로그인 화면 구현 (java swing)

package Main;

import User.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private UserDAO userDAO; // UserDAO 쿼리문 선언 (쿼리문 작성은 UserDAO에서)

    public LoginFrame(UserDAO userDAO) {
        this.userDAO = userDAO;
        setTitle("로그인");
        setSize(300, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 5, 5));

        JLabel userIdLabel = new JLabel("아이디:");
        JTextField userIdField = new JTextField();
        JLabel passwdLabel = new JLabel("비밀번호:");
        JPasswordField passwdField = new JPasswordField();
        JButton loginButton = new JButton("로그인");
        JButton registerButton = new JButton("회원가입");

        panel.add(userIdLabel);
        panel.add(userIdField);
        panel.add(passwdLabel);
        panel.add(passwdField);
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);
// 로그인 화면 구현
        
        loginButton.addActionListener(new ActionListener() { // 로그인 버튼
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = userIdField.getText(); // userID 입력받는 부분
                String passwd = new String(passwdField.getPassword()); // passwd 입력받는 부분

                boolean authenticated = userDAO.authenticateUser(userId, passwd); // 데이터베이스에 존재하는지 판단 (boolean)

                if (authenticated) {
                    JOptionPane.showMessageDialog(null, "로그인 성공!"); // 존재하면
                } else {
                    JOptionPane.showMessageDialog(null, "아이디 또는 비밀번호가 올바르지 않습니다."); // 존재하지 않으면
                }
            }
        });

        registerButton.addActionListener(new ActionListener() { // 회원가입 버튼
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterFrame(userDAO).setVisible(true); // 클릭 시 RegisterFrame으로 이동
            }
        });
    }
}
