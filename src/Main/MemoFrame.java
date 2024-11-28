package Main;

import Client.ClientSocketHandler;
import javax.swing.*;
import Channel.ChannelDAO;
import Memo.*;
import java.awt.*;
import java.io.IOException;

public class MemoFrame extends JFrame {
    private JTextArea memoArea; // Memo display area
    private JTextArea receivedMemoArea; // Received memo display area
    private String channelName; // Channel name
    private int channelId; // Channel ID
    private ClientSocketHandler clientSocketHandler; // Server communication handler
    private DefaultListModel<String> participantsModel; // Participant list model
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

        // Top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel channelLabel = new JLabel("Channel: " + channelName);
        topPanel.add(channelLabel);

        // Memo display area
        memoArea = new JTextArea();
        memoArea.setEditable(false);
        JScrollPane memoScrollPane = new JScrollPane(memoArea);

        // Received memo display area
        receivedMemoArea = new JTextArea();
        receivedMemoArea.setEditable(false); // Read-only
        receivedMemoArea.setBackground(new Color(230, 240, 250)); // Visual distinction with background color
        JScrollPane receivedMemoScrollPane = new JScrollPane(receivedMemoArea);

        JPanel memoPanel = new JPanel(new GridLayout(2, 1)); // Arrange memo and received memo vertically
        memoPanel.add(memoScrollPane);
        memoPanel.add(receivedMemoScrollPane);

        // Right panel (participant list and buttons)
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel participantsLabel = new JLabel("Participants");
        JList<String> participantsList = new JList<>(participantsModel);
        JScrollPane participantsScrollPane = new JScrollPane(participantsList);
        JButton leaveChannelButton = new JButton("Leave Channel");

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        JButton memoSaveAllButton = new JButton("Memo Save");
        JButton pushMemoButton = new JButton("Push Memo");
        buttonPanel.add(memoSaveAllButton);
        buttonPanel.add(pushMemoButton);
        buttonPanel.add(leaveChannelButton);

        rightPanel.add(participantsLabel, BorderLayout.NORTH);
        rightPanel.add(participantsScrollPane, BorderLayout.CENTER);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField memoInputField = new JTextField(30);
        JButton saveMemoButton = new JButton("Send Memo");
        bottomPanel.add(memoInputField);
        bottomPanel.add(saveMemoButton);

        // Memo send action listener
        saveMemoButton.addActionListener(e -> sendMemo(memoInputField));

        // Leave channel action listener
        leaveChannelButton.addActionListener(e -> leaveChannel());

        // Memo save button action listener
        memoSaveAllButton.addActionListener(e -> saveAllMemos());

        // Push memo button action listener
        pushMemoButton.addActionListener(e -> pushAllMemos(memoInputField));

        // Add components to the frame
        add(topPanel, BorderLayout.NORTH);
        add(memoPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Start receiving server messages
        startListening();
    }

    // Handle leaving the channel
    private void leaveChannel() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to leave this channel?", "Leave Channel", JOptionPane.YES_NO_OPTION);
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
                        JOptionPane.showMessageDialog(null, "You have left the channel.");
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to leave channel: " + response);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error while leaving the channel: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // Handle memo sending
    private void sendMemo(JTextField memoInputField) {
        String memoContent = memoInputField.getText();
        if (memoContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter memo content!");
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
                        JOptionPane.showMessageDialog(null, "Failed to send memo: " + response);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error while sending memo: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // Handle pushing memo to another channel
    private void pushAllMemos(JTextField memoInputField) {
        String memoContent = memoInputField.getText();
        if (memoContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter memo content!");
            return;
        }
        String targetChannel = JOptionPane.showInputDialog(this, "Enter the target channel name:");
        if (targetChannel == null || targetChannel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You must enter a channel name!");
            return;
        }

        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                int targetChannelId = ChannelDAO.getChannelIdFromName(targetChannel); // Get the target channel ID

                // Create PUSH_MEMO request with memoId and target channel ID
                String request = "PUSH_MEMO " + targetChannelId + " SESSION_ID=" + sessionID + " " + memoContent;
                System.out.println("Push Memo Request: " + request); // Debug log
                return clientSocketHandler.sendRequest(request);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    System.out.println("Received response from server: " + response); // Debug log
                    if (response.startsWith("PUSH_MEMO_SUCCESS")) {
                        memoInputField.setText("");
                        JOptionPane.showMessageDialog(null, "Memo successfully sent!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to send memo: " + response);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Error while sending memo: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }.execute();
    }

    // Start receiving server messages
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
                        // Extract memo content
                        String memo = response.replace("RECEIVED_MEMO ", "").trim();
                        appendReceivedMemo(memo);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Append pushed memo to Received Memo area
    private void appendReceivedMemo(String memo) {
        SwingUtilities.invokeLater(() -> receivedMemoArea.append(memo + "\n"));
    }

    // Update participant list
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

    // Append memo to the memo display area
    private void appendMemo(String memo) {
        SwingUtilities.invokeLater(() -> memoArea.append(memo + "\n"));
    }

    // Save all memos to the database
    private void saveAllMemos() {
        String allMemos = memoArea.getText();
        if (allMemos.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are no memos to save!");
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
                JOptionPane.showMessageDialog(null, "Memos successfully backed up to the database!");
            }
        }.execute();
    }
}
