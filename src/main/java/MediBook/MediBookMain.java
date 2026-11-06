package MediBook;

import javax.swing.SwingUtilities;

/**
 * MediBookMain is the primary application launcher.
 */
public class MediBookMain {

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}
