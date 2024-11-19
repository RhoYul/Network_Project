// 회원가입 화면 구현

package Main;

import User.UserDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterFrame extends JFrame {

    private UserDAO userDAO; // UserDAO 쿼리문 선언 (쿼리문 작성은 UserDAO에서)

    public RegisterFrame(UserDAO userDAO) {
        this.userDAO = userDAO;
        setTitle("회원가입");
        setSize(350, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, 5, 5));

        JLabel userIdLabel = new JLabel("아이디:");
        JTextField userIdField = new JTextField();
        JLabel passwdLabel = new JLabel("비밀번호:");
        JPasswordField passwdField = new JPasswordField();
        JLabel userNameLabel = new JLabel("이름:");
        JTextField userNameField = new JTextField();
        JLabel emailLabel = new JLabel("이메일:");
        JTextField emailField = new JTextField();
        JButton registerButton = new JButton("회원가입");

        panel.add(userIdLabel);
        panel.add(userIdField);
        panel.add(passwdLabel);
        panel.add(passwdField);
        panel.add(userNameLabel);
        panel.add(userNameField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(new JLabel()); // 빈 공간
        panel.add(registerButton);

        add(panel);
        // 회원가입 화면 구현

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = userIdField.getText();
                String passwd = new String(passwdField.getPassword());
                String userName = userNameField.getText();
                String email = emailField.getText();
                // 회원가입 정보 입력받는 부분

                if (userId.isEmpty() || passwd.isEmpty() || userName.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "모든 필드를 입력하세요.");
                    return;
                }
                // 빈 칸이 있을 시

                userDAO.insertUser(userId, passwd, userName, email); // 입력받은 데이터 query문을 통해 데이터베이스에 insert
                JOptionPane.showMessageDialog(null, "회원가입 완료!");
                dispose(); // 창 닫기
            }
        });
    }
}
