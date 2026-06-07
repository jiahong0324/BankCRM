package bankcrm.util;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;


public final class UITheme {

    private UITheme() {}

    // ── Palette ───────────────────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(13,  71, 161);   
    public static final Color PRIMARY_DARK   = new Color( 1,  53, 120);
    public static final Color PRIMARY_LIGHT  = new Color(66, 165, 245);
    public static final Color ACCENT         = new Color(30, 136, 229);
    public static final Color SIDEBAR_BG     = new Color(20,  49,  98);
    public static final Color SIDEBAR_HOVER  = new Color(30,  70, 130);
    public static final Color HEADER_BG      = new Color(13,  71, 161);
    public static final Color BACKGROUND     = new Color(245, 247, 251);
    public static final Color CARD_BG        = Color.WHITE;
    public static final Color WHITE          = Color.WHITE;
    public static final Color TEXT_DARK      = new Color( 30,  30,  45);
    public static final Color TEXT_MID       = new Color( 90,  90, 110);
    public static final Color TEXT_LIGHT     = new Color(160, 165, 180);
    public static final Color BORDER_COLOR   = new Color(220, 225, 235);

    public static final Color SUCCESS        = new Color( 46, 125,  50);
    public static final Color SUCCESS_LIGHT  = new Color(232, 245, 233);
    public static final Color WARNING        = new Color(230, 119,   0);
    public static final Color WARNING_LIGHT  = new Color(255, 243, 224);
    public static final Color DANGER         = new Color(198,  40,  40);
    public static final Color DANGER_LIGHT   = new Color(255, 235, 238);
    public static final Color INFO           = new Color( 13,  71, 161);
    public static final Color INFO_LIGHT     = new Color(227, 242, 253);
    public static final Color NEUTRAL        = new Color(117, 117, 117);
    public static final Color NEUTRAL_LIGHT  = new Color(245, 245, 245);
    public static final Color ROW_ALT        = new Color(248, 250, 255);
    public static final Color ROW_SELECT     = new Color(212, 230, 255);


    public static Color statusColor(String status) {
        switch (status) {
            case "PENDING":     return WARNING;
            case "IN_PROGRESS": return ACCENT;
            case "RESOLVED":    return SUCCESS;
            case "CLOSED":      return NEUTRAL;
            default:            return TEXT_MID;
        }
    }

    public static Color priorityColor(String p) {
        switch (p) {
            case "HIGH":   return DANGER;
            case "MEDIUM": return WARNING;
            case "LOW":    return SUCCESS;
            default:       return TEXT_MID;
        }
    }

    public static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font F_HEADER  = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font F_LABEL   = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font F_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font F_BUTTON  = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font F_TABLE_H = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font F_TABLE   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_MONO    = new Font("Consolas",  Font.PLAIN, 13);

    public static JButton primaryBtn(String text) {
        return styledBtn(text, PRIMARY, WHITE);
    }

    public static JButton dangerBtn(String text) {
        return styledBtn(text, DANGER, WHITE);
    }

    public static JButton successBtn(String text) {
        return styledBtn(text, SUCCESS, WHITE);
    }

    public static JButton secondaryBtn(String text) {
        JButton b = styledBtn(text, WHITE, PRIMARY);
        b.setBorder(BorderFactory.createLineBorder(PRIMARY, 1));
        return b;
    }

    public static JButton warningBtn(String text) {
        return styledBtn(text, WARNING, WHITE);
    }

    private static JButton styledBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(F_BUTTON);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 36));   
        b.setMargin(new Insets(5, 14, 5, 14));
        return b;
    }

    public static JTextField textField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(F_LABEL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));   
        return f;
    }

    public static JPasswordField passwordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setFont(F_LABEL);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 210), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    public static JTextArea textArea(int rows, int cols) {
        JTextArea a = new JTextArea(rows, cols);
        a.setFont(F_LABEL);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return a;
    }

    public static JLabel headerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_HEADER);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_LABEL);
        l.setForeground(TEXT_DARK);
        return l;
    }

    public static JLabel smallLabel(String text, Color c) {
        JLabel l = new JLabel(text);
        l.setFont(F_SMALL);
        l.setForeground(c);
        return l;
    }

    public static JPanel card(int padding) {
        JPanel p = new JPanel();
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(padding, padding, padding, padding)));
        return p;
    }

    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(F_HEADER);
        l.setForeground(PRIMARY);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return l;
    }

    public static void styleTable(JTable t) {
        t.setFont(F_TABLE);
        t.getTableHeader().setFont(F_TABLE_H);
        t.getTableHeader().setBackground(PRIMARY);
        t.getTableHeader().setForeground(WHITE);
        t.setRowHeight(32);                           
        t.setGridColor(BORDER_COLOR);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(ROW_SELECT);
        t.setSelectionForeground(TEXT_DARK);
        t.setShowVerticalLines(false);
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                c.setFont(F_TABLE);
                if (!sel) c.setBackground(row % 2 == 0 ? WHITE : ROW_ALT);
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    public static JScrollPane scrollPane(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        sp.getVerticalScrollBar().setUnitIncrement(12);
        return sp;
    }

    /** Stat card: big number + label below. */
    public static JPanel statCard(String title, String value, Color accent) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        JLabel num = new JLabel(value);
        num.setFont(new Font("Segoe UI", Font.BOLD, 30)); 
        num.setForeground(accent);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13)); 
        lbl.setForeground(TEXT_MID);

        // Accent bar on the left
        JPanel bar = new JPanel();
        bar.setPreferredSize(new Dimension(5, 0));
        bar.setBackground(accent);

        p.add(bar, BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout(0, 2));
        right.setBackground(CARD_BG);
        right.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        right.add(num, BorderLayout.NORTH);
        right.add(lbl, BorderLayout.CENTER);
        p.add(right, BorderLayout.CENTER);
        return p;
    }

    public static JPanel headerBar(String title, Component right) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(HEADER_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lbl = new JLabel(title);
        lbl.setFont(F_TITLE);
        lbl.setForeground(WHITE);
        bar.add(lbl, BorderLayout.WEST);
        if (right != null) bar.add(right, BorderLayout.EAST);
        return bar;
    }

    public static JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER_COLOR);
        return s;
    }

    public static JComboBox<String> comboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(F_LABEL);
        cb.setBackground(WHITE);
        cb.setPreferredSize(new Dimension(160, 34));
        return cb;
    }

    public static JPanel formRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(F_LABEL);
        lbl.setForeground(TEXT_DARK);
        lbl.setPreferredSize(new Dimension(180, 34));   
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }
}
