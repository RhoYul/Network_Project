// The frame for creating a channel (memo). When the 'Create Channel' button is pressed, 
// the client sends a request to the server to create a channel.

package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import Client.ClientSocketHandler;

public class ChannelFrame extends JFrame {
    private ClientSocketHandler clientSocketHandler;
    private String sessionID; // Session ID

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
        panel.add(new JLabel()); // Empty space
        panel.add(createChannelButton);

        add(panel);

        // Channel creation button action
        createChannelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String channelName = channelField.getText();

                // Channel name validation
                if (channelName == null || channelName.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "채널 이름을 입력하세요!");
                    return;
                }

                // Start background task
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        try {
                            // Send channel creation request to the server
                            String request = "CREATE_CHANNEL " + channelName + " SESSION_ID=" + sessionID;
                            System.out.println("Request sent: " + request); // Debugging log

                            String response = clientSocketHandler.sendRequest(request);
                            System.out.println("Response received: " + response); // Debugging log

                            // Handle server response
                            if (response.startsWith("CHANNEL_CREATED")) {
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(null, "채널 생성 성공!");
                                });
                            } else {
                                SwingUtilities.invokeLater(() -> {
                                    JOptionPane.showMessageDialog(null, "채널 생성 실패: " + response);
                                });
                            }
                        } catch (IOException ex) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(null, "서버와 통신 중 오류 발생: " + ex.getMessage());
                            });
                            ex.printStackTrace();
                        }
                        return null;
                    }
                }.execute();
            }
        });

    }
}
