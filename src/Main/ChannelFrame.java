package Main;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import Client.ClientSocketHandler;

public class ChannelFrame extends JFrame {
    private ClientSocketHandler clientSocketHandler;
    private String sessionID; // 세션 ID
    private String userID;    // 사용자 ID
    private JList<String> channelList; // 채널 목록

    public ChannelFrame(ClientSocketHandler clientSocketHandler, String sessionID, String userID) {
        this.clientSocketHandler = clientSocketHandler;
        this.sessionID = sessionID;
        this.userID = userID;

        setTitle("채널 관리");
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // 채널 생성 패널
        JPanel createChannelPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JLabel channelLabel = new JLabel("채널 이름:");
        JTextField channelField = new JTextField();
        JButton createChannelButton = new JButton("채널 생성");

        createChannelPanel.add(channelLabel);
        createChannelPanel.add(channelField);
        createChannelPanel.add(createChannelButton);

        // 채널 목록 표시
        channelList = new JList<>();
        JScrollPane scrollPane = new JScrollPane(channelList);

        // 채널 관리 버튼
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5)); // 3개의 버튼 사용
        JButton userInfoButton = new JButton("사용자 ID: " + userID); // 사용자 ID 표시
        JButton joinChannelButton = new JButton("채널 참가"); // 채널 참가 버튼 추가
        JButton refreshButton = new JButton("⟳"); // 새로고침 버튼

        // 새로고침 버튼 크기 설정
        refreshButton.setPreferredSize(new Dimension(40, 25));
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 12));

        buttonPanel.add(userInfoButton); // 사용자 ID 버튼 추가
        buttonPanel.add(joinChannelButton); // 채널 참가 버튼 추가
        buttonPanel.add(refreshButton); // 새로고침 버튼 추가

        mainPanel.add(createChannelPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 초기화: 서버로부터 채널 목록 가져오기
        loadChannelList();

        // 상단 채널 생성 버튼 동작
        createChannelButton.addActionListener(e -> createChannel(channelField));

        // 하단 채널 참가 버튼 동작
        joinChannelButton.addActionListener(e -> joinSelectedChannel());

        // 새로고침 버튼 동작
        refreshButton.addActionListener(e -> loadChannelList());
    }

    // 서버에서 채널 목록 로드
    private void loadChannelList() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    String request = "LIST_CHANNELS SESSION_ID=" + sessionID;
                    String response = clientSocketHandler.sendRequest(request);

                    if (response.startsWith("CHANNEL_LIST")) {
                        String[] channels = response.replace("CHANNEL_LIST ", "").split(",");
                        SwingUtilities.invokeLater(() -> channelList.setListData(channels));
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "채널 목록 로드 실패: " + response));
                    }
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "서버와 통신 중 오류 발생: " + ex.getMessage()));
                }
                return null;
            }
        }.execute();
    }

    // 채널 생성 요청 처리
    private void createChannel(JTextField channelField) {
        String channelName = channelField.getText();
        if (channelName == null || channelName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "채널 이름을 입력하세요!");
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    String request = "CREATE_CHANNEL " + channelName + " SESSION_ID=" + sessionID;
                    String response = clientSocketHandler.sendRequest(request);

                    if (response.startsWith("CHANNEL_CREATED")) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null, "채널 생성 성공!");
                            int channelId = Integer.parseInt(response.split(" ")[1]);
                            MemoFrame memoFrame = new MemoFrame(clientSocketHandler, sessionID, channelId);
                            memoFrame.setVisible(true);
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

    // 채널 참가 요청 처리
    private void joinSelectedChannel() {
    	String selectedChannel = channelList.getSelectedValue();
        if (selectedChannel == null) {
            JOptionPane.showMessageDialog(null, "참가할 채널을 선택하세요!");
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // 서버에 참가 요청 보내기
                    String request = "JOIN_CHANNEL " + selectedChannel + " SESSION_ID=" + sessionID;
                    String response = clientSocketHandler.sendRequest(request); // 서버 응답 받기

                    if (response.startsWith("CHANNEL_JOINED")) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null, "채널 참가 성공!");

                            // 서버 응답에서 채널 ID 또는 이름 추출
                            String[] responseParts = response.split(" ");
                            String channelIdOrName = responseParts[1];

                            try {
                                // 응답이 숫자인 경우 채널 ID로 처리
                                int channelId = Integer.parseInt(channelIdOrName); // 숫자로 변환
                                MemoFrame memoFrame = new MemoFrame(clientSocketHandler, sessionID, channelId);
                                memoFrame.setVisible(true);
                            } catch (NumberFormatException ex) {
                                // 숫자가 아니면 채널 이름으로 처리
                                System.out.println("채널 ID 대신 채널 이름이 반환됨: " + channelIdOrName);
                                JOptionPane.showMessageDialog(null, "채널 이름을 기반으로 이동합니다.");
                                int pseudoChannelId = channelIdOrName.hashCode(); // 이름의 고유 해시값 사용
                                MemoFrame memoFrame = new MemoFrame(clientSocketHandler, sessionID, pseudoChannelId);
                                memoFrame.setVisible(true);
                            }
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null, "채널 참가 실패: " + response);
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
}
