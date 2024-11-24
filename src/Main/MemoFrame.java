package Main;

import Client.ClientSocketHandler;
import javax.swing.*;
import Channel.ChannelDAO;
import Memo.*;
import java.awt.*;
import java.io.IOException;

public class MemoFrame extends JFrame {
    private JTextArea memoArea; // 메모 영역
    private JTextArea receivedMemoArea; // 받은 메모 영역
    private String channelName; // 채널 이름
    private int channelId; // 채널 ID
    private ClientSocketHandler clientSocketHandler; // 서버와 통신 핸들러
    private DefaultListModel<String> participantsModel; // 참여자 목록 모델
    private final String sessionID;

    public MemoFrame(ClientSocketHandler clientSocketHandler, String channelName, DefaultListModel<String> participantsModel, String sessionID) {
        this.clientSocketHandler = clientSocketHandler;
        this.channelName = channelName;
        this.participantsModel = participantsModel;
        this.sessionID = sessionID;

        setTitle("Channel Memo - " + channelName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // 상단 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel channelLabel = new JLabel("채널: " + channelName);
        topPanel.add(channelLabel);

        // 메모 영역
        memoArea = new JTextArea();
        memoArea.setEditable(false);
        JScrollPane memoScrollPane = new JScrollPane(memoArea);

        // 받은 메모 영역
        receivedMemoArea = new JTextArea();
        receivedMemoArea.setEditable(false); // 읽기 전용
        receivedMemoArea.setBackground(new Color(230, 240, 250)); // 시각적으로 구분되도록 색상 지정
        JScrollPane receivedMemoScrollPane = new JScrollPane(receivedMemoArea);

        JPanel memoPanel = new JPanel(new GridLayout(2, 1)); // 메모와 받은 메모를 세로로 나란히 배치
        memoPanel.add(memoScrollPane);
        memoPanel.add(receivedMemoScrollPane);

        // 오른쪽 패널 (참여자 목록 및 버튼)
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel participantsLabel = new JLabel("참여자");
        JList<String> participantsList = new JList<>(participantsModel);
        JScrollPane participantsScrollPane = new JScrollPane(participantsList);
        JButton leaveChannelButton = new JButton("채널 퇴장");

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        JButton memoSaveAllButton = new JButton("Memo Save");
        JButton pushMemoButton = new JButton("Push Memo");
        buttonPanel.add(memoSaveAllButton);
        buttonPanel.add(pushMemoButton);
        buttonPanel.add(leaveChannelButton);

        rightPanel.add(participantsLabel, BorderLayout.NORTH);
        rightPanel.add(participantsScrollPane, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 하단 패널
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField memoInputField = new JTextField(30);
        JButton saveMemoButton = new JButton("Send Memo");
        bottomPanel.add(memoInputField);
        bottomPanel.add(saveMemoButton);

        // 메모 전송 액션 리스너
        saveMemoButton.addActionListener(e -> sendMemo(memoInputField));

        // 채널 퇴장 액션 리스너
        leaveChannelButton.addActionListener(e -> leaveChannel());

        // Memo Save 버튼 액션 리스너
        memoSaveAllButton.addActionListener(e -> saveAllMemos());

        // Push Memo 버튼 액션 리스너
        pushMemoButton.addActionListener(e -> pushAllMemos());

        // 프레임에 컴포넌트 추가
        add(topPanel, BorderLayout.NORTH);
        add(memoPanel, BorderLayout.CENTER); // 메모 패널을 중앙에 추가
        add(rightPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // 서버 메시지 수신 시작
        startListening();
    }

    // 채널 퇴장 처리
    private void leaveChannel() {
        int confirm = JOptionPane.showConfirmDialog(this, "정말 이 채널에서 퇴장하시겠습니까?", "채널 퇴장", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                int roomId = ChannelDAO.getChannelIdFromName(channelName);
                return clientSocketHandler.sendRequest("QUIT_CHANNEL " + roomId + " SESSION_ID=" + sessionID);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    if (response.startsWith("CHANNEL_LEFT")) {
                        JOptionPane.showMessageDialog(null, "채널에서 퇴장하였습니다.");
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "채널 퇴장 실패: " + response);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "채널 퇴장 중 오류 발생: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // 메모 전송 처리
    private void sendMemo(JTextField memoInputField) {
        String memoContent = memoInputField.getText();
        if (memoContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "메모 내용을 입력하세요!");
            return;
        }

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                int roomId = ChannelDAO.getChannelIdFromName(channelName);
                return clientSocketHandler.sendRequest("ADD_MEMO " + roomId + " SESSION_ID=" + sessionID + " " + memoContent);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    if (response.startsWith("ADD_MEMO_SUCCESS")) {
                        memoInputField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(null, "메모 전송 실패: " + response);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "메모 전송 중 오류 발생: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // Push Memo 처리
    private void pushAllMemos() {
        String targetChannel = JOptionPane.showInputDialog(this, "메모를 전송할 채널명을 입력하세요:");
        if (targetChannel == null || targetChannel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "채널명을 입력해야 합니다!");
            return;
        }

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                int targetChannelId = ChannelDAO.getChannelIdFromName(targetChannel); // 대상 채널 ID 가져오기
                int currentChannelId = ChannelDAO.getChannelIdFromName(channelName); // 현재 채널 ID 가져오기

                // 메모를 추가하고 memoId 반환
                String memoContent = memoArea.getText().trim(); // 메모 내용을 가져옴
                if (memoContent.isEmpty()) {
                    throw new Exception("메모 내용이 비어 있습니다!");
                }

                // 현재 채널에서 메모를 저장하고 memoId를 받아옴
                String memoId = clientSocketHandler.sendAddMemoRequest(currentChannelId, memoContent, sessionID);

                // memoId와 타겟 채널 ID를 포함한 PUSH_MEMO 요청 생성
                String request = "PUSH_MEMO " + memoId + " " + targetChannelId + " SESSION_ID=" + sessionID;
                System.out.println("Push Memo Request: " + request); // 디버깅용 로그
                return clientSocketHandler.sendRequest(request);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    System.out.println("Received response from server: " + response); // 디버깅용 로그
                    if (response.startsWith("PUSH_MEMO_SUCCESS")) {
                        JOptionPane.showMessageDialog(null, "메모가 성공적으로 전송되었습니다!");
                    } else {
                        JOptionPane.showMessageDialog(null, "메모 전송 실패: " + response);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "메모 전송 중 오류 발생: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }.execute();
    }


    // 서버 메시지 수신 처리
    private void startListening() {
        new Thread(() -> {
            try {
                String response;
                while ((response = clientSocketHandler.receiveResponse()) != null) {
                    if (response.startsWith("USER_JOINED")) {
                        updateParticipants(response, true);
                    } else if (response.startsWith("USER_LEFT")) {
                        updateParticipants(response, false);
                    } else if (response.startsWith("MEMO_UPDATE")) {
                        appendMemo(response.replace("MEMO_UPDATE ", ""));
                    } else if (response.startsWith("RECEIVED_MEMO")) {
                        // 수신한 메모 내용에서 "RECEIVED_MEMO " 제거
                        String memo = response.replace("RECEIVED_MEMO ", "").trim();
                        appendReceivedMemo(memo);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Push 받은 메모를 Received Memo 영역에 추가
    private void appendReceivedMemo(String memo) {
        SwingUtilities.invokeLater(() -> receivedMemoArea.append(memo + "\n"));
    }


    // 참여자 목록 갱신
    private void updateParticipants(String message, boolean joined) {
        String userId = message.split(" ")[1];
        SwingUtilities.invokeLater(() -> {
            if (joined) {
                if (!participantsModel.contains(userId)) {
                    participantsModel.addElement(userId);
                }
            } else {
                participantsModel.removeElement(userId);
            }
        });
    }

    // 메모 추가 처리
    private void appendMemo(String memo) {
        SwingUtilities.invokeLater(() -> memoArea.append(memo + "\n"));
    }
    
    private void saveAllMemos() {
        String allMemos = memoArea.getText();
        if (allMemos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "백업할 메모가 없습니다!");
            return;
        }

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                int channelId = ChannelDAO.getChannelIdFromName(channelName);
                MemoDAO.saveMemoBackup(channelId, allMemos);
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(null, "메모가 데이터베이스에 성공적으로 백업되었습니다!");
            }
        }.execute();
    }
}