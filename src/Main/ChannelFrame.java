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

        setTitle("Channel Management");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        // Top panel for channel creation
        JPanel createChannelPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JLabel channelLabel = new JLabel("Channel Name:");
        JTextField channelField = new JTextField();
        JButton createChannelButton = new JButton("Create Channel");

        createChannelPanel.add(channelLabel);
        createChannelPanel.add(channelField);
        createChannelPanel.add(createChannelButton);

        // Center panel for channel list
        channelList = new JList<>();
        JScrollPane scrollPane = new JScrollPane(channelList);

        // Bottom panel with action buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        JLabel userInfoLabel = new JLabel("User ID: " + userID, SwingConstants.CENTER);
        JButton joinChannelButton = new JButton("Join Channel");
        JButton deleteChannelButton = new JButton("Delete Channel");
        JButton refreshButton = new JButton("⟳");

        buttonPanel.add(userInfoLabel);
        buttonPanel.add(joinChannelButton);
        buttonPanel.add(deleteChannelButton);
        buttonPanel.add(refreshButton);

        mainPanel.add(createChannelPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Add event listeners
        createChannelButton.addActionListener(e -> createChannel(channelField));
        joinChannelButton.addActionListener(e -> joinSelectedChannel());
        deleteChannelButton.addActionListener(e -> deleteSelectedChannel());
        refreshButton.addActionListener(e -> loadChannelList());

        // Initialize: load channel list from server
        loadChannelList();
    }

    // Load channel list from the server
    private void loadChannelList() {
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                String request = "LIST_CHANNELS SESSION_ID=" + sessionID;
                String response = clientSocketHandler.sendRequest(request);

                if (response.startsWith("CHANNEL_LIST")) {
                    return response.replace("CHANNEL_LIST ", "").split(",");
                } else {
                    throw new IOException("Failed to load channel list: " + response);
                }
            }

            @Override
            protected void done() {
                try {
                    String[] channels = get();
                    channelList.setListData(channels);
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, "Error while loading channel list: " + e.getMessage());
                }
            }
        }.execute();
    }

    // Handle channel creation request
    private void createChannel(JTextField channelField) {
        String channelName = channelField.getText().trim();
        if (channelName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a channel name!");
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
                        JOptionPane.showMessageDialog(null, "Channel created successfully!");
                        loadChannelList(); // Refresh the channel list
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to create channel: " + response);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, "Error while creating channel: " + e.getMessage());
                }
            }
        }.execute();
    }

    // Handle channel join request
    private void joinSelectedChannel() {
        String selectedChannel = channelList.getSelectedValue();
        if (selectedChannel == null) {
            JOptionPane.showMessageDialog(null, "Please select a channel to join!");
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
                        // Parse the participant list from the server
                        String[] responseParts = response.split(" ", 3);
                        String participants = responseParts.length > 2 ? responseParts[2] : "";

                        // Initialize participant list
                        DefaultListModel<String> participantsModel = new DefaultListModel<>();
                        for (String participant : participants.split(",")) {
                            if (!participant.isEmpty()) {
                                participantsModel.addElement(participant);
                            }
                        }

                        // Initialize and display MemoFrame
                        MemoFrame memoFrame = new MemoFrame(clientSocketHandler, selectedChannel, participantsModel, sessionID);
                        memoFrame.setVisible(true);

                        JOptionPane.showMessageDialog(null, "Successfully joined the channel!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to join the channel: " + response);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, "Error while joining the channel: " + e.getMessage());
                }
            }
        }.execute();
    }

    // Handle channel deletion request
    private void deleteSelectedChannel() {
        String selectedChannel = channelList.getSelectedValue();
        if (selectedChannel == null) {
            JOptionPane.showMessageDialog(null, "Please select a channel to delete!");
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
                        JOptionPane.showMessageDialog(null, "Channel deleted successfully!");
                        loadChannelList(); // Refresh the channel list
                    } else if (response.equals("NOT_OWNER")) {
                        JOptionPane.showMessageDialog(null, "You do not have permission to delete this channel.");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to delete the channel: " + response);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(null, "Error while deleting the channel: " + e.getMessage());
                }
            }
        }.execute();
    }
}
