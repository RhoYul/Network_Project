package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import Client.ClientSocketHandler;

public class LoginFrame extends JFrame {
    private ClientSocketHandler clientSocketHandler;

    public LoginFrame(ClientSocketHandler clientSocketHandler) {
        this.clientSocketHandler = clientSocketHandler; // 소켓 핸들러 주입

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
        JButton registerButton = new JButton("회원가입"); // 회원가입 버튼 추가

        panel.add(userIdLabel);
        panel.add(userIdField);
        panel.add(passwdLabel);
        panel.add(passwdField);
        panel.add(loginButton);
        panel.add(registerButton); // 회원가입 버튼 추가

        add(panel);

        // 로그인 버튼 동작
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = userIdField.getText();
                String passwd = new String(passwdField.getPassword());

                try {
                    // 서버로 로그인 요청
                    String request = "LOGIN " + userId + " " + passwd;
                    String response = clientSocketHandler.sendRequest(request);

                    if ("LOGIN_SUCCESS".equals(response)) {
                        JOptionPane.showMessageDialog(null, "로그인 성공!");
                        new ChannelFrame(clientSocketHandler).setVisible(true); // 채널 화면으로 이동
                        dispose(); // 현재 창 닫기
                    } else {
                        JOptionPane.showMessageDialog(null, "로그인 실패!");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "서버와 통신 중 오류 발생: " + ex.getMessage());
                }
            }
        });

        // 회원가입 버튼 동작
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterFrame(clientSocketHandler).setVisible(true); // 회원가입 화면으로 이동
                dispose(); // 현재 창 닫기
            }
        });
    }
}
