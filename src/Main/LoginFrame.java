// This Frame is responsible for login functionality. 
// When the login button is clicked, a request is sent to the server, and if the account exists in the database, login will succeed.

package Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import Client.ClientSocketHandler;

public class LoginFrame extends JFrame {
    private ClientSocketHandler clientSocketHandler;

    public LoginFrame(ClientSocketHandler clientSocketHandler) {
        this.clientSocketHandler = clientSocketHandler; // Inject the socket handler

        // Set frame properties
        setTitle("Login");
        setSize(300, 200);
        setLocationRelativeTo(null);

        // Create a panel with a grid layout for login form
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 5, 5));

        // Add labels and input fields for username and password
        JLabel userIdLabel = new JLabel("Username:");
        JTextField userIdField = new JTextField();
        JLabel passwdLabel = new JLabel("Password:");
        JPasswordField passwdField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register"); // Add a register button

        // Add components to the panel
        panel.add(userIdLabel);
        panel.add(userIdField);
        panel.add(passwdLabel);
        panel.add(passwdField);
        panel.add(loginButton);
        panel.add(registerButton); // Add the register button

        // Add the panel to the frame
        add(panel);

        // Action for login button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = userIdField.getText();
                String passwd = new String(passwdField.getPassword());

                try {
                    // Send login request to the server
                    String request = "LOGIN " + userId + " " + passwd;
                    String response = clientSocketHandler.sendRequest(request);

                    if (response.startsWith("LOGIN_SUCCESS")) {
                        String[] responseParts = response.split(" ");
                        if (responseParts.length < 2) {
                            JOptionPane.showMessageDialog(null, "Invalid server response.");
                            return;
                        }
                        String sessionID = responseParts[1]; // "LOGIN_SUCCESS <sessionId>"
                        JOptionPane.showMessageDialog(null, "Login successful!");
                        new ChannelFrame(clientSocketHandler, sessionID, userId).setVisible(true); // Move to channel management screen
                        dispose(); // Close the current frame
                    } else {
                        JOptionPane.showMessageDialog(null, "Login failed!");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error during server communication: " + ex.getMessage());
                }
            }
        });

        // Action for register button
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterFrame(clientSocketHandler).setVisible(true); // Navigate to the registration screen
                dispose(); // Close the current frame
            }
        });
    }
}
