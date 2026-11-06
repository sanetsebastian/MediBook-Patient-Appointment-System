package MediBook;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * UITheme provides consistent medical-style UI styling for the MediBook application.
 * Features:
 * - Professional light background and white cards
 * - Medical blue and teal accents
 * - Dark readable text
 * - Clean borders and consistent padding
 */
public class UITheme {

    public static final Color PRIMARY_COLOR   = new Color(16, 85, 154);   // Deep Medical Blue
    public static final Color PRIMARY_DARK    = new Color(10, 56, 105);
    public static final Color ACCENT_TEAL     = new Color(0, 150, 136);   // Teal Accent
    public static final Color BG_LIGHT        = new Color(244, 247, 250); // Clean light background
    public static final Color CARD_BG         = Color.WHITE;
    public static final Color TEXT_PRIMARY    = new Color(30, 41, 59);    // Dark slate text
    public static final Color TEXT_MUTED      = new Color(100, 116, 139);
    public static final Color BORDER_COLOR    = new Color(226, 232, 240);
    public static final Color SUCCESS_GREEN   = new Color(22, 101, 52);   // Green
    public static final Color SUCCESS_BG      = new Color(220, 252, 231);
    public static final Color WARNING_AMBER   = new Color(180, 83, 9);    // Amber
    public static final Color WARNING_BG      = new Color(254, 243, 199);
    public static final Color DANGER_RED      = new Color(185, 28, 28);   // Red
    public static final Color DANGER_BG       = new Color(254, 226, 226);
    public static final Color INFO_BLUE       = new Color(29, 78, 216);
    public static final Color INFO_BG         = new Color(219, 234, 254);

    public static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SECTION     = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BOLD        = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 11);

    public static void applyGlobalTheme() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    public static JPanel createHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_COLOR);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);

        header.add(lblTitle, BorderLayout.WEST);

        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel lblSub = new JLabel(subtitle);
            lblSub.setFont(FONT_SUBTITLE);
            lblSub.setForeground(new Color(224, 242, 254));
            header.add(lblSub, BorderLayout.EAST);
        }

        return header;
    }

    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return card;
    }

    public static void stylePrimaryButton(JButton btn) {
        styleButton(btn, PRIMARY_COLOR, Color.WHITE);
    }

    public static void styleTealButton(JButton btn) {
        styleButton(btn, ACCENT_TEAL, Color.WHITE);
    }

    public static void styleDangerButton(JButton btn) {
        styleButton(btn, DANGER_RED, Color.WHITE);
    }

    public static void styleSuccessButton(JButton btn) {
        styleButton(btn, SUCCESS_GREEN, Color.WHITE);
    }

    public static void styleSecondaryButton(JButton btn) {
        btn.setFont(FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(bg.darker(), 1, true),
            new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(30);
        table.setGridColor(BORDER_COLOR);
        table.setShowGrid(true);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(new Color(224, 242, 254));
        table.setSelectionForeground(TEXT_PRIMARY);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));

        // Center / left align cell renderer with padding
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
                setBorder(new EmptyBorder(4, 10, 4, 10));
                
                String text = val == null ? "" : val.toString();
                // Status badge highlighting
                if ("Scheduled".equalsIgnoreCase(text)) {
                    if (!isSel) {
                        setForeground(INFO_BLUE);
                        setFont(FONT_BOLD);
                    }
                } else if ("Completed".equalsIgnoreCase(text)) {
                    if (!isSel) {
                        setForeground(SUCCESS_GREEN);
                        setFont(FONT_BOLD);
                    }
                } else if ("Delayed".equalsIgnoreCase(text)) {
                    if (!isSel) {
                        setForeground(WARNING_AMBER);
                        setFont(FONT_BOLD);
                    }
                } else if ("Rescheduled".equalsIgnoreCase(text)) {
                    if (!isSel) {
                        setForeground(new Color(124, 58, 237)); // Purple
                        setFont(FONT_BOLD);
                    }
                } else if ("Cancelled".equalsIgnoreCase(text)) {
                    if (!isSel) {
                        setForeground(DANGER_RED);
                        setFont(FONT_BOLD);
                    }
                } else if (!isSel) {
                    setForeground(TEXT_PRIMARY);
                    setFont(FONT_REGULAR);
                }
                return comp;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);
    }
}
