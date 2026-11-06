package MediBook;

import javax.swing.SwingUtilities;

/**
 * Login is the entry point alias specified in project structure requirements.
 * Inherits and launches MainMenu portal.
 */
public class Login extends MainMenu {

    public Login() {
        super();
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
