package MediBook;

import javax.swing.SwingUtilities;

/**
 * PatientRegister is an alias/wrapper for PatientRegistration.
 */
public class PatientRegister extends PatientRegistration {

    public PatientRegister() {
        super();
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new PatientRegister().setVisible(true));
    }
}
