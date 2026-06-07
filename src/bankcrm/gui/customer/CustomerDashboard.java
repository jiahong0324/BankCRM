package bankcrm.gui.customer;

import bankcrm.gui.MainFrame;
import bankcrm.model.*;
import bankcrm.service.*;
import bankcrm.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerDashboard extends JPanel {

    private final Customer            customer;
    private final TicketService       ts  = TicketService.getInstance();
    private final NotificationService ns  = NotificationService.getInstance();
    private final AuthService         as  = AuthService.getInstance();
    private final UserService         us  = UserService.getInstance();
    private final DataStore           ds  = DataStore.getInstance();

    private JTabbedPane tabs;
    private JLabel      notifBadge;

    public CustomerDashboard(Customer customer) {
        this.customer = customer;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        buildUI();
    }

    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JLabel title = new JLabel("  SecureBank CRM  -  Customer Portal");
        title.setFont(UITheme.F_TITLE); title.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        long unread = ns.unreadCount(customer.getUserId());
        notifBadge = new JLabel("Notifications" + (unread > 0 ? " (" + unread + ")" : ""));
        notifBadge.setFont(UITheme.F_BUTTON);
        notifBadge.setForeground(unread > 0 ? UITheme.WARNING_LIGHT : Color.WHITE);

        JLabel userLbl = new JLabel(customer.getFullName() + "  |  " + customer.getAccountNumber());
        userLbl.setFont(UITheme.F_LABEL); userLbl.setForeground(new Color(200, 220, 255));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UITheme.F_BUTTON); btnLogout.setBackground(new Color(220, 80, 60));
        btnLogout.setForeground(Color.WHITE); btnLogout.setFocusPainted(false);
        btnLogout.setOpaque(true); btnLogout.setBorderPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(88, 32));
        btnLogout.addActionListener(e -> { if (MainFrame.confirm(this, "Logout?")) MainFrame.getInstance().logout(); });

        right.add(notifBadge); right.add(userLbl); right.add(btnLogout);
        header.add(title, BorderLayout.WEST); header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(UITheme.F_BUTTON);
        tabs.addTab("  Overview",       buildOverviewTab());
        tabs.addTab("  Help & FAQ",     buildFAQTab());
        tabs.addTab("  Submit Ticket",  buildSubmitTab());
        tabs.addTab("  My Tickets",     buildMyTicketsTab());
        tabs.addTab("  Profile",        buildProfileTab());
        add(tabs, BorderLayout.CENTER);

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 3) refreshTicketsTab();
            if (tabs.getSelectedIndex() == 0) refreshOverview();
        });
    }

    
    private JPanel overviewTab;
    private JPanel statsRow, notifPanel, annPanel;

    private JPanel buildOverviewTab() {
        overviewTab = new JPanel(new BorderLayout(0, 14));
        overviewTab.setBackground(UITheme.BACKGROUND);
        overviewTab.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JPanel welcomeBar = new JPanel(new BorderLayout());
        welcomeBar.setOpaque(false);
        JLabel welcome = new JLabel("Welcome back, " + customer.getFullName() + "!");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcome.setForeground(UITheme.PRIMARY);
        JLabel sub = UITheme.smallLabel("Account: " + customer.getAccountNumber(), UITheme.TEXT_MID);
        JPanel wLeft = new JPanel(new BorderLayout(0, 2)); wLeft.setOpaque(false);
        wLeft.add(welcome, BorderLayout.NORTH); wLeft.add(sub, BorderLayout.CENTER);
        welcomeBar.add(wLeft, BorderLayout.WEST);
        overviewTab.add(welcomeBar, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        statsRow = new JPanel(new GridLayout(1, 3, 14, 0));
        statsRow.setOpaque(false);
        body.add(statsRow, BorderLayout.NORTH);

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 16, 0));
        bottomRow.setOpaque(false);

        JPanel annCard = new JPanel(new BorderLayout(0, 0));
        annCard.setBackground(UITheme.CARD_BG);
        annCard.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true));

        JPanel annHeader = new JPanel(new BorderLayout());
        annHeader.setBackground(UITheme.PRIMARY);
        annHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel annTitle = new JLabel("Announcements");
        annTitle.setFont(UITheme.F_BUTTON); annTitle.setForeground(Color.WHITE);
        annHeader.add(annTitle, BorderLayout.WEST);
        annCard.add(annHeader, BorderLayout.NORTH);

        annPanel = new JPanel(); annPanel.setLayout(new BoxLayout(annPanel, BoxLayout.Y_AXIS));
        annPanel.setBackground(UITheme.CARD_BG);
        JPanel annWrapper = new JPanel(new BorderLayout());
        annWrapper.setBackground(UITheme.CARD_BG);
        annWrapper.add(annPanel, BorderLayout.NORTH);
        JScrollPane annScroll = new JScrollPane(annWrapper,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        annScroll.setBorder(BorderFactory.createEmptyBorder());
        annScroll.getVerticalScrollBar().setUnitIncrement(14);
        annCard.add(annScroll, BorderLayout.CENTER);
        bottomRow.add(annCard);

        JPanel notifCard = new JPanel(new BorderLayout(0, 0));
        notifCard.setBackground(UITheme.CARD_BG);
        notifCard.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1, true));

        JPanel notifHeader = new JPanel(new BorderLayout());
        notifHeader.setBackground(UITheme.ACCENT);
        notifHeader.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JLabel notifTitle2 = new JLabel("Notifications");
        notifTitle2.setFont(UITheme.F_BUTTON); notifTitle2.setForeground(Color.WHITE);
        JButton markAll = new JButton("Mark All Read");
        markAll.setFont(UITheme.F_SMALL); markAll.setBackground(Color.WHITE);
        markAll.setForeground(UITheme.ACCENT); markAll.setFocusPainted(false);
        markAll.setOpaque(true); markAll.setBorderPainted(false);
        markAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        markAll.setPreferredSize(new Dimension(118, 26));
        markAll.addActionListener(e -> { ns.markAllRead(customer.getUserId()); refreshOverview(); });
        notifHeader.add(notifTitle2, BorderLayout.WEST); notifHeader.add(markAll, BorderLayout.EAST);
        notifCard.add(notifHeader, BorderLayout.NORTH);

        notifPanel = new JPanel(); notifPanel.setLayout(new BoxLayout(notifPanel, BoxLayout.Y_AXIS));
        notifPanel.setBackground(UITheme.CARD_BG);
        JPanel notifWrapper = new JPanel(new BorderLayout());
        notifWrapper.setBackground(UITheme.CARD_BG);
        notifWrapper.add(notifPanel, BorderLayout.NORTH);
        JScrollPane notifScroll = new JScrollPane(notifWrapper,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        notifScroll.setBorder(BorderFactory.createEmptyBorder());
        notifScroll.getVerticalScrollBar().setUnitIncrement(14);
        notifCard.add(notifScroll, BorderLayout.CENTER);
        bottomRow.add(notifCard);

        body.add(bottomRow, BorderLayout.CENTER);
        overviewTab.add(body, BorderLayout.CENTER);

        refreshOverview();
        return overviewTab;
    }

    private void refreshOverview() {
        List<Ticket> mine = ts.getTicketsByCustomer(customer.getUserId());
        long pending  = mine.stream().filter(t -> t.getStatus() == Ticket.Status.PENDING || t.getStatus() == Ticket.Status.IN_PROGRESS).count();
        long resolved = mine.stream().filter(t -> t.getStatus() == Ticket.Status.RESOLVED).count();
        statsRow.removeAll();
        statsRow.add(UITheme.statCard("Total Tickets",      String.valueOf(mine.size()), UITheme.INFO));
        statsRow.add(UITheme.statCard("Open / In-Progress", String.valueOf(pending),     UITheme.WARNING));
        statsRow.add(UITheme.statCard("Resolved",           String.valueOf(resolved),    UITheme.SUCCESS));
        statsRow.revalidate(); statsRow.repaint();

        annPanel.removeAll();
        List<Announcement> anns = ds.getActiveAnnouncements();
        if (anns.isEmpty()) {
            JLabel none = UITheme.smallLabel("  No announcements at this time.", UITheme.TEXT_MID);
            none.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
            annPanel.add(none);
        } else {
            for (int i = anns.size() - 1; i >= 0; i--) {
                Announcement a = anns.get(i);
                Color tc = a.typeColor();

                JPanel card = new JPanel(new BorderLayout(0, 6));
                card.setBackground(UITheme.CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                    BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 5, 0, 0, tc),
                        BorderFactory.createEmptyBorder(12, 14, 12, 14))));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

                JPanel titleRow = new JPanel(new BorderLayout(8, 0));
                titleRow.setOpaque(false);
                JLabel badge = new JLabel("  " + a.getType().name() + "  ");
                badge.setFont(UITheme.F_SMALL.deriveFont(Font.BOLD));
                badge.setForeground(Color.WHITE); badge.setOpaque(true); badge.setBackground(tc);
                badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                JLabel ttl = new JLabel(a.getTitle());
                ttl.setFont(UITheme.F_LABEL.deriveFont(Font.BOLD)); ttl.setForeground(UITheme.TEXT_DARK);
                JLabel date = UITheme.smallLabel(a.getFormattedDate(), UITheme.TEXT_LIGHT);
                JPanel badgeTtl = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); badgeTtl.setOpaque(false);
                badgeTtl.add(badge); badgeTtl.add(ttl);
                titleRow.add(badgeTtl, BorderLayout.WEST); titleRow.add(date, BorderLayout.EAST);

                JLabel bodyLbl = new JLabel("<html><body style='width:320px; color:#5a5a6e'>"
                    + a.getBody() + "</body></html>");
                bodyLbl.setFont(UITheme.F_SMALL);

                card.add(titleRow, BorderLayout.NORTH);
                card.add(bodyLbl, BorderLayout.CENTER);
                annPanel.add(card);
            }
        }
        annPanel.revalidate(); annPanel.repaint();

        notifPanel.removeAll();
        List<Notification> notifs = ns.getAll(customer.getUserId());
        if (notifs.isEmpty()) {
            JPanel emptyState = new JPanel(new BorderLayout());
            emptyState.setBackground(UITheme.CARD_BG);
            emptyState.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
            JLabel icon = new JLabel("No new notifications", JLabel.CENTER);
            icon.setFont(UITheme.F_LABEL.deriveFont(Font.ITALIC));
            icon.setForeground(UITheme.TEXT_LIGHT);
            emptyState.add(icon, BorderLayout.CENTER);
            emptyState.setAlignmentX(Component.LEFT_ALIGNMENT);
            emptyState.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            notifPanel.add(emptyState);
        } else {
            for (int i = notifs.size() - 1; i >= 0; i--) {
                Notification n = notifs.get(i);
                boolean unread = !n.isRead();

                JPanel card = new JPanel(new BorderLayout(12, 0));
                card.setBackground(unread ? new Color(240, 247, 255) : Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    new MatteBorder(0, 0, 1, 0, new Color(238, 241, 248)),
                    BorderFactory.createEmptyBorder(12, 14, 12, 14)));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

                JPanel indicator = new JPanel() {
                    @Override protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(unread ? UITheme.ACCENT : new Color(210, 218, 230));
                        g2.fillOval(2, (getHeight() - 10) / 2, 10, 10);
                    }
                };
                indicator.setOpaque(false);
                indicator.setPreferredSize(new Dimension(16, 16));

                JPanel centre = new JPanel(new BorderLayout(0, 3));
                centre.setOpaque(false);
                JLabel msg = new JLabel("<html><body style='width:320px'>"
                    + n.getMessage() + "</body></html>");
                msg.setFont(unread
                    ? new Font("Segoe UI", Font.BOLD,  13)
                    : new Font("Segoe UI", Font.PLAIN, 13));
                msg.setForeground(unread ? UITheme.TEXT_DARK : UITheme.TEXT_MID);

                JLabel time = new JLabel(n.getFormattedCreatedAt());
                time.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                time.setForeground(UITheme.TEXT_LIGHT);

                centre.add(msg,  BorderLayout.NORTH);
                centre.add(time, BorderLayout.CENTER);

                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
                right.setOpaque(false);
                if (unread) {
                    JLabel badge = new JLabel("NEW");
                    badge.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    badge.setForeground(Color.WHITE);
                    badge.setBackground(UITheme.ACCENT);
                    badge.setOpaque(true);
                    badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
                    right.add(badge);
                }

                card.add(indicator, BorderLayout.WEST);
                card.add(centre,    BorderLayout.CENTER);
                card.add(right,     BorderLayout.EAST);

                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                card.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                        n.setRead(true); refreshOverview();
                    }
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                        card.setBackground(new Color(230, 240, 255));
                    }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) {
                        card.setBackground(unread ? new Color(240, 247, 255) : Color.WHITE);
                    }
                });

                notifPanel.add(card);
            }
        }
        notifPanel.revalidate(); notifPanel.repaint();

        long unread = ns.unreadCount(customer.getUserId());
        notifBadge.setText("Notifications" + (unread > 0 ? " (" + unread + ")" : ""));
        notifBadge.setForeground(unread > 0 ? UITheme.WARNING_LIGHT : Color.WHITE);
    }

   
    private JTextField faqSearch;
    private JComboBox<String> faqCatFilter;
    private JPanel faqListPanel;

    private JPanel buildFAQTab() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

    
        JLabel title = new JLabel("Help Centre  &  FAQ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UITheme.PRIMARY);
        JLabel sub = UITheme.smallLabel("Find answers to common questions before submitting a support ticket.", UITheme.TEXT_MID);
        JPanel titlePanel = new JPanel(new BorderLayout(0, 4)); titlePanel.setOpaque(false);
        titlePanel.add(title, BorderLayout.NORTH); titlePanel.add(sub, BorderLayout.CENTER);
        p.add(titlePanel, BorderLayout.NORTH);

   
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(UITheme.CARD_BG);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        faqSearch = UITheme.textField(25);
        faqSearch.setToolTipText("Search questions or answers...");
        faqCatFilter = UITheme.comboBox(new String[]{"ALL","ACCOUNT","CARD","LOAN","TRANSACTION","SECURITY","GENERAL"});
        JButton btnSearch  = UITheme.primaryBtn("Search");
        JButton btnClear   = UITheme.secondaryBtn("Clear");
        JButton btnTicket  = UITheme.warningBtn("Still need help? Submit a Ticket ->");
        btnSearch.setPreferredSize(new Dimension(90, 30));
        btnClear.setPreferredSize(new Dimension(70, 30));
        btnTicket.setPreferredSize(new Dimension(240, 30));
        toolbar.add(UITheme.label("Search:")); toolbar.add(faqSearch);
        toolbar.add(UITheme.label("Category:")); toolbar.add(faqCatFilter);
        toolbar.add(btnSearch); toolbar.add(btnClear);
        toolbar.add(Box.createHorizontalStrut(20)); toolbar.add(btnTicket);
        p.add(toolbar, BorderLayout.CENTER);

    
        faqListPanel = new JPanel();
        faqListPanel.setLayout(new BoxLayout(faqListPanel, BoxLayout.Y_AXIS));
        faqListPanel.setBackground(UITheme.BACKGROUND);
        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.setBackground(UITheme.BACKGROUND);
        listWrapper.add(faqListPanel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(listWrapper, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        p.add(scroll, BorderLayout.SOUTH);
        p.setLayout(new BorderLayout(0, 10));
        p.add(titlePanel, BorderLayout.NORTH);
        JPanel mid = new JPanel(new BorderLayout(0, 8)); mid.setOpaque(false);
        mid.add(toolbar, BorderLayout.NORTH); mid.add(scroll, BorderLayout.CENTER);
        p.add(mid, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> refreshFAQ());
        btnClear.addActionListener(e -> { faqSearch.setText(""); faqCatFilter.setSelectedIndex(0); refreshFAQ(); });
        btnTicket.addActionListener(e -> tabs.setSelectedIndex(2));
        faqSearch.addActionListener(e -> refreshFAQ());
        faqCatFilter.addActionListener(e -> refreshFAQ());

        refreshFAQ();
        return p;
    }

    private void refreshFAQ() {
        String kw  = faqSearch.getText().trim().toLowerCase();
        String cat = (String) faqCatFilter.getSelectedItem();
        List<FAQItem> items = ds.getFaqItems().stream()
            .filter(FAQItem::isActive)
            .filter(f -> cat.equals("ALL") || f.getCategory().name().equals(cat))
            .filter(f -> kw.isEmpty() || f.getQuestion().toLowerCase().contains(kw)
                                      || f.getAnswer().toLowerCase().contains(kw))
            .collect(Collectors.toList());

        faqListPanel.removeAll();
        faqListPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        if (items.isEmpty()) {
            JLabel none = UITheme.label("No matching FAQ entries. Try a different search or submit a support ticket.");
            none.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));
            none.setForeground(UITheme.TEXT_MID);
            faqListPanel.add(none);
        } else {
            for (FAQItem faq : items) {
                faqListPanel.add(buildFAQCard(faq));
                faqListPanel.add(Box.createVerticalStrut(8));
            }
        }
        faqListPanel.revalidate(); faqListPanel.repaint();
    }

    private JPanel buildFAQCard(FAQItem faq) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

      
        JPanel top = new JPanel(new BorderLayout(8, 0)); top.setOpaque(false);
        JLabel catBadge = new JLabel("  " + faq.getCategory().name() + "  ");
        catBadge.setFont(UITheme.F_SMALL.deriveFont(Font.BOLD));
        catBadge.setForeground(Color.WHITE); catBadge.setOpaque(true);
        catBadge.setBackground(UITheme.PRIMARY); catBadge.setBorder(BorderFactory.createEmptyBorder(2,6,2,6));
        JLabel question = new JLabel("Q:  " + faq.getQuestion());
        question.setFont(UITheme.F_LABEL.deriveFont(Font.BOLD));
        question.setForeground(UITheme.TEXT_DARK);
        top.add(catBadge, BorderLayout.WEST); top.add(question, BorderLayout.CENTER);

        JLabel answer = new JLabel("<html><body style='width:600px;padding:4px 0'><b>A:</b>  " + faq.getAnswer() + "</body></html>");
        answer.setFont(UITheme.F_LABEL); answer.setForeground(UITheme.TEXT_MID);

  
        JButton btnHelp = new JButton("Helpful (" + faq.getHelpfulCount() + ")");
        btnHelp.setFont(UITheme.F_SMALL); btnHelp.setForeground(UITheme.SUCCESS);
        btnHelp.setBackground(UITheme.SUCCESS_LIGHT); btnHelp.setBorderPainted(false);
        btnHelp.setOpaque(true); btnHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnHelp.setPreferredSize(new Dimension(130, 26));
        btnHelp.addActionListener(e -> { faq.incrementHelpful(); btnHelp.setText("Helpful (" + faq.getHelpfulCount() + ")"); });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); bottom.setOpaque(false);
        bottom.add(btnHelp);
        card.add(top, BorderLayout.NORTH); card.add(answer, BorderLayout.CENTER); card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    
    private JComboBox<String> cbCategory, cbPriority;
    private JTextArea taDesc;
    private JLabel lblSubmitErr;

    private JPanel buildSubmitTab() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(UITheme.BACKGROUND);
        JPanel form = UITheme.card(32);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(620, 500));

        JLabel title = UITheme.sectionTitle("Submit a Support Ticket");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hint = UITheme.smallLabel("Tip: Check the Help & FAQ tab first — your question may already be answered!", UITheme.ACCENT);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title); form.add(hint); form.add(Box.createVerticalStrut(16));

        cbCategory = UITheme.comboBox(new String[]{"ACCOUNT","CARD","LOAN","TRANSACTION","GENERAL"});
        cbCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        form.add(formRow("Category *", cbCategory)); form.add(Box.createVerticalStrut(12));

        cbPriority = UITheme.comboBox(new String[]{"LOW","MEDIUM","HIGH"});
        cbPriority.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        form.add(formRow("Priority *", cbPriority)); form.add(Box.createVerticalStrut(12));

        JLabel descLbl = UITheme.label("Description * (10-1000 characters)");
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(descLbl); form.add(Box.createVerticalStrut(4));
        taDesc = UITheme.textArea(8, 40);
        JScrollPane descScroll = UITheme.scrollPane(taDesc);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(descScroll); form.add(Box.createVerticalStrut(10));

        lblSubmitErr = new JLabel(" ");
        lblSubmitErr.setFont(UITheme.F_SMALL); lblSubmitErr.setForeground(UITheme.DANGER);
        lblSubmitErr.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblSubmitErr); form.add(Box.createVerticalStrut(12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false); btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnClear = UITheme.secondaryBtn("Clear");
        JButton btnSubmit = UITheme.primaryBtn("Submit Ticket");
        btnRow.add(btnClear); btnRow.add(btnSubmit);
        form.add(btnRow);
        outer.add(form);

        btnSubmit.addActionListener(e -> doSubmitTicket());
        btnClear.addActionListener(e -> { taDesc.setText(""); lblSubmitErr.setText(" "); });
        return outer;
    }

    private JPanel formRow(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false); p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel l = UITheme.label(label); l.setPreferredSize(new Dimension(160, 30));
        p.add(l, BorderLayout.WEST); p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private void doSubmitTicket() {
        String desc = taDesc.getText().trim();
        String err = Validator.validateTicketDescription(desc);
        if (err != null) { lblSubmitErr.setText(err); return; }
        err = ts.createTicket(customer.getUserId(), customer.getFullName(),
            (String) cbCategory.getSelectedItem(), desc, (String) cbPriority.getSelectedItem());
        if (err != null) { lblSubmitErr.setText(err); return; }
        MainFrame.showInfo(this, "Ticket submitted successfully!");
        taDesc.setText(""); lblSubmitErr.setText(" ");
        tabs.setSelectedIndex(3); refreshTicketsTab();
    }

    
    private DefaultTableModel ticketModel;
    private JTable ticketTable;
    private JTextField tfSearch;
    private List<Ticket> currentTickets;

    
    private JLabel   lblDetailId, lblDetailStatus, lblDetailCat, lblDetailPriority, lblDetailDate, lblDetailAssigned;
    private JTextArea taReply;
    private JPanel   timelinePanel;
    private JTabbedPane detailTabs;

    private JPanel buildMyTicketsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        tfSearch = UITheme.textField(20);
        JButton btnSearch  = UITheme.primaryBtn("Search by ID");
        JButton btnRefresh = UITheme.secondaryBtn("Refresh");
        btnSearch.setPreferredSize(new Dimension(110, 30));
        btnRefresh.setPreferredSize(new Dimension(90, 30));
        toolbar.add(tfSearch); toolbar.add(btnSearch); toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

       
        String[] cols = {"Ticket ID","Category","Priority","Status","Submitted","Last Updated"};
        ticketModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        ticketTable = new JTable(ticketModel);
        styleTable(ticketTable);
        ticketTable.getColumnModel().getColumn(3).setCellRenderer(statusRenderer());
        ticketTable.getColumnModel().getColumn(2).setCellRenderer(priorityRenderer());
        ticketTable.getColumnModel().getColumn(0).setPreferredWidth(160);

        JPanel detailContainer = buildDetailContainer();
        detailContainer.setPreferredSize(new Dimension(0, 380));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            UITheme.scrollPane(ticketTable), detailContainer);
        split.setResizeWeight(0.38); split.setDividerSize(6);
        panel.add(split, BorderLayout.CENTER);

        btnSearch.addActionListener(e -> searchById());
        btnRefresh.addActionListener(e -> refreshTicketsTab());
        tfSearch.addActionListener(e -> searchById());
        ticketTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadTicketDetail();
        });

        refreshTicketsTab();
        return panel;
    }

    
    private JScrollPane chatScroll;
    private JPanel      chatMessages;
    private JPanel      closeResolveBanner;

    private JPanel buildDetailContainer() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

       
        JPanel infoBar = new JPanel(new BorderLayout(0, 0));
        infoBar.setBackground(Color.WHITE);
        infoBar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 1, 0, new Color(225, 229, 238)),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));

       
        JPanel fields = new JPanel(new GridLayout(1, 6, 0, 0));
        fields.setBackground(Color.WHITE);

        lblDetailId       = infoBarValue("-");
        lblDetailCat      = infoBarValue("-");
        lblDetailPriority = infoBarValue("-");
        lblDetailStatus   = infoBarValue("-");
        lblDetailDate     = infoBarValue("-");
        lblDetailAssigned = infoBarValue("-");

        fields.add(infoBarCell("Ticket ID",   lblDetailId,       false));
        fields.add(infoBarCell("Category",    lblDetailCat,      true));
        fields.add(infoBarCell("Priority",    lblDetailPriority, false));
        fields.add(infoBarCell("Status",      lblDetailStatus,   true));
        fields.add(infoBarCell("Submitted",   lblDetailDate,     false));
        fields.add(infoBarCell("Assigned To", lblDetailAssigned, true));

        infoBar.add(fields, BorderLayout.CENTER);
        p.add(infoBar, BorderLayout.NORTH);

       
        detailTabs = new JTabbedPane();
        detailTabs.setFont(UITheme.F_BUTTON);
        detailTabs.addTab("  Chat  ",     buildChatTab());
        detailTabs.addTab("  Timeline  ", buildTimelineTab());
        p.add(detailTabs, BorderLayout.CENTER);
        return p;
    }

    private JLabel infoBarValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(UITheme.TEXT_DARK);
        return l;
    }

   
    private JPanel infoBarCell(String labelText, JLabel valueLabel, boolean shaded) {
        JPanel cell = new JPanel(new BorderLayout(0, 3));
        cell.setBackground(shaded ? new Color(249, 250, 253) : Color.WHITE);
        cell.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 0, 1, new Color(228, 232, 240)),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JLabel lbl = new JLabel(labelText.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lbl.setForeground(new Color(160, 168, 185));
        lbl.setPreferredSize(new Dimension(0, 14));

        cell.add(lbl,        BorderLayout.NORTH);
        cell.add(valueLabel, BorderLayout.CENTER);
        return cell;
    }

  
    private JLabel infoHeaderValue(String t) { return infoBarValue(t); }
    private JPanel infoHeaderCell(String l, JLabel v, Color c) { return infoBarCell(l, v, false); }

    
    private JPanel buildChatTab() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(new Color(235, 238, 245));

       
        chatMessages = new JPanel();
        chatMessages.setLayout(new BoxLayout(chatMessages, BoxLayout.Y_AXIS));
        chatMessages.setBackground(new Color(235, 238, 245));
        chatMessages.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));

        JPanel chatWrapper = new JPanel(new BorderLayout());
        chatWrapper.setBackground(new Color(235, 238, 245));
        chatWrapper.add(chatMessages, BorderLayout.NORTH);

        chatScroll = new JScrollPane(chatWrapper,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScroll.setBorder(BorderFactory.createEmptyBorder());
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        p.add(chatScroll, BorderLayout.CENTER);

       
        closeResolveBanner = new JPanel(new BorderLayout(12, 0));
        closeResolveBanner.setBackground(new Color(232, 248, 236));
        closeResolveBanner.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(100, 200, 120)),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)));
        closeResolveBanner.setVisible(false);

        JLabel bannerTxt = new JLabel("Your ticket has been RESOLVED by staff. You may close it and leave a rating.");
        bannerTxt.setFont(UITheme.F_LABEL.deriveFont(Font.BOLD));
        bannerTxt.setForeground(new Color(30, 120, 50));

        JButton btnCloseDirect = new JButton("Close & Rate Ticket");
        btnCloseDirect.setFont(UITheme.F_BUTTON);
        btnCloseDirect.setBackground(UITheme.SUCCESS);
        btnCloseDirect.setForeground(Color.WHITE);
        btnCloseDirect.setFocusPainted(false); btnCloseDirect.setOpaque(true); btnCloseDirect.setBorderPainted(false);
        btnCloseDirect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCloseDirect.setPreferredSize(new Dimension(200, 36));
        btnCloseDirect.addActionListener(e -> showCloseRateDialog());

        closeResolveBanner.add(bannerTxt, BorderLayout.CENTER);
        closeResolveBanner.add(btnCloseDirect, BorderLayout.EAST);

       
        JPanel inputBar = new JPanel(new BorderLayout(0, 0));
        inputBar.setBackground(Color.WHITE);
        inputBar.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 225, 235)));

      
        JLabel inputHint = new JLabel("  Reply to staff  (available when ticket is In-Progress or Resolved)");
        inputHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        inputHint.setForeground(new Color(155, 165, 185));
        inputHint.setBackground(new Color(248, 250, 254));
        inputHint.setOpaque(true);
        inputHint.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(220, 225, 235)),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        inputBar.add(inputHint, BorderLayout.NORTH);

        
        JPanel inputRow = new JPanel(new BorderLayout(0, 0));
        inputRow.setBackground(Color.WHITE);

        taReply = UITheme.textArea(3, 30);
        taReply.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taReply.setBackground(Color.WHITE);
        taReply.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        taReply.setLineWrap(true); taReply.setWrapStyleWord(true);
        
        inputRow.add(taReply, BorderLayout.CENTER);

        
        JPanel sendPanel = new JPanel(new BorderLayout());
        sendPanel.setBackground(new Color(13, 71, 161));  
        sendPanel.setPreferredSize(new Dimension(88, 0));  
        sendPanel.setBorder(new MatteBorder(0, 1, 0, 0, new Color(10, 55, 130)));

        JButton btnSend = new JButton("Send");
        btnSend.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSend.setBackground(new Color(13, 71, 161));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false); btnSend.setOpaque(true); btnSend.setBorderPainted(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setHorizontalAlignment(SwingConstants.CENTER);
        btnSend.addActionListener(e -> doSendReply());

    
        btnSend.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                btnSend.setBackground(new Color(25, 95, 190));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btnSend.setBackground(new Color(13, 71, 161));
            }
        });

        sendPanel.add(btnSend, BorderLayout.CENTER);
        inputRow.add(sendPanel, BorderLayout.EAST);

        inputBar.add(inputRow, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout()); south.setOpaque(false);
        south.add(closeResolveBanner, BorderLayout.NORTH);
        south.add(inputBar, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

  
    private JPanel buildTimelineTab() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BACKGROUND);
        timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(UITheme.BACKGROUND);
        JPanel tw = new JPanel(new BorderLayout());
        tw.setBackground(UITheme.BACKGROUND);
        tw.add(timelinePanel, BorderLayout.NORTH);
        JScrollPane ts2 = UITheme.scrollPane(tw);
        ts2.getVerticalScrollBar().setUnitIncrement(14);
        ts2.setBorder(BorderFactory.createEmptyBorder());
        p.add(ts2, BorderLayout.CENTER);
        return p;
    }

   
    private void showCloseRateDialog() {
        int row = ticketTable.getSelectedRow();
        if (row < 0) return;
        Ticket t = currentTickets.get(ticketTable.convertRowIndexToModel(row));
        if (t.getStatus() != Ticket.Status.RESOLVED) {
            MainFrame.showError(this, "Only RESOLVED tickets can be closed."); return;
        }

        JPanel dlgPanel = new JPanel();
        dlgPanel.setLayout(new BoxLayout(dlgPanel, BoxLayout.Y_AXIS));
        dlgPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel rateLbl = UITheme.label("Rate your experience (1 = Poor, 5 = Excellent):");
        rateLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JComboBox<Integer> ratingBox = new JComboBox<>(new Integer[]{1,2,3,4,5});
        ratingBox.setSelectedItem(5); ratingBox.setFont(UITheme.F_LABEL);
        ratingBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratingBox.setMaximumSize(new Dimension(200, 34));

        JLabel fbLbl = UITheme.label("Feedback (optional):");
        fbLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextArea fbArea = UITheme.textArea(4, 30);
        JScrollPane fbScroll = UITheme.scrollPane(fbArea);
        fbScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        fbScroll.setPreferredSize(new Dimension(400, 100));
        fbScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        dlgPanel.add(rateLbl); dlgPanel.add(Box.createVerticalStrut(6));
        dlgPanel.add(ratingBox); dlgPanel.add(Box.createVerticalStrut(12));
        dlgPanel.add(fbLbl); dlgPanel.add(Box.createVerticalStrut(6));
        dlgPanel.add(fbScroll);

        int result = JOptionPane.showConfirmDialog(this, dlgPanel,
            "Close Ticket: " + t.getTicketId(), JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        int rating   = (Integer) ratingBox.getSelectedItem();
        String fbTxt = fbArea.getText().trim();
        String err   = ts.closeTicket(t.getTicketId(), customer.getUserId(), rating, fbTxt);
        if (err != null) { MainFrame.showError(this, err); return; }

        MainFrame.showInfo(this, "Ticket closed. Thank you for your feedback!");
        refreshTicketsTab();
    }

    private void loadTicketDetail() {
        int row = ticketTable.getSelectedRow();
        if (row < 0 || currentTickets == null) return;
        Ticket t = currentTickets.get(ticketTable.convertRowIndexToModel(row));

   
        lblDetailId.setText(t.getTicketId());
        lblDetailCat.setText(t.getCategory().name());
        lblDetailPriority.setText(t.getPriority().name());
        lblDetailPriority.setForeground(UITheme.priorityColor(t.getPriority().name()));
        lblDetailStatus.setText(t.getStatus().name());
        lblDetailStatus.setForeground(UITheme.statusColor(t.getStatus().name()));
        lblDetailStatus.setFont(UITheme.F_LABEL.deriveFont(Font.BOLD));
        lblDetailDate.setText(t.getFormattedCreatedAt());
        lblDetailAssigned.setText(t.getAssignedStaffName() != null
            ? t.getAssignedStaffName() : "Not yet assigned");

   
        chatMessages.removeAll();

      
        chatMessages.add(chatBubble(
            customer.getFullName() + "  (You)", t.getDescription(),
            t.getFormattedCreatedAt(), true));
        chatMessages.add(Box.createVerticalStrut(6));

     
        boolean anyStaffMessage = false;
        for (TicketHistory h : t.getHistory()) {
            String action = h.getAction();
            String notes  = h.getNotes();
            if (notes == null || notes.isEmpty()) continue;

            if (action.startsWith("Response Added") || action.startsWith("Response")) {
                String staffName = h.getPerformedByName() != null
                    ? h.getPerformedByName() : "Support Staff";
                chatMessages.add(chatBubble(
                    staffName + "  (Staff)", notes,
                    h.getFormattedTimestamp(), false));
                chatMessages.add(Box.createVerticalStrut(6));
                anyStaffMessage = true;

            } else if (action.equals("Customer Replied")) {
                chatMessages.add(chatBubble(
                    customer.getFullName() + "  (You)", notes,
                    h.getFormattedTimestamp(), true));
                chatMessages.add(Box.createVerticalStrut(6));
            }
        }

        if (!anyStaffMessage) {
            JLabel wait = new JLabel("  Waiting for a staff response...");
            wait.setFont(UITheme.F_LABEL.deriveFont(Font.ITALIC));
            wait.setForeground(UITheme.TEXT_LIGHT);
            wait.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            wait.setAlignmentX(Component.LEFT_ALIGNMENT);
            chatMessages.add(wait);
        }

        chatMessages.revalidate(); chatMessages.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });

        boolean resolved = t.getStatus() == Ticket.Status.RESOLVED;
        closeResolveBanner.setVisible(resolved);


        buildTimeline(t);

        detailTabs.setSelectedIndex(0);
    }

    private JPanel chatBubble(String sender, String message, String time, boolean isCustomer) {

        Color bubbleBg   = isCustomer ? new Color(0, 95, 184)   : Color.WHITE;
        Color bubbleFg   = isCustomer ? Color.WHITE              : UITheme.TEXT_DARK;
        Color metaFg     = isCustomer ? new Color(180, 215, 255) : UITheme.TEXT_LIGHT;
        Color senderFg   = isCustomer ? new Color(210, 230, 255) : UITheme.TEXT_MID;

        JPanel bubble = new JPanel(new BorderLayout(0, 4));
        bubble.setBackground(bubbleBg);
        bubble.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(isCustomer ? new Color(0, 75, 160) : new Color(220, 225, 235), 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        bubble.setOpaque(true);

        JLabel senderLbl = new JLabel(sender);
        senderLbl.setFont(UITheme.F_SMALL.deriveFont(Font.BOLD));
        senderLbl.setForeground(senderFg);

        JLabel msgLbl = new JLabel("<html><body style='width:360px'>" + message + "</body></html>");
        msgLbl.setFont(UITheme.F_LABEL); msgLbl.setForeground(bubbleFg);

        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(UITheme.F_SMALL); timeLbl.setForeground(metaFg);

        bubble.add(senderLbl, BorderLayout.NORTH);
        bubble.add(msgLbl,    BorderLayout.CENTER);
        bubble.add(timeLbl,   BorderLayout.SOUTH);

        int maxW = 520;
        JPanel outer = new JPanel(new FlowLayout(
            isCustomer ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        outer.setOpaque(false);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.setMaximumSize(new Dimension(maxW, Integer.MAX_VALUE));
        bubble.setPreferredSize(new Dimension(maxW, bubble.getPreferredSize().height));
        outer.add(bubble);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return outer;
    }

    private void buildTimeline(Ticket t) {
        timelinePanel.removeAll();
        timelinePanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        List<TicketHistory> events = t.getHistory();
        for (int i = 0; i < events.size(); i++) {
            TicketHistory h = events.get(i);
            boolean isLast = (i == events.size() - 1);

            JPanel entry = new JPanel(new BorderLayout(12, 0));
            entry.setOpaque(false);
            entry.setAlignmentX(Component.LEFT_ALIGNMENT);
            entry.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            JPanel dotLine = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color dotColor = timelineDotColor(h.getAction());
                    int cx = getWidth() / 2;                
                    if (!isLast) {
                        g2.setColor(new Color(200, 210, 225));
                        g2.setStroke(new BasicStroke(2));
                        g2.drawLine(cx, 22, cx, getHeight());
                    }

                    g2.setColor(dotColor);
                    g2.fillOval(cx - 8, 6, 16, 16);
                    g2.setColor(Color.WHITE);
                    g2.fillOval(cx - 5, 9, 10, 10);
                    g2.setColor(dotColor);
                    g2.fillOval(cx - 4, 10, 8, 8);
                }
            };
            dotLine.setOpaque(false);
            dotLine.setPreferredSize(new Dimension(28, isLast ? 40 : 70));

            JPanel content = new JPanel(new BorderLayout(0, 2));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));

            JLabel action = new JLabel(h.getAction());
            action.setFont(UITheme.F_LABEL.deriveFont(Font.BOLD));
            action.setForeground(timelineDotColor(h.getAction()));

            JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            metaRow.setOpaque(false);
            metaRow.add(UITheme.smallLabel(h.getFormattedTimestamp(), UITheme.TEXT_LIGHT));
            metaRow.add(UITheme.smallLabel("by " + h.getPerformedByName(), UITheme.TEXT_MID));

            content.add(action, BorderLayout.NORTH);
            content.add(metaRow, BorderLayout.CENTER);
            if (!h.getNotes().isEmpty()) {
                JLabel notes = UITheme.smallLabel("  -> " + h.getNotes(), UITheme.TEXT_MID);
                content.add(notes, BorderLayout.SOUTH);
            }

            entry.add(dotLine, BorderLayout.WEST);
            entry.add(content, BorderLayout.CENTER);
            timelinePanel.add(entry);
        }
        timelinePanel.revalidate(); timelinePanel.repaint();
    }

    private Color timelineDotColor(String action) {
        if (action.contains("Created"))    return UITheme.INFO;
        if (action.contains("RESOLVED") || action.contains("Resolved")) return UITheme.SUCCESS;
        if (action.contains("Closed"))     return UITheme.NEUTRAL;
        if (action.contains("Assigned"))   return UITheme.ACCENT;
        if (action.contains("Reopened"))   return UITheme.WARNING;
        if (action.contains("Response"))   return UITheme.SUCCESS;
        return UITheme.PRIMARY;
    }

    private void doSendReply() {
        int row = ticketTable.getSelectedRow();
        if (row < 0) { MainFrame.showError(this, "Please select a ticket first."); return; }
        Ticket t = currentTickets.get(ticketTable.convertRowIndexToModel(row));
        if (t.getStatus() == Ticket.Status.PENDING) {
            MainFrame.showError(this, "You can only reply once a staff member has responded."); return;
        }
        if (t.getStatus() == Ticket.Status.CLOSED) {
            MainFrame.showError(this, "This ticket is already closed."); return;
        }
        String msg = taReply.getText().trim();
        if (msg.isEmpty()) { MainFrame.showError(this, "Reply cannot be empty."); return; }
        if (msg.length() < 5) { MainFrame.showError(this, "Reply must be at least 5 characters."); return; }

        String rid = ds.nextReplyId();
        CustomerReply reply = new CustomerReply(rid, t.getTicketId(),
            customer.getUserId(), customer.getFullName(), msg);
        t.addCustomerReply(reply);
        t.addHistory(new TicketHistory("Customer Replied", customer.getUserId(), customer.getFullName(), msg));

        ns.notify(t.getAssignedStaffId() != null ? t.getAssignedStaffId() : "U001",
            "Customer " + customer.getFullName() + " replied on ticket " + t.getTicketId(), t.getTicketId());

        taReply.setText("");
        loadTicketDetail();
    }

    private void doCloseTicket() { showCloseRateDialog(); }

    private void searchById() {
        String q = tfSearch.getText().trim();
        refreshTicketsTab(q.isEmpty() ? null : q);
    }

    private void refreshTicketsTab() { refreshTicketsTab(null); }
    private void refreshTicketsTab(String searchId) {
        currentTickets = ts.getTicketsByCustomer(customer.getUserId());
        if (searchId != null) {
            final String q = searchId.toLowerCase();
            currentTickets = currentTickets.stream().filter(t -> t.getTicketId().toLowerCase().contains(q)).collect(Collectors.toList());
        }
        ticketModel.setRowCount(0);
        for (Ticket t : currentTickets)
            ticketModel.addRow(new Object[]{t.getTicketId(), t.getCategory().name(), t.getPriority().name(), t.getStatus().name(), t.getFormattedCreatedAt(), t.getFormattedUpdatedAt()});
        // Clear detail area
        lblDetailId.setText("-");
        if (chatMessages != null) { chatMessages.removeAll(); chatMessages.revalidate(); }
        if (closeResolveBanner != null) closeResolveBanner.setVisible(false);
    }

    private JTextField tfPName, tfPEmail, tfPPhone, tfPAddress;
    private JPasswordField tfCurrPwd, tfNewPwd, tfConfPwd;
    private JLabel lblProfileErr, lblPwdErr;

    private JScrollPane buildProfileTab() {
        JPanel outer = new JPanel();
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBackground(UITheme.BACKGROUND);
        outer.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel bankCard = new JPanel(new BorderLayout(0, 10)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(13, 71, 161),
                    getWidth(), getHeight(), new Color(30, 136, 229));
                g2.setPaint(gp); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        bankCard.setOpaque(false);
        bankCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        bankCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        bankCard.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        JLabel bankName = new JLabel("SecureBank");
        bankName.setFont(new Font("Segoe UI", Font.BOLD, 15)); bankName.setForeground(Color.WHITE);
        JLabel accTypeLbl = new JLabel(customer.getAccountType() + " ACCOUNT");
        accTypeLbl.setFont(new Font("Segoe UI", Font.BOLD, 10)); accTypeLbl.setForeground(new Color(180, 220, 255));
        JPanel topRow = new JPanel(new BorderLayout()); topRow.setOpaque(false);
        topRow.add(bankName, BorderLayout.WEST); topRow.add(accTypeLbl, BorderLayout.EAST);

        JLabel cardNumLbl = new JLabel(customer.getCardNumber());
        cardNumLbl.setFont(new Font("Courier New", Font.BOLD, 16)); cardNumLbl.setForeground(Color.WHITE);

        JLabel holderLbl = new JLabel(customer.getFullName().toUpperCase());
        holderLbl.setFont(new Font("Segoe UI", Font.BOLD, 10)); holderLbl.setForeground(new Color(200, 225, 255));
        JLabel accNumLbl = new JLabel(customer.getAccountNumber());
        accNumLbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); accNumLbl.setForeground(new Color(210, 235, 255));
        JPanel botRow = new JPanel(new BorderLayout()); botRow.setOpaque(false);
        botRow.add(holderLbl, BorderLayout.WEST); botRow.add(accNumLbl, BorderLayout.EAST);

        bankCard.add(topRow,    BorderLayout.NORTH);
        bankCard.add(cardNumLbl,BorderLayout.CENTER);
        bankCard.add(botRow,    BorderLayout.SOUTH);

        JPanel balPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        balPanel.setOpaque(false); balPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        balPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JPanel balCard = new JPanel(new BorderLayout(0, 4));
        balCard.setBackground(Color.WHITE);
        balCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 225, 235), 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel balLbl = new JLabel("CURRENT BALANCE");
        balLbl.setFont(new Font("Segoe UI", Font.BOLD, 10)); balLbl.setForeground(new Color(160, 165, 180));
        JLabel balVal = new JLabel(customer.getFormattedBalance());
        balVal.setFont(new Font("Segoe UI", Font.BOLD, 22)); balVal.setForeground(new Color(46, 125, 50));
        balCard.add(balLbl, BorderLayout.NORTH); balCard.add(balVal, BorderLayout.CENTER);

        JPanel accCard = new JPanel(new BorderLayout(0, 4));
        accCard.setBackground(Color.WHITE);
        accCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 225, 235), 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel accLbl = new JLabel("MEMBER SINCE");
        accLbl.setFont(new Font("Segoe UI", Font.BOLD, 10)); accLbl.setForeground(new Color(160, 165, 180));
        JLabel accVal = new JLabel(customer.getFormattedCreatedAt());
        accVal.setFont(new Font("Segoe UI", Font.BOLD, 14)); accVal.setForeground(new Color(30, 30, 45));
        accCard.add(accLbl, BorderLayout.NORTH); accCard.add(accVal, BorderLayout.CENTER);

        balPanel.add(balCard); balPanel.add(accCard);

        JPanel infoCard = UITheme.card(20);
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sec1 = UITheme.sectionTitle("Personal Information"); sec1.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCard.add(sec1);

        tfPName = UITheme.textField(25); tfPName.setText(customer.getFullName());
        tfPEmail = UITheme.textField(25); tfPEmail.setText(customer.getEmail());
        tfPPhone = UITheme.textField(25); tfPPhone.setText(customer.getPhone());
        tfPAddress = UITheme.textField(25); tfPAddress.setText(customer.getAddress());

        for (Object[] r : new Object[][]{{"Full Name *",tfPName},{"Email *",tfPEmail},{"Phone *",tfPPhone},{"Address *",tfPAddress}}) {
            JPanel pr = UITheme.formRow((String)r[0],(JComponent)r[1]);
            pr.setAlignmentX(Component.LEFT_ALIGNMENT); pr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            infoCard.add(Box.createVerticalStrut(8)); infoCard.add(pr);
        }
        lblProfileErr = new JLabel(" "); lblProfileErr.setFont(UITheme.F_SMALL); lblProfileErr.setForeground(UITheme.DANGER); lblProfileErr.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnSave = UITheme.primaryBtn("Save Changes"); btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> { String err = us.updateCustomerProfile(customer.getUserId(), tfPName.getText().trim(), tfPEmail.getText().trim(), tfPPhone.getText().trim(), tfPAddress.getText().trim()); if (err != null) { lblProfileErr.setText(err); return; } lblProfileErr.setText(" "); MainFrame.showInfo(this, "Profile updated."); });
        infoCard.add(Box.createVerticalStrut(8)); infoCard.add(lblProfileErr); infoCard.add(Box.createVerticalStrut(6)); infoCard.add(btnSave);

        JPanel pwdCard = UITheme.card(20);
        pwdCard.setLayout(new BoxLayout(pwdCard, BoxLayout.Y_AXIS));
        pwdCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sec2 = UITheme.sectionTitle("Change Password"); sec2.setAlignmentX(Component.LEFT_ALIGNMENT);
        pwdCard.add(sec2);
        tfCurrPwd = UITheme.passwordField(25); tfNewPwd = UITheme.passwordField(25); tfConfPwd = UITheme.passwordField(25);
        for (Object[] r : new Object[][]{{"Current Password *",tfCurrPwd},{"New Password *",tfNewPwd},{"Confirm New *",tfConfPwd}}) {
            JPanel pr = UITheme.formRow((String)r[0],(JComponent)r[1]);
            pr.setAlignmentX(Component.LEFT_ALIGNMENT); pr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            pwdCard.add(Box.createVerticalStrut(8)); pwdCard.add(pr);
        }
        lblPwdErr = new JLabel(" "); lblPwdErr.setFont(UITheme.F_SMALL); lblPwdErr.setForeground(UITheme.DANGER); lblPwdErr.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnChgPwd = UITheme.primaryBtn("Change Password"); btnChgPwd.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnChgPwd.addActionListener(e -> { String err = as.changePassword(customer.getUserId(), new String(tfCurrPwd.getPassword()), new String(tfNewPwd.getPassword()), new String(tfConfPwd.getPassword())); if (err != null) { lblPwdErr.setText(err); return; } lblPwdErr.setText(" "); tfCurrPwd.setText(""); tfNewPwd.setText(""); tfConfPwd.setText(""); MainFrame.showInfo(this, "Password changed."); });
        pwdCard.add(Box.createVerticalStrut(8)); pwdCard.add(lblPwdErr); pwdCard.add(Box.createVerticalStrut(6)); pwdCard.add(btnChgPwd);

        outer.add(bankCard); outer.add(Box.createVerticalStrut(14));
        outer.add(balPanel); outer.add(Box.createVerticalStrut(14));
        outer.add(infoCard); outer.add(Box.createVerticalStrut(16)); outer.add(pwdCard);
        return new JScrollPane(outer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private void styleTable(JTable t) {
        UITheme.styleTable(t);
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl,Object val,boolean sel,boolean foc,int row,int col) {
                JLabel l=(JLabel)super.getTableCellRendererComponent(tbl,val,sel,foc,row,col);
                l.setBackground(UITheme.PRIMARY); l.setForeground(Color.WHITE); l.setFont(UITheme.F_TABLE_H);
                l.setBorder(BorderFactory.createEmptyBorder(6,10,6,10)); l.setOpaque(true); return l;
            }
        });
        h.setBackground(UITheme.PRIMARY); h.setForeground(Color.WHITE); h.setOpaque(true);
    }
    private TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col) {
                JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if(!sel&&v!=null){l.setForeground(UITheme.statusColor(v.toString()));l.setFont(UITheme.F_TABLE.deriveFont(Font.BOLD));}
                l.setBorder(BorderFactory.createEmptyBorder(0,8,0,8)); return l;
            }
        };
    }
    private TableCellRenderer priorityRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean sel,boolean foc,int row,int col) {
                JLabel l=(JLabel)super.getTableCellRendererComponent(t,v,sel,foc,row,col);
                if(!sel&&v!=null) l.setForeground(UITheme.priorityColor(v.toString()));
                l.setBorder(BorderFactory.createEmptyBorder(0,8,0,8)); return l;
            }
        };
    }
}
