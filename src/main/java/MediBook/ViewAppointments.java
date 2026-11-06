package MediBook;

import javax.swing.SwingUtilities;

/**
 * ViewAppointments is an alias for ViewAppoinments.
 */
public class ViewAppointments extends ViewAppoinments {

    public ViewAppointments() {
        super();
    }

    public static void main(String[] args) {
        UITheme.applyGlobalTheme();
        SwingUtilities.invokeLater(() -> new ViewAppointments().setVisible(true));
    }
}
