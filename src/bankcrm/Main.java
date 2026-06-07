package bankcrm;
import bankcrm.gui.MainFrame;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        String[] ticketCategories = {"ACCOUNT", "CARD", "LOAN", "TRANSACTION", "GENERAL"};
        String[] priorityLevels   = {"LOW", "MEDIUM", "HIGH"};
        String[] statusValues     = {"PENDING", "IN_PROGRESS", "RESOLVED", "CLOSED"};

        System.out.println("=== Supported Ticket Categories ===");
        for (String cat : ticketCategories) {
            System.out.println("  " + cat);
        }

        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.renderer", "sun.java2d.marlin.MarlinRenderingEngine");

        Font baseFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font boldFont = new Font("Segoe UI", Font.BOLD,  13);

        String[] plainKeys = {
            "Button.font","CheckBox.font","ComboBox.font","Label.font",
            "List.font","MenuItem.font","Menu.font","MenuBar.font",
            "OptionPane.font","Panel.font","PopupMenu.font",
            "ProgressBar.font","RadioButton.font","ScrollPane.font",
            "Slider.font","Spinner.font","TabbedPane.font",
            "Table.font","TableHeader.font","TextArea.font",
            "TextField.font","TextPane.font","TitledBorder.font",
            "ToggleButton.font","ToolBar.font","ToolTip.font","Tree.font"
        };

        for (String key : plainKeys)
            UIManager.put(key, baseFont);

        UIManager.put("TabbedPane.font", boldFont);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}