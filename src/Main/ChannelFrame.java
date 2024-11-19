package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import Client.ClientSocketHandler;

public class ChannelFrame extends JFrame {
    private ClientSocketHandler clientSocketHandler;

    public ChannelFrame(ClientSocketHandler clientSocketHandler) {
        this.clientSocketHandler = clientSocketHandler;

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

                try {
                    // 서버로 채널 생성 요청
                    String request = "CREATE_CHANNEL " + channelName;
                    String response = clientSocketHandler.sendRequest(request);

                    if (response.startsWith("CHANNEL_CREATED")) {
                        JOptionPane.showMessageDialog(null, "채널 생성 성공!");
                    } else {
                        JOptionPane.showMessageDialog(null, "채널 생성 실패!");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "서버와 통신 중 오류 발생: " + ex.getMessage());
                }
            }
        });
    }
}

