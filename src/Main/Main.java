// This code is responsible for connecting to the server and launching the login screen.
// When testing the code, you can debug starting from this Main class.

package Main;

import javax.swing.SwingUtilities;
import Client.ClientSocketHandler;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Connect to the server
                ClientSocketHandler clientSocketHandler = new ClientSocketHandler("localhost", 12345);

                // Launch the login screen
                LoginFrame loginFrame = new LoginFrame(clientSocketHandler);
                loginFrame.setVisible(true);
            } catch (IOException e) {
                System.out.println("Unable to connect to the server: " + e.getMessage());
            }
        });
    }
}
