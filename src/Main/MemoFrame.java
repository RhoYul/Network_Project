package Main;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import Client.ClientSocketHandler;

public class MemoFrame extends JFrame {
    private JTextArea memoArea; // 메모 영역
    private String channelName; // 채널 이름
    private int channelId; // 채널 ID
    private ClientSocketHandler clientSocketHandler; // 서버와 통신 핸들러
    private DefaultListModel<String> participantsModel; // 참여자 목록 모델

    public MemoFrame(ClientSocketHandler clientSocketHandler, String channelName, DefaultListModel<String> participantsModel) {
        this.clientSocketHandler = clientSocketHandler;
        this.channelName = channelName;
        this.participantsModel = participantsModel;

        setTitle("Channel Memo - " + channelName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        // 상단 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel channelLabel = new JLabel("채널: " + channelName);
        topPanel.add(channelLabel);

        // 메모 영역
        memoArea = new JTextArea();
        memoArea.setEditable(false);
        JScrollPane memoScrollPane = new JScrollPane(memoArea);

        // 오른쪽 패널 (참여자 목록)
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel participantsLabel = new JLabel("참여자");
        JList<String> participantsList = new JList<>(participantsModel);
        JScrollPane participantsScrollPane = new JScrollPane(participantsList);
        JButton deleteChannelButton = new JButton("채널 삭제");

        rightPanel.add(participantsLabel, BorderLayout.NORTH);
        rightPanel.add(participantsScrollPane, BorderLayout.CENTER);
        rightPanel.add(deleteChannelButton, BorderLayout.SOUTH);

        // 하단 패널
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField memoInputField = new JTextField(30);
        JButton saveMemoButton = new JButton("메모 전송");
        bottomPanel.add(memoInputField);
        bottomPanel.add(saveMemoButton);

        // 메모 전송 액션 리스너
        saveMemoButton.addActionListener(e -> sendMemo(memoInputField));

        // 채널 삭제 액션 리스너
        deleteChannelButton.addActionListener(e -> deleteChannel());

        // 프레임에 컴포넌트 추가
        add(topPanel, BorderLayout.NORTH);
        add(memoScrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // 서버 메시지 수신 시작
        startListening();
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
    	            // 서버에 ADD_MEMO 요청
    	            return clientSocketHandler.sendRequest("ADD_MEMO " + channelId + " " + memoContent);
    	        }

    	        @Override
    	        protected void done() {
    	            try {
    	                String response = get();
    	                if (response.startsWith("ADD_MEMO_SUCCESS")) {
    	                    memoInputField.setText(""); // 입력 필드 초기화
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

    // 채널 삭제 요청 처리
    private void deleteChannel() {
    	 int confirm = JOptionPane.showConfirmDialog(this, "정말 이 채널을 삭제하시겠습니까?", "채널 삭제", JOptionPane.YES_NO_OPTION);
    	    if (confirm != JOptionPane.YES_OPTION) {
    	        return;
    	    }

    	    // 비동기로 삭제 요청 처리
    	    new SwingWorker<String, Void>() {
    	        @Override
    	        protected String doInBackground() throws Exception {
    	            // 서버에 DELETE_CHANNEL 요청
    	            return clientSocketHandler.sendRequest("DELETE_CHANNEL " + channelName);
    	        }

    	        @Override
    	        protected void done() {
    	            try {
    	                // 서버 응답 처리
    	                String response = get();
    	                if (response.startsWith("CHANNEL_DELETED")) {
    	                    JOptionPane.showMessageDialog(null, "채널이 삭제되었습니다.");
    	                    dispose(); // 현재 창 닫기
    	                } else {
    	                    JOptionPane.showMessageDialog(null, "채널 삭제 실패: " + response);
    	                }
    	            } catch (Exception ex) {
    	                JOptionPane.showMessageDialog(null, "채널 삭제 중 오류 발생: " + ex.getMessage());
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
                    	System.out.println("Processing USER_JOINED: " + response);
                        updateParticipants(response, true);
                        System.out.println("Current participants: " + participantsModel);
                    } else if (response.startsWith("USER_LEFT")) {
                        updateParticipants(response, false);
                        System.out.println("Current participants: " + participantsModel);
                    } else if (response.startsWith("MEMO_UPDATE")) {
                        appendMemo(response);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    // 참여자 목록 갱신 메서드
    private void updateParticipants(String message, boolean joined) {
    	String userId = message.split(" ")[1]; // 유저 ID 추출
    	SwingUtilities.invokeLater(() -> {
    	    if (joined) {
    	        if (!participantsModel.contains(userId)) {
    	            participantsModel.addElement(userId); // 모델에 추가
    	        }
    	    } else {
    	        participantsModel.removeElement(userId); // 모델에서 제거
    	    }
    	});
    }


    // 메모 추가 처리
    private void appendMemo(String response) {
        String memo = response.substring("MEMO_UPDATE ".length());
        SwingUtilities.invokeLater(() -> memoArea.append(memo + "\n"));
    }
}
