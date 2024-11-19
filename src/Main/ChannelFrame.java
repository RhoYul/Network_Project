package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import Client.ClientSocketHandler;

public class ChannelFrame extends JFrame {
    private ClientSocketHandler clientSocketHandler;
    private String sessionID; // 세션 ID

    public ChannelFrame(ClientSocketHandler clientSocketHandler, String sessionID) {
        this.clientSocketHandler = clientSocketHandler;
        this.sessionID = sessionID;

        setTitle("채널 생성");
        setSize(300, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2, 5, 5));

        JLabel channelLabel = new JLabel("채널 이름:");
        JTextField channelField = new JTextField();
        JButton createChannelButton = new JButton("채널 생성");

        panel.add(channelLabel);
        panel.add(channelField);
        panel.add(new JLabel()); // 빈 공간
        panel.add(createChannelButton);

        add(panel);

        // 채널 생성 버튼 동작
        createChannelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String channelName = channelField.getText();

                // 채널 이름 유효성 검사
                if (channelName == null || channelName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "채널 이름을 입력하세요!");
                    return;
                }

                try {
                    // 서버로 채널 생성 요청
                    String request = "CREATE_CHANNEL " + channelName + " SESSION_ID=" + sessionID;
                    System.out.println("Request sent: " + request); // 디버깅용 로그

                    String response = clientSocketHandler.sendRequest(request);
                    System.out.println("Response received: " + response); // 디버깅용 로그

                    if (response.startsWith("CHANNEL_CREATED")) {
                        JOptionPane.showMessageDialog(null, "채널 생성 성공!");
                    } else {
                        JOptionPane.showMessageDialog(null, "채널 생성 실패: " + response);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "서버와 통신 중 오류 발생: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }
}
