package bankcrm.gui;

import bankcrm.service.UserService;
import bankcrm.util.UITheme;
import bankcrm.util.Validator;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;


public class RegisterPanel extends JPanel {

    private JTextField     tfUsername, tfFullName, tfEmail, tfPhone, tfAddress;
    private JPasswordField tfPassword, tfConfirm;
    private JLabel         lblError;

    public RegisterPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        buildUI();
    }

    private void buildUI() {
        add(UITheme.headerBar("SecureBank  \u2013  Create New Account", null), BorderLayout.NORTH);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BACKGROUND);

        JPanel card = new JPanel();
        card.setBackground(UITheme.CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(36, 44, 36, 44)));

        JLabel title = new JLabel("Customer Registration");
        title.setFont(UITheme.F_TITLE);
        title.setForeground(UITheme.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(24));

        tfFullName = UITheme.textField(18);
        tfUsername = UITheme.textField(18);
        card.add(dualRow("Full Name *", tfFullName, "Username *", tfUsername));
        card.add(Box.createVerticalStrut(14));

        tfEmail = UITheme.textField(18);
        tfPhone = UITheme.textField(18);
        card.add(dualRow("Email *", tfEmail, "Phone *", tfPhone));
        card.add(Box.createVerticalStrut(14));

        tfPassword = UITheme.passwordField(18);
        tfConfirm  = UITheme.passwordField(18);
        card.add(dualRow("Password *", tfPassword, "Confirm Password *", tfConfirm));
        card.add(Box.createVerticalStrut(14));

        tfAddress = UITheme.textField(40);
        card.add(singleRow("Address *", tfAddress));
        card.add(Box.createVerticalStrut(10));

        JLabel hint = UITheme.smallLabel(
            "Password: 8+ chars, uppercase, lowercase, digit, special character",
            UITheme.TEXT_MID);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(hint);
        card.add(Box.createVerticalStrut(10));

        lblError = new JLabel(" ");
        lblError.setFont(UITheme.F_SMALL);
        lblError.setForeground(UITheme.DANGER);
        lblError.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblError);
        card.add(Box.createVerticalStrut(14));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JButton btnBack   = UITheme.secondaryBtn("\u2190 Back to Login");
        JButton btnSubmit = UITheme.primaryBtn("Register");
        btnBack.setPreferredSize(new Dimension(148, 36));
        btnSubmit.setPreferredSize(new Dimension(120, 36));
        btnRow.add(btnBack);
        btnRow.add(btnSubmit);
        card.add(btnRow);

        outer.add(card);
        add(new JScrollPane(outer,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);

        btnBack.addActionListener(e -> MainFrame.getInstance().showLogin());
        btnSubmit.addActionListener(e -> doRegister());
        tfConfirm.addActionListener(e -> doRegister());
    }

    private JPanel dualRow(String l1, JComponent f1, String l2, JComponent f2) {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row.add(labelledField(l1, f1));
        row.add(labelledField(l2, f2));
        return row;
    }

    private JPanel singleRow(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        row.add(UITheme.label(label), BorderLayout.NORTH);
        row.add(field,                BorderLayout.CENTER);
        return row;
    }

    private JPanel labelledField(String label, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        field.setPreferredSize(new Dimension(0, 34));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        p.add(UITheme.label(label), BorderLayout.NORTH);
        p.add(field,                BorderLayout.CENTER);
        return p;
    }

    private void doRegister() {
        String username = tfUsername.getText().trim();
        String fullName = tfFullName.getText().trim();
        String email    = tfEmail.getText().trim();
        String phone    = tfPhone.getText().trim();
        String address  = tfAddress.getText().trim();
        String password = new String(tfPassword.getPassword());
        String confirm  = new String(tfConfirm.getPassword());

        String err;
        if ((err = Validator.validateFullName(fullName)) != null) { show(err); return; }
        if ((err = Validator.validateUsername(username))  != null) { show(err); return; }
        if ((err = Validator.validateEmail(email))        != null) { show(err); return; }
        if ((err = Validator.validatePhone(phone))        != null) { show(err); return; }
        if (address.isEmpty()) { show("Address is required."); return; }
        if ((err = Validator.validatePassword(password))  != null) { show(err); return; }
        if (!password.equals(confirm)) { show("Passwords do not match."); return; }

        err = UserService.getInstance()
                .registerCustomer(username, password, confirm, fullName, email, phone, address);
        if (err != null) { show(err); return; }

        JOptionPane.showMessageDialog(this,
            "Account created successfully!\nYou can now sign in with your credentials.",
            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
        MainFrame.getInstance().showLogin();
    }

    private void show(String msg) { lblError.setText(msg); }

    private void clearForm() {
        tfUsername.setText(""); tfFullName.setText(""); tfEmail.setText("");
        tfPhone.setText(""); tfAddress.setText("");
        tfPassword.setText(""); tfConfirm.setText("");
        lblError.setText(" ");
    }
}
