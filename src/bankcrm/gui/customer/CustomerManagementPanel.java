package bankcrm.gui.customer;

import bankcrm.gui.MainFrame;
import bankcrm.model.*;
import bankcrm.service.*;
import bankcrm.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;


public class CustomerManagementPanel extends JPanel {

    public enum Mode { STAFF_VIEW, MANAGER_VIEW }

    private static final Color C_BG      = new Color(245, 247, 251);
    private static final Color C_CARD    = Color.WHITE;
    private static final Color C_BORDER  = new Color(220, 225, 235);
    private static final Color C_PRIMARY = new Color(13, 71, 161);
    private static final Color C_ACCENT  = new Color(30, 136, 229);
    private static final Color C_SUCCESS = new Color(46, 125, 50);
    private static final Color C_WARN    = new Color(230, 119, 0);
    private static final Color C_DANGER  = new Color(198, 40, 40);
    private static final Color C_NEUTRAL = new Color(117, 117, 117);
    private static final Color C_TDARK   = new Color(30, 30, 45);
    private static final Color C_TMID    = new Color(90, 90, 110);
    private static final Color C_TLIGHT  = new Color(160, 165, 180);
    private static final Font  F_H1      = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font  F_H2      = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font  F_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  F_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font  F_BOLD    = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font  F_TAG     = new Font("Segoe UI", Font.BOLD, 10);

    private final Mode          mode;
    private final User          viewer;
    private final DataStore     ds = DataStore.getInstance();
    private final UserService   us = UserService.getInstance();
    private final AuthService   as = AuthService.getInstance();
    private final TicketService ts = TicketService.getInstance();

    // Table
    private DefaultTableModel tableModel;
    private JTable            table;
    private List<Customer>    displayed;
    private JTextField        tfSearch;
    private JComboBox<String> cbStatus;

    // Right panel
    private JPanel rightPanel;

    // Edit fields (manager only)
    private JTextField     efName, efEmail, efPhone, efAddress;
    private JPasswordField efPwd, efPwdConf;

    public CustomerManagementPanel(Mode mode, User viewer) {
        this.mode   = mode;
        this.viewer = viewer;
        setLayout(new BorderLayout());
        setBackground(C_BG);
        build();
    }


    private void build() {
        add(buildTopBar(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.38);
        split.setDividerSize(1);
        split.setDividerLocation(420);
        split.setLeftComponent(buildListPanel());
        rightPanel = buildEmptyRight();
        split.setRightComponent(rightPanel);
        add(split, BorderLayout.CENTER);

        refresh(null, "ALL");
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setBackground(C_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, C_BORDER),
            BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JPanel left = new JPanel(new BorderLayout(12, 0));
        left.setOpaque(false);

        JPanel iconBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(227, 242, 253));
                g2.fillRoundRect(0, 0, 42, 42, 12, 12);
                g2.setColor(C_PRIMARY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                g2.drawString("\uD83D\uDC65", 8, 30);
            }
        };
        iconBox.setPreferredSize(new Dimension(42, 42));
        iconBox.setOpaque(false);

        JPanel titles = new JPanel(new BorderLayout(0, 3)); titles.setOpaque(false);
        JLabel h1 = new JLabel("Customer Management");
        h1.setFont(F_H1); h1.setForeground(C_PRIMARY);
        String sub = mode == Mode.MANAGER_VIEW
            ? "View, edit, reset passwords and manage account status"
            : "Browse customer profiles and ticket history (read-only)";
        JLabel h2 = new JLabel(sub); h2.setFont(F_SMALL); h2.setForeground(C_TMID);
        titles.add(h1, BorderLayout.NORTH); titles.add(h2, BorderLayout.CENTER);

        left.add(iconBox, BorderLayout.WEST); left.add(titles, BorderLayout.CENTER);
        bar.add(left, BorderLayout.WEST);

