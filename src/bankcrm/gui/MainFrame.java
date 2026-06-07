package bankcrm.gui;

import bankcrm.gui.customer.CustomerDashboard;
import bankcrm.gui.manager.ManagerDashboard;
import bankcrm.gui.staff.StaffDashboard;
import bankcrm.model.*;
import bankcrm.util.UITheme;

import javax.swing.*;
import java.awt.*;


public class MainFrame extends JFrame {

    private static MainFrame instance;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel     root       = new JPanel(cardLayout);

    private LoginPanel    loginPanel;
    private RegisterPanel registerPanel;
    private User          currentUser;

    public static MainFrame getInstance() { return instance; }

    public MainFrame() {
        instance = this;
        setTitle("SecureBank - Customer Relationship Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1280, 800));
        setSize(1440, 900);
        setLocationRelativeTo(null);

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        Toolkit.getDefaultToolkit().setDynamicLayout(true);

        add(root);
        loginPanel    = new LoginPanel();
        registerPanel = new RegisterPanel();
        root.add(loginPanel,    "LOGIN");
        root.add(registerPanel, "REGISTER");

        showLogin();
    }

    public void showLogin() {
        currentUser = null;
        loginPanel.reset();
        cardLayout.show(root, "LOGIN");
    }

    public void showRegister() {
        cardLayout.show(root, "REGISTER");
    }

    public void navigateTo(String card) { cardLayout.show(root, card); }

    public void showDashboard(User user) {
        this.currentUser = user;
        switch (user.getRole()) {
            case "CUSTOMER": {
                CustomerDashboard cd = new CustomerDashboard((Customer) user);
                root.add(cd, "CUSTOMER");
                cardLayout.show(root, "CUSTOMER");
                break;
            }
            case "STAFF": {
                StaffDashboard sd = new StaffDashboard((Staff) user);
                root.add(sd, "STAFF");
                cardLayout.show(root, "STAFF");
                break;
            }
            case "MANAGER": {
                ManagerDashboard md = new ManagerDashboard((Manager) user);
                root.add(md, "MANAGER");
                cardLayout.show(root, "MANAGER");
                break;
            }
        }
    }

    public void logout() {
        for (Component c : root.getComponents()) {
            if (c instanceof CustomerDashboard
             || c instanceof StaffDashboard
             || c instanceof ManagerDashboard) {
                root.remove(c);
            }
        }
        showLogin();
    }

    public User getCurrentUser() { return currentUser; }

    public static void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg) {
        return JOptionPane.showConfirmDialog(parent, msg, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
