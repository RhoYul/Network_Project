package Main;

import Client.ClientSocketHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class RegisterFrame extends JFrame {
    private ClientSocketHandler clientSocketHandler;

    public RegisterFrame(ClientSocketHandler clientSocketHandler) {
        this.clientSocketHandler = clientSocketHandler;

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

        // 회원가입 버튼 동작
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = userIdField.getText();
                String passwd = new String(passwdField.getPassword());
                String userName = userNameField.getText();
                String email = emailField.getText();

                if (userId.isEmpty() || passwd.isEmpty() || userName.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "모든 필드를 입력하세요.");
                    return;
                }

                try {
                    // 서버로 회원가입 요청
                    String request = "REGISTER " + userId + " " + passwd + " " + userName + " " + email;
                    String response = clientSocketHandler.sendRequest(request);

                    if ("REGISTER_SUCCESS".equals(response)) {
                        JOptionPane.showMessageDialog(null, "회원가입 완료!");
                        new LoginFrame(clientSocketHandler).setVisible(true); // 로그인 화면으로 이동
                        dispose(); // 현재 창 닫기
                    } else {
                        JOptionPane.showMessageDialog(null, "회원가입 실패: " + response);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "서버와 통신 중 오류 발생: " + ex.getMessage());
                }
            }
        });
    }
}
