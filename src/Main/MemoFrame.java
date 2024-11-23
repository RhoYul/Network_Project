package Main;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import Client.ClientSocketHandler;

public class MemoFrame extends JFrame {
    private ClientSocketHandler clientSocketHandler;
    private String sessionID;
    private int roomId;

    public MemoFrame(ClientSocketHandler clientSocketHandler, String sessionID, int roomId) {
        this.clientSocketHandler = clientSocketHandler;
        this.sessionID = sessionID;
        this.roomId = roomId;

        setTitle("메모 작성 - 채널 ID: " + roomId);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JTextArea memoTextArea = new JTextArea(10, 30);
        JScrollPane scrollPane = new JScrollPane(memoTextArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton saveMemoButton = new JButton("메모 저장");
        panel.add(saveMemoButton, BorderLayout.SOUTH);

        add(panel);

        // 메모 데이터 로드
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    String request = "GET_MEMOS " + roomId + " SESSION_ID=" + sessionID;
                    String response = clientSocketHandler.sendRequest(request);

                    if (response.startsWith("MEMOS")) {
                        String memoData = response.replace("MEMOS ", "");
                        SwingUtilities.invokeLater(() -> memoTextArea.setText(memoData));
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "메모 불러오기 실패: " + response));
                    }
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "서버와 통신 중 오류 발생: " + ex.getMessage()));
                    ex.printStackTrace();
                }
                return null;
            }
        }.execute();

        // 메모 저장 버튼 동작
        saveMemoButton.addActionListener(e -> {
            String memoContent = memoTextArea.getText();

            if (memoContent == null || memoContent.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "메모 내용을 입력하세요!");
                return;
            }

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        String request = "SAVE_MEMO " + roomId + " " + memoContent + " SESSION_ID=" + sessionID;
                        String response = clientSocketHandler.sendRequest(request);

                        if (response.startsWith("MEMO_SAVED")) {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "메모가 저장되었습니다!"));
                        } else {
                            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "메모 저장 실패: " + response));
                        }
                    } catch (IOException ex) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "서버와 통신 중 오류 발생: " + ex.getMessage()));
                        ex.printStackTrace();
                    }
                    return null;
                }
            }.execute();
        });
    }
}
