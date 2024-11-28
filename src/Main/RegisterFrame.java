// This Frame is responsible for user registration. When the "Register" button is clicked from the main screen, this Frame appears.
// Once the user inputs the required information, a registration request is sent to the server.
// If there are no network issues, the registration details will be saved to the database.

package Main;

import Client.ClientSocketHandler;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class RegisterFrame extends JFrame {
    private ClientSocketHandler clientSocketHandler;

    public RegisterFrame(ClientSocketHandler clientSocketHandler) {
        this.clientSocketHandler = clientSocketHandler;

        // Set frame properties
        setTitle("Register");
        setSize(350, 300);
        setLocationRelativeTo(null);

        // Create a panel for user input fields
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2, 5, 5));

        // Add input fields and labels
        JLabel userIdLabel = new JLabel("Username:");
        JTextField userIdField = new JTextField();
        JLabel passwdLabel = new JLabel("Password:");
        JPasswordField passwdField = new JPasswordField();
        JLabel userNameLabel = new JLabel("Name:");
        JTextField userNameField = new JTextField();
        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();
        JButton registerButton = new JButton("Register");

        // Add components to the panel
        panel.add(userIdLabel);
        panel.add(userIdField);
        panel.add(passwdLabel);
        panel.add(passwdField);
        panel.add(userNameLabel);
        panel.add(userNameField);
        panel.add(emailLabel);
        panel.add(emailField);
        panel.add(new JLabel()); // Empty space
        panel.add(registerButton);

        // Add the panel to the frame
        add(panel);

        // Register button action
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userId = userIdField.getText();
                String passwd = new String(passwdField.getPassword());
                String userName = userNameField.getText();
                String email = emailField.getText();

                // Validate input fields
                if (userId.isEmpty() || passwd.isEmpty() || userName.isEmpty() || email.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill out all fields.");
                    return;
                }

                try {
                    // Send registration request to the server
                    String request = "REGISTER " + userId + " " + passwd + " " + userName + " " + email;
                    String response = clientSocketHandler.sendRequest(request);

                    if ("REGISTER_SUCCESS".equals(response)) {
                        JOptionPane.showMessageDialog(null, "Registration successful!");
                        new LoginFrame(clientSocketHandler).setVisible(true); // Navigate to the login screen
                        dispose(); // Close the current frame
                    } else {
                        JOptionPane.showMessageDialog(null, "Registration failed: " + response);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error communicating with the server: " + ex.getMessage());
                }
            }
        });
    }
}
