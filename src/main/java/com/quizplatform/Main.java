package com.quizplatform;

import com.quizplatform.ui.LoginFrame;
import javax.swing.SwingUtilities;

/**
 * Entry point for Java Online Quiz Platform.
 * Launches the primary LoginFrame on the Event Dispatch Thread (EDT).
 */
public class Main {

    public static void main(String[] args) {
        // Safe startup on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
