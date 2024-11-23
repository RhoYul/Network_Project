package Main;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import Client.ClientSocketHandler;

public class ChannelFrame extends JFrame {
    private final ClientSocketHandler clientSocketHandler;
    private final String sessionID;
    private final String userID;
    private final JList<String> channelList;

    public ChannelFrame(ClientSocketHandler clientSocketHandler, String sessionID, String userID) {
        this.clientSocketHandler = clientSocketHandler;
        this.sessionID = sessionID;
        this.userID = userID;

        setTitle("채널 관리");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // 상단 채널 생성 패널
        JPanel createChannelPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JLabel channelLabel = new JLabel("채널 이름:");
        JTextField channelField = new JTextField();
        JButton createChannelButton = new JButton("채널 생성");

        createChannelPanel.add(channelLabel);
        createChannelPanel.add(channelField);
        createChannelPanel.add(createChannelButton);

        // 중앙 채널 목록
        channelList = new JList<>();
        JScrollPane scrollPane = new JScrollPane(channelList);

        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JLabel userInfoLabel = new JLabel("사용자 ID: " + userID, SwingConstants.CENTER);
        JButton joinChannelButton = new JButton("채널 참가");
        JButton deleteChannelButton = new JButton("채널 삭제");
        JButton refreshButton = new JButton("⟳");

        buttonPanel.add(userInfoLabel);
        buttonPanel.add(joinChannelButton);
        buttonPanel.add(deleteChannelButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(createChannelPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 이벤트 리스너 설정
        createChannelButton.addActionListener(e -> createChannel(channelField));
        joinChannelButton.addActionListener(e -> joinSelectedChannel());
        deleteChannelButton.addActionListener(e -> deleteSelectedChannel());
        refreshButton.addActionListener(e -> loadChannelList());

        // 초기화: 서버에서 채널 목록 로드
        loadChannelList();
    }

    // 서버에서 채널 목록 로드
    private void loadChannelList() {
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String request = "LIST_CHANNELS SESSION_ID=" + sessionID;
                String response = clientSocketHandler.sendRequest(request);

                if (response.startsWith("CHANNEL_LIST")) {
                    return response.replace("CHANNEL_LIST ", "").split(",");
                } else {
                    throw new IOException("채널 목록 로드 실패: " + response);
                }
            }

            @Override
            protected void done() {
                try {
                    String[] channels = get();
                    channelList.setListData(channels);
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, "채널 목록 로드 중 오류 발생: " + e.getMessage());
                }
            }
        }.execute();
    }

    // 채널 생성 요청 처리
    private void createChannel(JTextField channelField) {
        String channelName = channelField.getText().trim();
        if (channelName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "채널 이름을 입력하세요!");
            return;
        }

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String request = "CREATE_CHANNEL " + channelName + " SESSION_ID=" + sessionID;
                return clientSocketHandler.sendRequest(request);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    if (response.startsWith("CHANNEL_CREATED")) {
                        JOptionPane.showMessageDialog(null, "채널 생성 성공!");
                        loadChannelList(); // 채널 목록 새로고침
                    } else {
                        JOptionPane.showMessageDialog(null, "채널 생성 실패: " + response);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, "채널 생성 중 오류 발생: " + e.getMessage());
                }
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

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String request = "JOIN_CHANNEL " + selectedChannel + " SESSION_ID=" + sessionID;
                return clientSocketHandler.sendRequest(request);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    if (response.startsWith("CHANNEL_JOINED")) {
                        // 서버에서 전달된 참여자 목록 파싱
                        String[] responseParts = response.split(" ", 3);
                        String participants = responseParts.length > 2 ? responseParts[2] : "";

                        // 참여자 목록 초기화
                        DefaultListModel<String> participantsModel = new DefaultListModel<>();
                        for (String participant : participants.split(",")) {
                            if (!participant.isEmpty()) {
                                participantsModel.addElement(participant);
                            }
                        }

                        // MemoFrame 초기화 및 표시
                        MemoFrame memoFrame = new MemoFrame(clientSocketHandler, selectedChannel, participantsModel);
                        memoFrame.setVisible(true);

                        JOptionPane.showMessageDialog(null, "채널 참가 성공!");
                    } else {
                        JOptionPane.showMessageDialog(null, "채널 참가 실패: " + response);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, "채널 참가 중 오류 발생: " + e.getMessage());
                }
            }
        }.execute();
    }



    // 채널 삭제 요청 처리
    private void deleteSelectedChannel() {
        String selectedChannel = channelList.getSelectedValue();
        if (selectedChannel == null) {
            JOptionPane.showMessageDialog(null, "삭제할 채널을 선택하세요!");
            return;
        }

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                String request = "DELETE_CHANNEL " + selectedChannel + " SESSION_ID=" + sessionID;
                return clientSocketHandler.sendRequest(request);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    if (response.startsWith("CHANNEL_DELETED")) {
                        JOptionPane.showMessageDialog(null, "채널 삭제 성공!");
                        loadChannelList(); // 채널 목록 새로고침
                    } else if (response.equals("NOT_OWNER")) {
                        JOptionPane.showMessageDialog(null, "채널 삭제 권한이 없습니다.");
                    } else {
                        JOptionPane.showMessageDialog(null, "채널 삭제 실패: " + response);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, "채널 삭제 중 오류 발생: " + e.getMessage());
                }
            }
        }.execute();
    }

}