        int total  = ds.getCustomers().size();
        int active = (int) ds.getCustomers().stream().filter(User::isActive).count();
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        chips.setOpaque(false);
        chips.add(statChip("Total", String.valueOf(total),   C_PRIMARY));
        chips.add(statChip("Active", String.valueOf(active), C_SUCCESS));
        chips.add(statChip("Inactive", String.valueOf(total - active), C_NEUTRAL));
        bar.add(chips, BorderLayout.EAST);
        return bar;
    }

    private JLabel statChip(String label, String value, Color accent) {
        JLabel l = new JLabel("  " + value + " " + label + "  ");
        l.setFont(F_TAG);
        l.setForeground(accent);
        l.setBackground(accent.equals(C_NEUTRAL) ? new Color(245,245,245) : new Color(
            Math.min(255, accent.getRed()   + 200),
            Math.min(255, accent.getGreen() + 200),
            Math.min(255, accent.getBlue()  + 200)));
        l.setOpaque(true);
        l.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(accent, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return l;
    }

    
    private JPanel buildListPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(C_BG);

        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(C_CARD);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, C_BORDER),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        tfSearch = UITheme.textField(18);
        tfSearch.setToolTipText("Search by name, username, account or email");

        cbStatus = UITheme.comboBox(new String[]{"ALL", "Active", "Inactive"});
        cbStatus.setPreferredSize(new Dimension(120, 34));

        JButton btnSearch  = makeBtn("Search", C_PRIMARY);
        JButton btnRefresh = makeBtn("Refresh", C_NEUTRAL);
        btnSearch.setPreferredSize(new Dimension(80, 34));
        btnRefresh.setPreferredSize(new Dimension(80, 34));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); left.setOpaque(false);
        left.add(tfSearch); left.add(cbStatus);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); right.setOpaque(false);
        right.add(btnSearch); right.add(btnRefresh);
        toolbar.add(left, BorderLayout.CENTER); toolbar.add(right, BorderLayout.EAST);
        p.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Account", "Name", "Type", "Balance", "Status", "#Tickets"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        styleTableHeader(table);
        table.getColumnModel().getColumn(4).setCellRenderer(activeRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(balanceRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        table.setRowHeight(34);

        p.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        btnSearch.addActionListener(e  -> applyFilter());
        btnRefresh.addActionListener(e -> refresh(null, "ALL"));
        cbStatus.addActionListener(e   -> applyFilter());
        tfSearch.addActionListener(e   -> applyFilter());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadRight();
        });
        return p;
    }

    
    private JPanel buildEmptyRight() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(C_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(40, 48, 40, 48)));

        JLabel icon = new JLabel("👤", JLabel.CENTER);
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        JLabel msg = new JLabel("Select a customer to view details", JLabel.CENTER);
        msg.setFont(F_BODY.deriveFont(Font.ITALIC)); msg.setForeground(C_TLIGHT);
        card.add(icon, BorderLayout.CENTER); card.add(msg, BorderLayout.SOUTH);
        p.add(card);
        return p;
    }

    private void loadRight() {
        Customer c = selectedCustomer();
        if (c == null) return;

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(F_BOLD);
        tabs.addTab("  Profile  ",   buildProfileTab(c));
        tabs.addTab("  Account  ",   buildAccountTab(c));
        tabs.addTab("  Tickets  ",   buildTicketsTab(c));
        if (mode == Mode.MANAGER_VIEW) {
            tabs.addTab("  Edit  ",   buildEditTab(c));
            tabs.addTab("  Password  ", buildPasswordTab(c));
        }

        JSplitPane split = (JSplitPane) rightPanel.getParent();
        JPanel newRight = new JPanel(new BorderLayout());
        newRight.setBackground(C_BG);
        newRight.add(tabs, BorderLayout.CENTER);
        rightPanel = newRight;
        split.setRightComponent(rightPanel);
    }

    private JPanel buildProfileTab(Customer c) {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel hero = new JPanel(new BorderLayout(16, 0));
        hero.setBackground(C_CARD);
        hero.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JPanel av = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_PRIMARY);
                g2.fillOval(0, 0, 64, 64);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                String init = c.getFullName().isEmpty() ? "?" : String.valueOf(c.getFullName().charAt(0)).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init, (64 - fm.stringWidth(init)) / 2, (64 + fm.getAscent() - fm.getDescent()) / 2);
            }
        };
        av.setPreferredSize(new Dimension(64, 64)); av.setOpaque(false);

        JPanel mid = new JPanel(new BorderLayout(0, 5)); mid.setOpaque(false);
        JLabel name = new JLabel(c.getFullName());
        name.setFont(new Font("Segoe UI", Font.BOLD, 18)); name.setForeground(C_TDARK);
        JLabel acc  = new JLabel("@" + c.getUsername() + "  ·  " + c.getAccountNumber());
        acc.setFont(F_SMALL); acc.setForeground(C_TMID);
        mid.add(name, BorderLayout.NORTH); mid.add(acc, BorderLayout.CENTER);

        JLabel badge = statusBadge(c.isActive());
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); bp.setOpaque(false);
        bp.add(badge);
        mid.add(bp, BorderLayout.SOUTH);

        hero.add(av, BorderLayout.WEST); hero.add(mid, BorderLayout.CENTER);

        if (mode == Mode.MANAGER_VIEW) {
            JButton toggle = c.isActive() ? makeBtn("Deactivate", C_WARN) : makeBtn("Activate", C_SUCCESS);
            toggle.setPreferredSize(new Dimension(108, 32));
            toggle.addActionListener(e -> {
                String action = c.isActive() ? "Deactivate" : "Activate";
                if (!MainFrame.confirm(this, action + " " + c.getFullName() + "?")) return;
                c.setActive(!c.isActive());
                refresh(null, (String) cbStatus.getSelectedItem());
                loadRight();
            });
            JPanel bPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); bPanel.setOpaque(false);
            bPanel.add(toggle);
            hero.add(bPanel, BorderLayout.EAST);
        }
        p.add(hero, BorderLayout.NORTH);


        JPanel grid = new JPanel(new GridLayout(3, 2, 10, 10));
        grid.setOpaque(false);
        grid.add(infoCard("Email",    c.getEmail(),   C_ACCENT));
        grid.add(infoCard("Phone",    c.getPhone(),   C_PRIMARY));
        grid.add(infoCard("Address",  c.getAddress(), C_NEUTRAL));
        grid.add(infoCard("Registered", c.getFormattedCreatedAt(), C_TMID));
        grid.add(infoCard("Account Type", c.getAccountType(), C_SUCCESS));
        grid.add(infoCard("Open Tickets",
            String.valueOf(ts.getTicketsByCustomer(c.getUserId()).stream()
                .filter(t -> t.getStatus() == Ticket.Status.PENDING ||
                             t.getStatus() == Ticket.Status.IN_PROGRESS).count()),
            C_WARN));
        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildAccountTab(Customer c) {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel card = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(13, 71, 161),
                    getWidth(), getHeight(), new Color(30, 136, 229));
                g2.setPaint(gp); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        card.setPreferredSize(new Dimension(0, 180));

        JLabel bank = new JLabel("SecureBank");
        bank.setFont(new Font("Segoe UI", Font.BOLD, 15)); bank.setForeground(Color.WHITE);

        JLabel acctType = new JLabel(c.getAccountType() + " ACCOUNT");
        acctType.setFont(F_TAG); acctType.setForeground(new Color(180, 220, 255));

        JLabel cardNum = new JLabel(c.getCardNumber());
        cardNum.setFont(new Font("Courier New", Font.BOLD, 16)); cardNum.setForeground(Color.WHITE);

        JLabel accNum = new JLabel(c.getAccountNumber());
        accNum.setFont(new Font("Segoe UI", Font.BOLD, 13)); accNum.setForeground(new Color(210, 235, 255));

        JLabel holder = new JLabel(c.getFullName().toUpperCase());
        holder.setFont(F_TAG); holder.setForeground(new Color(200, 225, 255));

        JPanel topRow = new JPanel(new BorderLayout()); topRow.setOpaque(false);
        topRow.add(bank, BorderLayout.WEST); topRow.add(acctType, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);
        card.add(cardNum, BorderLayout.CENTER);
        JPanel botRow = new JPanel(new BorderLayout()); botRow.setOpaque(false);
        botRow.add(holder, BorderLayout.WEST); botRow.add(accNum, BorderLayout.EAST);
        card.add(botRow, BorderLayout.SOUTH);

        p.add(card, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setOpaque(false);

        JPanel balCard = new JPanel(new BorderLayout(0, 6));
        balCard.setBackground(C_CARD);
        balCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JPanel balTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); balTop.setOpaque(false);
        JLabel bDot = new JLabel("●"); bDot.setFont(F_TAG); bDot.setForeground(C_SUCCESS);
        JLabel bLbl = new JLabel("CURRENT BALANCE"); bLbl.setFont(F_TAG); bLbl.setForeground(C_TLIGHT);
        balTop.add(bDot); balTop.add(bLbl);
        JLabel balVal = new JLabel(c.getFormattedBalance());
        balVal.setFont(new Font("Segoe UI", Font.BOLD, 22)); balVal.setForeground(C_SUCCESS);
        balCard.add(balTop, BorderLayout.NORTH); balCard.add(balVal, BorderLayout.CENTER);

        if (mode == Mode.MANAGER_VIEW) {
            JButton adj = makeBtn("Adjust", C_ACCENT);
            adj.setPreferredSize(new Dimension(80, 28));
            adj.setFont(F_TAG.deriveFont(Font.BOLD));
            adj.addActionListener(e -> showBalanceDialog(c));
            balCard.add(adj, BorderLayout.EAST);
        }

        grid.add(balCard);
        grid.add(infoCard("Account Number",  c.getAccountNumber(),  C_PRIMARY));
        grid.add(infoCard("Card Number",     c.getCardNumber(),      C_ACCENT));
        grid.add(infoCard("Account Type",    c.getAccountType(),     C_NEUTRAL));

        p.add(grid, BorderLayout.CENTER);
        return p;
    }

    private void showBalanceDialog(Customer c) {
        JPanel dlg = new JPanel(new BorderLayout(0, 12));
        dlg.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        JLabel lbl = new JLabel("New balance for " + c.getFullName() + ":");
        lbl.setFont(F_BODY);
        JTextField tfBal = UITheme.textField(20);
        tfBal.setText(String.format("%.2f", c.getBalance()));
        dlg.add(lbl, BorderLayout.NORTH); dlg.add(tfBal, BorderLayout.CENTER);
        int r = JOptionPane.showConfirmDialog(this, dlg, "Adjust Balance",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;
        try {
            double newBal = Double.parseDouble(tfBal.getText().trim().replace(",", ""));
            if (newBal < 0) { MainFrame.showError(this, "Balance cannot be negative."); return; }
            c.setBalance(newBal);
            loadRight();
            refresh(null, (String) cbStatus.getSelectedItem());
        } catch (NumberFormatException ex) {
            MainFrame.showError(this, "Invalid amount.");
        }
    }

    private JPanel buildTicketsTab(Customer c) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(C_BG);
        p.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        List<Ticket> tickets = ts.getTicketsByCustomer(c.getUserId());

        long open  = tickets.stream().filter(t -> t.getStatus()==Ticket.Status.PENDING || t.getStatus()==Ticket.Status.IN_PROGRESS).count();
        long res   = tickets.stream().filter(t -> t.getStatus()==Ticket.Status.RESOLVED || t.getStatus()==Ticket.Status.CLOSED).count();
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); chips.setOpaque(false);
        chips.add(chip("Total: " + tickets.size(),  C_PRIMARY));
        chips.add(chip("Open: " + open,             C_WARN));
        chips.add(chip("Resolved: " + res,          C_SUCCESS));
        p.add(chips, BorderLayout.NORTH);

        if (tickets.isEmpty()) {
            JLabel none = new JLabel("No tickets for this customer yet.", JLabel.CENTER);
            none.setFont(F_BODY.deriveFont(Font.ITALIC)); none.setForeground(C_TLIGHT);
            p.add(none, BorderLayout.CENTER);
        } else {
            String[] cols = {"Ticket ID","Category","Priority","Status","Submitted","Assigned To"};
            DefaultTableModel tm = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r2, int c2) { return false; }
            };
            for (Ticket t : tickets)
                tm.addRow(new Object[]{ t.getTicketId(), t.getCategory().name(),
                    t.getPriority().name(), t.getStatus().name(),
                    t.getFormattedCreatedAt(),
                    t.getAssignedStaffName() != null ? t.getAssignedStaffName() : "Unassigned"});
            JTable tbl = new JTable(tm);
            UITheme.styleTable(tbl); styleTableHeader(tbl);
            tbl.getColumnModel().getColumn(2).setCellRenderer(priorityRenderer());
            tbl.getColumnModel().getColumn(3).setCellRenderer(statusRenderer());
            tbl.setRowHeight(32);
            p.add(UITheme.scrollPane(tbl), BorderLayout.CENTER);
        }
        return p;
    }

    private JPanel buildEditTab(Customer c) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(C_BG);
        outer.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel(new BorderLayout(0, 14));
        form.setBackground(C_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)));

        JLabel title = new JLabel("Edit Customer Profile");
        title.setFont(F_H2); title.setForeground(C_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JPanel fields = new JPanel(new GridBagLayout()); fields.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 4, 6, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        efName    = UITheme.textField(20); efName.setText(c.getFullName());
        efEmail   = UITheme.textField(20); efEmail.setText(c.getEmail());
        efPhone   = UITheme.textField(20); efPhone.setText(c.getPhone());
        efAddress = UITheme.textField(20); efAddress.setText(c.getAddress());

        JComboBox<String> cbType = UITheme.comboBox(new String[]{"SAVINGS","CURRENT"});
        cbType.setSelectedItem(c.getAccountType());

        addRow(fields, gc, 0, "Full Name *", efName,    "Email *",       efEmail);
        addRow(fields, gc, 1, "Phone *",     efPhone,   "Address *",     efAddress);

        gc.gridx=0; gc.gridy=2; gc.gridwidth=1; gc.weightx=0;
        fields.add(mkLabel("Account Type"), gc);
        gc.gridx=1; gc.weightx=1;
        fields.add(cbType, gc);

        JLabel err = new JLabel(" "); err.setFont(F_SMALL); err.setForeground(C_DANGER);
        gc.gridx=0; gc.gridy=3; gc.gridwidth=4; gc.weightx=1;
        fields.add(err, gc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); btnRow.setOpaque(false);
        JButton btnSave  = makeBtn("Save Changes", C_PRIMARY);
        JButton btnReset = makeBtn("Reset", C_NEUTRAL);
        btnSave.setPreferredSize(new Dimension(130, 34));
        btnReset.setPreferredSize(new Dimension(100, 34));
        btnSave.addActionListener(e -> {
            String r = us.updateCustomerProfile(c.getUserId(),
                efName.getText().trim(), efEmail.getText().trim(),
                efPhone.getText().trim(), efAddress.getText().trim());
            if (r != null) { err.setText(r); return; }
            c.setAccountType((String) cbType.getSelectedItem());
            err.setText(" ");
            MainFrame.showInfo(this, "Profile updated for " + c.getFullName() + ".");
            refresh(null, (String) cbStatus.getSelectedItem());
            loadRight();
        });
        btnReset.addActionListener(e -> {
            efName.setText(c.getFullName()); efEmail.setText(c.getEmail());
            efPhone.setText(c.getPhone());   efAddress.setText(c.getAddress());
            cbType.setSelectedItem(c.getAccountType()); err.setText(" ");
        });
        btnRow.add(btnReset); btnRow.add(btnSave);
        gc.gridy=4; fields.add(btnRow, gc);

        form.add(title, BorderLayout.NORTH);
        form.add(fields, BorderLayout.CENTER);
        outer.add(form, BorderLayout.NORTH);
        return outer;
    }

    private JPanel buildPasswordTab(Customer c) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(C_BG);
        outer.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(C_CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)));

        JLabel title = new JLabel("Reset Customer Password");
        title.setFont(F_H2); title.setForeground(C_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Min. 8 chars — uppercase, lowercase, digit, special character.");
        hint.setFont(F_SMALL); hint.setForeground(C_TLIGHT);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        efPwd     = UITheme.passwordField(22);
        efPwdConf = UITheme.passwordField(22);

        JLabel pwdErr = new JLabel(" "); pwdErr.setFont(F_SMALL);
        pwdErr.setForeground(C_DANGER); pwdErr.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnR = makeBtn("Reset Password", C_DANGER);
        btnR.setPreferredSize(new Dimension(160, 36));
        btnR.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnR.addActionListener(e -> {
            String r = as.resetPassword(c.getUserId(),
                new String(efPwd.getPassword()), new String(efPwdConf.getPassword()));
            if (r != null) { pwdErr.setText(r); return; }
            pwdErr.setText(" "); efPwd.setText(""); efPwdConf.setText("");
            MainFrame.showInfo(this, "Password reset for " + c.getFullName() + ".");
        });

        for (Object[] row : new Object[][]{{"New Password *", efPwd}, {"Confirm Password *", efPwdConf}}) {
            JPanel r = UITheme.formRow((String)row[0], (JComponent)row[1]);
            r.setAlignmentX(Component.LEFT_ALIGNMENT);
            r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            form.add(Box.createVerticalStrut(10)); form.add(r);
        }

        form.add(title); form.add(Box.createVerticalStrut(4)); form.add(hint);
        form.add(Box.createVerticalStrut(16)); form.add(pwdErr);
        form.add(Box.createVerticalStrut(8)); form.add(btnR);
        outer.add(form, BorderLayout.NORTH);
        return outer;
    }

    
    private Customer selectedCustomer() {
        int row = table.getSelectedRow();
        if (row < 0 || displayed == null) return null;
        int idx = table.convertRowIndexToModel(row);
        return idx < displayed.size() ? displayed.get(idx) : null;
    }

    private void applyFilter() {
        String q = tfSearch.getText().trim();
        refresh(q.isEmpty() ? null : q, (String) cbStatus.getSelectedItem());
    }

    private void refresh(String search, String status) {
        List<Customer> all = ds.getCustomers();
        if (search != null) {
            String q = search.toLowerCase();
            all = all.stream().filter(c ->
                c.getFullName().toLowerCase().contains(q) ||
                c.getUsername().toLowerCase().contains(q) ||
                c.getAccountNumber().toLowerCase().contains(q) ||
                c.getEmail().toLowerCase().contains(q)
            ).collect(Collectors.toList());
        }
        if ("Active".equals(status))   all = all.stream().filter(User::isActive).collect(Collectors.toList());
        if ("Inactive".equals(status)) all = all.stream().filter(c -> !c.isActive()).collect(Collectors.toList());
        displayed = all;
        tableModel.setRowCount(0);
        for (Customer c : displayed)
            tableModel.addRow(new Object[]{
                c.getAccountNumber(), c.getFullName(), c.getAccountType(),
                c.getFormattedBalance(),
                c.isActive() ? "Active" : "Inactive",
                ts.getTicketsByCustomer(c.getUserId()).size()});
    }

   
    private JButton makeBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(F_BOLD); b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel mkLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(F_BODY); l.setForeground(C_TDARK); return l;
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row,
                        String l1, JTextField f1, String l2, JTextField f2) {
        gc.gridwidth=1; gc.weightx=0; gc.gridx=0; gc.gridy=row; p.add(mkLabel(l1), gc);
        gc.gridx=1; gc.weightx=1; p.add(f1, gc);
        gc.gridx=2; gc.weightx=0; p.add(mkLabel(l2), gc);
        gc.gridx=3; gc.weightx=1; p.add(f2, gc);
    }

    private JPanel infoCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(C_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)); top.setOpaque(false);
        JLabel dot = new JLabel("●"); dot.setFont(new Font("Segoe UI", Font.PLAIN, 8)); dot.setForeground(accent);
        JLabel lbl = new JLabel(label.toUpperCase()); lbl.setFont(F_TAG); lbl.setForeground(C_TLIGHT);
        top.add(dot); top.add(lbl);
        JLabel val = new JLabel(value == null || value.isEmpty() ? "–" : value);
        val.setFont(F_BOLD); val.setForeground(C_TDARK);
        card.add(top, BorderLayout.NORTH); card.add(val, BorderLayout.CENTER);
        return card;
    }

    private JLabel statusBadge(boolean active) {
        JLabel l = new JLabel(active ? "  ● ACTIVE  " : "  ● INACTIVE  ");
        l.setFont(F_TAG);
        l.setForeground(Color.WHITE);
        l.setBackground(active ? C_SUCCESS : C_NEUTRAL);
        l.setOpaque(true);
        l.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return l;
    }

    private JLabel chip(String text, Color accent) {
        JLabel l = new JLabel("  " + text + "  "); l.setFont(F_TAG);
        l.setForeground(accent);
        l.setBackground(new Color(
            Math.min(255, accent.getRed()   + 195),
            Math.min(255, accent.getGreen() + 195),
            Math.min(255, accent.getBlue()  + 200)));
        l.setOpaque(true);
        l.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(accent, 1, true),
            BorderFactory.createEmptyBorder(3, 8, 3, 8)));
        return l;
    }

    private void styleTableHeader(JTable t) {
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                l.setBackground(C_PRIMARY); l.setForeground(Color.WHITE);
                l.setFont(UITheme.F_TABLE_H);
                l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10)); l.setOpaque(true); return l;
            }
        });
        h.setBackground(C_PRIMARY); h.setForeground(Color.WHITE); h.setOpaque(true);
    }

    private TableCellRenderer activeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel && v != null) {
                    l.setForeground("Active".equals(v) ? C_SUCCESS : C_NEUTRAL);
                    l.setFont(UITheme.F_TABLE.deriveFont(Font.BOLD));
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8)); return l;
            }
        };
    }

    private TableCellRenderer balanceRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) { l.setForeground(C_SUCCESS); l.setFont(F_BOLD); }
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8)); return l;
            }
        };
    }

    private TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel && v != null) {
                    l.setForeground(UITheme.statusColor(v.toString()));
                    l.setFont(UITheme.F_TABLE.deriveFont(Font.BOLD));
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8)); return l;
            }
        };
    }

    private TableCellRenderer priorityRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel && v != null) l.setForeground(UITheme.priorityColor(v.toString()));
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8)); return l;
            }
        };
    }
}
