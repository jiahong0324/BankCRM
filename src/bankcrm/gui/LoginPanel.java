package bankcrm.gui;

import bankcrm.model.User;
import bankcrm.service.AuthService;
import bankcrm.util.UITheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;


public class LoginPanel extends JPanel {

    private JTextField     tfUsername;
    private JPasswordField tfPassword;
    private JLabel         lblError;

    public LoginPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        buildUI();
    }

    private void buildUI() {
        JPanel leftPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                    0, 0, UITheme.PRIMARY_DARK,
                    0, getHeight(), UITheme.ACCENT);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        leftPanel.setPreferredSize(new Dimension(480, 0));
        leftPanel.setLayout(new GridBagLayout());

        JPanel branding = new JPanel();
        branding.setOpaque(false);
        branding.setLayout(new BoxLayout(branding, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🏦");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel("SecureBank");
        name.setFont(new Font("Segoe UI", Font.BOLD, 32));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Customer Relationship Management");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(new Color(255, 255, 255, 180));
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        branding.add(icon);
        branding.add(Box.createVerticalStrut(12));
        branding.add(name);
        branding.add(Box.createVerticalStrut(6));
        branding.add(tagline);
        branding.add(Box.createVerticalStrut(40));

        JPanel creds = new JPanel();
        creds.setOpaque(false);
        creds.setLayout(new BoxLayout(creds, BoxLayout.Y_AXIS));
        JLabel hint = new JLabel("Demo Accounts");
        hint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        hint.setForeground(new Color(255, 255, 255, 220));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        creds.add(hint);
        for (String line : new String[]{
                "manager  /  BankManager@324",
                "staff1   /  Staff1@324",
                "customer1/  Customer1@324"}) {
            JLabel cl = new JLabel(line);
            cl.setFont(new Font("Consolas", Font.PLAIN, 11));
            cl.setForeground(new Color(255, 255, 255, 160));
            cl.setAlignmentX(Component.CENTER_ALIGNMENT);
            creds.add(cl);
        }
        branding.add(creds);
        leftPanel.add(branding);
        add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(UITheme.BACKGROUND);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UITheme.CARD_BG);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(40, 48, 40, 48)));

        JLabel title = new JLabel("Welcome Back");
        title.setFont(UITheme.F_TITLE);
        title.setForeground(UITheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(UITheme.F_SMALL);
        sub.setForeground(UITheme.TEXT_MID);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(30));

        form.add(fieldLabel("Username"));
        form.add(Box.createVerticalStrut(4));
        tfUsername = UITheme.textField(20);
        tfUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(tfUsername);
        form.add(Box.createVerticalStrut(16));

        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(4));
        tfPassword = UITheme.passwordField(20);
        tfPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tfPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(tfPassword);
        form.add(Box.createVerticalStrut(8));

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.F_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblError);
        form.add(Box.createVerticalStrut(20));

        JButton btnLogin = UITheme.primaryBtn("Sign In");
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.setPreferredSize(new Dimension(300, 40));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(btnLogin);
        form.add(Box.createVerticalStrut(16));

        JPanel regRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        regRow.setOpaque(false);
        regRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noAcc = new JLabel("New customer? ");
        noAcc.setFont(UITheme.F_SMALL);
        noAcc.setForeground(UITheme.TEXT_MID);
        JButton lnkReg = new JButton("Create Account");
        lnkReg.setFont(UITheme.F_SMALL);
        lnkReg.setForeground(UITheme.ACCENT);
        lnkReg.setBorderPainted(false);
        lnkReg.setContentAreaFilled(false);
        lnkReg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regRow.add(noAcc);
        regRow.add(lnkReg);
        form.add(regRow);

        rightPanel.add(form);
        add(rightPanel, BorderLayout.CENTER);

        btnLogin.addActionListener(e -> doLogin());
        lnkReg.addActionListener(e -> MainFrame.getInstance().showRegister());

        ActionListener enterAction = e -> doLogin();
        tfUsername.addActionListener(enterAction);
        tfPassword.addActionListener(enterAction);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.F_LABEL);
        l.setForeground(UITheme.TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void doLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(tfPassword.getPassword());

        if (username.isEmpty()) { lblError.setText("Username is required."); return; }
        if (password.isEmpty()) { lblError.setText("Password is required."); return; }

        User user = AuthService.getInstance().login(username, password);
        if (user == null) {
            lblError.setText("Invalid username or password.");
            tfPassword.setText("");
            return;
        }
        lblError.setText(" ");
        MainFrame.getInstance().showDashboard(user);
    }

    public void reset() {
        tfUsername.setText("");
        tfPassword.setText("");
        lblError.setText(" ");
    }
}
