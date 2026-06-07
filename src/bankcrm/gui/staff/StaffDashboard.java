package bankcrm.gui.staff;

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


public class StaffDashboard extends JPanel {

    private final Staff         staff;
    private final TicketService ts = TicketService.getInstance();
    private final DataStore     ds = DataStore.getInstance();

    private DefaultTableModel tableModel;
    private JTable            table;
    private List<Ticket>      displayed;
    private JTextField        tfSearch;
    private JComboBox<String> cbStatus, cbPriority, cbCategory, cbSort;

    private JLabel wId, wCustomer, wCategory, wPriority, wStatus, wAssigned, wDate, wCSAT;
    private JPanel workspacePanel;   // the full right side
    private JTabbedPane workspaceTabs;

    private JComboBox<CannedResponse> cbCanned;
    private JComboBox<String>         cbNewStatus;
    private JTextArea                 taResponse, taRemarks;

  
    private JPanel    msgPanel;
    private JTextArea taMsgInput;

    private JTextArea taHistory;

    public StaffDashboard(Staff staff) {
        this.staff = staff;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        buildUI();
    }

  
    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        JLabel title = new JLabel("  SecureBank CRM  -  Support Staff Portal");
        title.setFont(UITheme.F_TITLE); title.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        JLabel userLbl = new JLabel(staff.getFullName() + "  |  " + staff.getDepartment());
        userLbl.setFont(UITheme.F_LABEL); userLbl.setForeground(new Color(200, 220, 255));
        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UITheme.F_BUTTON); btnLogout.setBackground(new Color(210, 60, 50));
        btnLogout.setForeground(Color.WHITE); btnLogout.setFocusPainted(false);
        btnLogout.setOpaque(true); btnLogout.setBorderPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(90, 32));
        btnLogout.addActionListener(e -> { if (MainFrame.confirm(this, "Logout?")) MainFrame.getInstance().logout(); });
        right.add(userLbl); right.add(btnLogout);
        header.add(title, BorderLayout.WEST); header.add(right, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.F_BUTTON);
        tabs.addTab("  Dashboard",        buildDashboardTab());
        tabs.addTab("  Manage Tickets",   buildTicketsTab());
        tabs.addTab("  Customers",        new bankcrm.gui.customer.CustomerManagementPanel(
                                              bankcrm.gui.customer.CustomerManagementPanel.Mode.STAFF_VIEW, staff));
        add(tabs, BorderLayout.CENTER);

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 1)
                refreshTable(null, "ALL", "ALL", "ALL", "Date Desc");
        });
    }

   
    private JPanel buildDashboardTab() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JLabel welcome = new JLabel("Welcome, " + staff.getFullName());
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcome.setForeground(UITheme.PRIMARY);
        JLabel sub = UITheme.smallLabel(staff.getDepartment() + "  |  Staff ID: " + staff.getStaffId(), UITheme.TEXT_MID);
        JPanel titleArea = new JPanel(new BorderLayout(0, 4)); titleArea.setOpaque(false);
        titleArea.add(welcome, BorderLayout.NORTH); titleArea.add(sub, BorderLayout.CENTER);
        p.add(titleArea, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0));
        stats.setOpaque(false);
        long total    = ts.getAllTickets().size();
        long pending  = ts.countByStatus(Ticket.Status.PENDING);
        long inProg   = ts.countByStatus(Ticket.Status.IN_PROGRESS);
        long resolved = ts.countByStatus(Ticket.Status.RESOLVED);
        stats.add(UITheme.statCard("All Tickets",  String.valueOf(total),    UITheme.INFO));
        stats.add(UITheme.statCard("Pending",      String.valueOf(pending),  UITheme.WARNING));
        stats.add(UITheme.statCard("In Progress",  String.valueOf(inProg),   UITheme.ACCENT));
        stats.add(UITheme.statCard("Resolved",     String.valueOf(resolved), UITheme.SUCCESS));
        p.add(stats, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 16, 0));
        bottom.setOpaque(false);

        JPanel perf = UITheme.card(18);
        perf.setLayout(new BoxLayout(perf, BoxLayout.Y_AXIS));
        JLabel pt = UITheme.sectionTitle("My Performance"); pt.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel ph = new JLabel("Tickets Handled:  " + staff.getHandledTickets());
        ph.setFont(new Font("Segoe UI", Font.BOLD, 26)); ph.setForeground(UITheme.SUCCESS);
        ph.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel ps = UITheme.smallLabel("Resolved / closed by you this session", UITheme.TEXT_MID);
        ps.setAlignmentX(Component.LEFT_ALIGNMENT);
        perf.add(pt); perf.add(Box.createVerticalStrut(10)); perf.add(ph); perf.add(Box.createVerticalStrut(4)); perf.add(ps);
        bottom.add(perf);

        JPanel cannedCard = UITheme.card(14);
        cannedCard.setLayout(new BorderLayout(0, 8));
        JLabel ch = UITheme.sectionTitle("Canned Response Templates  (" + ds.getCannedResponses().size() + ")");
        JLabel chint = UITheme.smallLabel("Click a template to preview", UITheme.TEXT_MID);
        JPanel cTop = new JPanel(new BorderLayout(0, 2)); cTop.setOpaque(false);
        cTop.add(ch, BorderLayout.NORTH); cTop.add(chint, BorderLayout.CENTER);
        cannedCard.add(cTop, BorderLayout.NORTH);

        JSplitPane cSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        cSplit.setResizeWeight(0.4); cSplit.setDividerSize(4);

        DefaultListModel<CannedResponse> cModel = new DefaultListModel<>();
        for (CannedResponse cr : ds.getCannedResponses()) cModel.addElement(cr);
        JList<CannedResponse> cList = new JList<>(cModel);
        cList.setFont(UITheme.F_LABEL); cList.setBackground(UITheme.ROW_ALT);
        cList.setSelectionBackground(UITheme.ROW_SELECT); cList.setSelectionForeground(UITheme.TEXT_DARK);
        cList.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                JLabel lb = (JLabel) super.getListCellRendererComponent(l, v, i, s, f);
                if (v instanceof CannedResponse) {
                    lb.setText(((CannedResponse) v).getTitle());
                    lb.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    if (!s) lb.setBackground(i % 2 == 0 ? Color.WHITE : UITheme.ROW_ALT);
                } return lb;
            }
        });

        JTextArea prev = UITheme.textArea(5, 18);
        prev.setEditable(false); prev.setBackground(new Color(252, 252, 255));
        prev.setFont(UITheme.F_SMALL); prev.setText("Select a template to preview its content.");
        JLabel catLbl = new JLabel(); catLbl.setFont(UITheme.F_SMALL.deriveFont(Font.BOLD));
        catLbl.setForeground(UITheme.PRIMARY);
        JPanel prevPanel = new JPanel(new BorderLayout(0, 4)); prevPanel.setBackground(Color.WHITE);
        prevPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        prevPanel.add(catLbl, BorderLayout.NORTH); prevPanel.add(UITheme.scrollPane(prev), BorderLayout.CENTER);

        cList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                CannedResponse cr = cList.getSelectedValue();
                if (cr != null) { prev.setText(cr.getBody()); prev.setCaretPosition(0); catLbl.setText("Category: " + cr.getCategory()); }
            }
        });

        cSplit.setLeftComponent(UITheme.scrollPane(cList));
        cSplit.setRightComponent(prevPanel);
        cannedCard.add(cSplit, BorderLayout.CENTER);
        bottom.add(cannedCard);

        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

   
    private JPanel buildTicketsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(UITheme.CARD_BG);
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));
        tfSearch   = UITheme.textField(16);
        cbStatus   = UITheme.comboBox(new String[]{"ALL","PENDING","IN_PROGRESS","RESOLVED","CLOSED"});
        cbPriority = UITheme.comboBox(new String[]{"ALL","LOW","MEDIUM","HIGH"});
        cbCategory = UITheme.comboBox(new String[]{"ALL","ACCOUNT","CARD","LOAN","TRANSACTION","GENERAL"});
        cbSort     = UITheme.comboBox(new String[]{"Date Desc","Date Asc","Priority","Priority Desc"});
        JButton btnSearch  = UITheme.primaryBtn("Search");
        JButton btnRefresh = UITheme.secondaryBtn("Refresh");
        btnSearch.setPreferredSize(new Dimension(80, 30)); btnRefresh.setPreferredSize(new Dimension(80, 30));
        toolbar.add(UITheme.label("Search:")); toolbar.add(tfSearch); toolbar.add(btnSearch);
        toolbar.add(new JSeparator(JSeparator.VERTICAL));
        toolbar.add(UITheme.label("Status:")); toolbar.add(cbStatus);
        toolbar.add(UITheme.label("Priority:")); toolbar.add(cbPriority);
        toolbar.add(UITheme.label("Category:")); toolbar.add(cbCategory);
        toolbar.add(UITheme.label("Sort:")); toolbar.add(cbSort);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Ticket ID", "Customer", "Category", "Priority", "Status", "Date Submitted"};
        tableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        table = new JTable(tableModel);
        styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(priorityRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(statusRenderer());

        workspacePanel = buildWorkspacePanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.35); split.setDividerSize(5);
        split.setLeftComponent(UITheme.scrollPane(table));
        split.setRightComponent(workspacePanel);
        panel.add(split, BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) loadDetail(); });
        btnSearch.addActionListener(e -> applyFilter());
        btnRefresh.addActionListener(e -> refreshTable(null, "ALL", "ALL", "ALL", "Date Desc"));
        cbStatus.addActionListener(e -> applyFilter()); cbPriority.addActionListener(e -> applyFilter());
        cbCategory.addActionListener(e -> applyFilter()); cbSort.addActionListener(e -> applyFilter());
        tfSearch.addActionListener(e -> applyFilter());

        refreshTable(null, "ALL", "ALL", "ALL", "Date Desc");
        return panel;
    }

    
    private JPanel buildWorkspacePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BACKGROUND);

        JPanel infoStrip = new JPanel(new BorderLayout(0, 0));
        infoStrip.setBackground(Color.WHITE);
        infoStrip.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 225, 235)));

        JLabel noTicket = new JLabel("   Select a ticket from the list to start working.");
        noTicket.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        noTicket.setForeground(new Color(175, 182, 200));
        noTicket.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel infoGrid = new JPanel(new GridLayout(2, 4, 0, 0));
        infoGrid.setBackground(Color.WHITE);

        wId       = headerInfoLabel("–");  wCustomer = headerInfoLabel("–");
        wCategory = headerInfoLabel("–");  wPriority = headerInfoLabel("–");
        wStatus   = headerInfoLabel("–");  wAssigned = headerInfoLabel("–");
        wDate     = headerInfoLabel("–");  wCSAT     = headerInfoLabel("–");

        infoGrid.add(infoCell("TICKET ID",   wId,       UITheme.PRIMARY));
        infoGrid.add(infoCell("CUSTOMER",    wCustomer, UITheme.ACCENT));
        infoGrid.add(infoCell("CATEGORY",    wCategory, UITheme.NEUTRAL));
        infoGrid.add(infoCell("PRIORITY",    wPriority, UITheme.DANGER));
        infoGrid.add(infoCell("STATUS",      wStatus,   UITheme.WARNING));
        infoGrid.add(infoCell("ASSIGNED TO", wAssigned, UITheme.SUCCESS));
        infoGrid.add(infoCell("SUBMITTED",   wDate,     UITheme.INFO));
        infoGrid.add(infoCell("CSAT RATING", wCSAT,     UITheme.SUCCESS));

        infoStrip.add(noTicket, BorderLayout.WEST);
        infoGrid.setVisible(false);
        infoStrip.add(infoGrid, BorderLayout.CENTER);
        p.add(infoStrip, BorderLayout.NORTH);

        workspaceTabs = new JTabbedPane(JTabbedPane.TOP);
        workspaceTabs.setFont(UITheme.F_BUTTON);
        workspaceTabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        workspaceTabs.addTab("  Chat & Respond  ", buildChatRespondTab());
        workspaceTabs.addTab("  Internal Notes  ", buildInternalTab());
        workspaceTabs.addTab("  History  ",        buildHistoryTab());

        workspaceTabs.setBackground(UITheme.BACKGROUND);

        p.add(workspaceTabs, BorderLayout.CENTER);

        p.putClientProperty("noTicket", noTicket);
        p.putClientProperty("infoGrid", infoGrid);
        return p;
    }

    private JPanel chatArea;      
    private JScrollPane chatScrollStaff;

    private JPanel buildChatRespondTab() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(new Color(235, 238, 245));

        chatArea = new JPanel();
        chatArea.setLayout(new BoxLayout(chatArea, BoxLayout.Y_AXIS));
        chatArea.setBackground(new Color(235, 238, 245));
        chatArea.setBorder(BorderFactory.createEmptyBorder(12, 14, 8, 14));

        JPanel chatWrapper = new JPanel(new BorderLayout());
        chatWrapper.setBackground(new Color(235, 238, 245));
        chatWrapper.add(chatArea, BorderLayout.NORTH);

        chatScrollStaff = new JScrollPane(chatWrapper,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        chatScrollStaff.setBorder(BorderFactory.createEmptyBorder());
        chatScrollStaff.getVerticalScrollBar().setUnitIncrement(16);
        p.add(chatScrollStaff, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(200, 210, 220)));

        JPanel pickerRow = new JPanel(new BorderLayout(8, 0));
        pickerRow.setBackground(new Color(240, 244, 252));
        pickerRow.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        JLabel tplLbl = UITheme.smallLabel("Template:", UITheme.TEXT_MID);
        tplLbl.setFont(UITheme.F_SMALL.deriveFont(Font.BOLD));
        tplLbl.setPreferredSize(new Dimension(80, 30));
        cbCanned = new JComboBox<>(); cbCanned.setFont(UITheme.F_LABEL);
        populateCannedCombo();
        JButton btnInsert = new JButton("Insert Template");
        btnInsert.setFont(UITheme.F_SMALL.deriveFont(Font.BOLD));
        btnInsert.setBackground(UITheme.PRIMARY); btnInsert.setForeground(Color.WHITE);
        btnInsert.setFocusPainted(false); btnInsert.setOpaque(true); btnInsert.setBorderPainted(false);
        btnInsert.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnInsert.setPreferredSize(new Dimension(150, 30));
        btnInsert.addActionListener(e -> insertCannedResponse());
        pickerRow.add(tplLbl, BorderLayout.WEST);
        pickerRow.add(cbCanned, BorderLayout.CENTER);
        pickerRow.add(btnInsert, BorderLayout.EAST);

        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setBackground(Color.WHITE);
        inputRow.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        taResponse = UITheme.textArea(3, 30);
        taResponse.setFont(UITheme.F_LABEL); taResponse.setBackground(new Color(246, 248, 252));
        taResponse.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(190, 205, 225)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JScrollPane respScroll = new JScrollPane(taResponse,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        respScroll.setBorder(BorderFactory.createLineBorder(new Color(190, 205, 225)));
        respScroll.setPreferredSize(new Dimension(0, 72));

        JButton btnSend = new JButton("Send");
        btnSend.setFont(UITheme.F_BUTTON); btnSend.setBackground(UITheme.PRIMARY);
        btnSend.setForeground(Color.WHITE); btnSend.setFocusPainted(false);
        btnSend.setOpaque(true); btnSend.setBorderPainted(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setPreferredSize(new Dimension(80, 72));
        btnSend.addActionListener(e -> doRespond());

        inputRow.add(respScroll, BorderLayout.CENTER);
        inputRow.add(btnSend, BorderLayout.EAST);

        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        actionRow.setBackground(new Color(245, 247, 252));
        actionRow.setBorder(new MatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));
        cbNewStatus = UITheme.comboBox(new String[]{"PENDING", "IN_PROGRESS", "RESOLVED"});
        cbNewStatus.setPreferredSize(new Dimension(175, 32));
        JButton btnStatusOnly = UITheme.warningBtn("Update Status");
        JButton btnReopen     = UITheme.successBtn("Reopen");
        btnStatusOnly.setPreferredSize(new Dimension(140, 32));
        btnReopen.setPreferredSize(new Dimension(100, 32));
        actionRow.add(UITheme.label("Status:"));
        actionRow.add(cbNewStatus);
        actionRow.add(btnStatusOnly);
        actionRow.add(btnReopen);
        btnStatusOnly.addActionListener(e -> doUpdateStatus());
        btnReopen.addActionListener(e     -> doReopen());

        JPanel remarksRow = new JPanel(new BorderLayout(8, 0));
        remarksRow.setBackground(new Color(255, 252, 235));
        remarksRow.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, new Color(230, 200, 80)),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        JLabel remLbl = new JLabel("Internal Remarks");
        remLbl.setFont(UITheme.F_BUTTON); remLbl.setForeground(new Color(140, 100, 0));
        remLbl.setPreferredSize(new Dimension(160, 52));
        taRemarks = UITheme.textArea(2, 20); taRemarks.setFont(UITheme.F_LABEL);
        taRemarks.setBackground(Color.WHITE);
        JScrollPane remScroll = new JScrollPane(taRemarks,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        remScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 180, 60)));
        remScroll.setPreferredSize(new Dimension(0, 52));
        JButton btnRemarks = UITheme.primaryBtn("Save");
        btnRemarks.setBackground(new Color(160, 120, 0));
        btnRemarks.setPreferredSize(new Dimension(80, 52));
        btnRemarks.addActionListener(e -> doSaveRemarks());
        remarksRow.add(remLbl, BorderLayout.WEST);
        remarksRow.add(remScroll, BorderLayout.CENTER);
        remarksRow.add(btnRemarks, BorderLayout.EAST);

        bottomPanel.add(pickerRow, BorderLayout.NORTH);
        JPanel midBottom = new JPanel(new BorderLayout());
        midBottom.setOpaque(false);
        midBottom.add(inputRow, BorderLayout.NORTH);
        midBottom.add(actionRow, BorderLayout.CENTER);
        midBottom.add(remarksRow, BorderLayout.SOUTH);
        bottomPanel.add(midBottom, BorderLayout.CENTER);
        p.add(bottomPanel, BorderLayout.SOUTH);
        return p;
    }

    private void rebuildChatArea(Ticket t) {
        chatArea.removeAll();
  
        boolean hasAnyContent = false;


        chatArea.add(staffChatBubble(
            t.getCustomerName() + "  (Customer)",
            t.getDescription(),
            t.getFormattedCreatedAt(), false));
        chatArea.add(Box.createVerticalStrut(10));
        hasAnyContent = true;

        for (TicketHistory h : t.getHistory()) {
            String action = h.getAction();
            String notes  = h.getNotes();

            if (action.startsWith("Response Added") && !notes.isEmpty()) {
                chatArea.add(staffChatBubble(
                    h.getPerformedByName() + "  (Staff)",
                    notes, h.getFormattedTimestamp(), true));
                chatArea.add(Box.createVerticalStrut(10));
                hasAnyContent = true;

            } else if (action.equals("Customer Replied") && !notes.isEmpty()) {
                chatArea.add(staffChatBubble(
                    t.getCustomerName() + "  (Customer)",
                    notes, h.getFormattedTimestamp(), false));
                chatArea.add(Box.createVerticalStrut(10));
                hasAnyContent = true;
            }
        }

        if (!hasAnyContent || t.getResponse().isEmpty()) {
            boolean hasResp = t.getHistory().stream()
                .anyMatch(h -> h.getAction().startsWith("Response Added") && !h.getNotes().isEmpty());
            if (!hasResp) {
                JLabel wait = new JLabel("  No response sent yet — type a response below and click Send.");
                wait.setFont(UITheme.F_LABEL.deriveFont(Font.ITALIC));
                wait.setForeground(UITheme.TEXT_LIGHT);
                wait.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                wait.setAlignmentX(Component.LEFT_ALIGNMENT);
                chatArea.add(wait);
            }
        }

        chatArea.revalidate();
        chatArea.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = chatScrollStaff.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private JPanel staffChatBubble(String sender, String message, String time, boolean isStaff) {
        Color bubbleBg = isStaff ? new Color(0, 95, 184) : Color.WHITE;
        Color bubbleFg = isStaff ? Color.WHITE            : UITheme.TEXT_DARK;
        Color senderFg = isStaff ? new Color(180, 215, 255) : UITheme.TEXT_MID;
        Color timeFg   = isStaff ? new Color(180, 215, 255) : UITheme.TEXT_LIGHT;

        JPanel bubble = new JPanel(new BorderLayout(0, 4));
        bubble.setBackground(bubbleBg);
        bubble.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(isStaff ? new Color(0, 70, 155) : new Color(215, 220, 230), 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JLabel senderLbl = new JLabel(sender);
        senderLbl.setFont(UITheme.F_SMALL.deriveFont(Font.BOLD));
        senderLbl.setForeground(senderFg);

        JLabel msgLbl = new JLabel("<html><body style='width:360px'>" + message + "</body></html>");
        msgLbl.setFont(UITheme.F_LABEL); msgLbl.setForeground(bubbleFg);

        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(UITheme.F_SMALL); timeLbl.setForeground(timeFg);

        bubble.add(senderLbl, BorderLayout.NORTH);
        bubble.add(msgLbl,    BorderLayout.CENTER);
        bubble.add(timeLbl,   BorderLayout.SOUTH);

        JPanel outer = new JPanel(new FlowLayout(
            isStaff ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        outer.setOpaque(false);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));
        bubble.setPreferredSize(new Dimension(520, bubble.getPreferredSize().height));
        outer.add(bubble);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return outer;
    }

    private JPanel buildInternalTab() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(50, 80, 130));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel hl = new JLabel("Staff-to-Staff Internal Messages");
        hl.setFont(UITheme.F_BUTTON); hl.setForeground(Color.WHITE);
        JLabel hint = UITheme.smallLabel("Private thread — never visible to customers", UITheme.TEXT_LIGHT);
        hint.setForeground(new Color(180, 210, 255));
        header.add(hl, BorderLayout.WEST); header.add(hint, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        msgPanel = new JPanel();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        msgPanel.setBackground(UITheme.BACKGROUND);
        JPanel msgWrapper = new JPanel(new BorderLayout());
        msgWrapper.setBackground(UITheme.BACKGROUND);
        msgWrapper.add(msgPanel, BorderLayout.NORTH);
        JScrollPane msgScroll = new JScrollPane(msgWrapper,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        msgScroll.getVerticalScrollBar().setUnitIncrement(14);
        msgScroll.setBorder(BorderFactory.createEmptyBorder());
        p.add(msgScroll, BorderLayout.CENTER);

        JPanel inputArea = new JPanel(new BorderLayout(10, 0));
        inputArea.setBackground(new Color(240, 244, 252));
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        taMsgInput = UITheme.textArea(3, 28);
        taMsgInput.setFont(UITheme.F_LABEL); taMsgInput.setBackground(Color.WHITE);
        JScrollPane msgInputScroll = new JScrollPane(taMsgInput,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        msgInputScroll.setBorder(BorderFactory.createLineBorder(new Color(170, 195, 235)));
        msgInputScroll.setPreferredSize(new Dimension(0, 70));

        JButton btnSend = new JButton("Send");
        btnSend.setFont(UITheme.F_BUTTON); btnSend.setBackground(new Color(50, 80, 130));
        btnSend.setForeground(Color.WHITE); btnSend.setFocusPainted(false);
        btnSend.setOpaque(true); btnSend.setBorderPainted(false);
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setPreferredSize(new Dimension(90, 70));
        btnSend.addActionListener(e -> doSendInternalMessage());

        JLabel sendLbl = UITheme.label("Message to team:");
        sendLbl.setFont(UITheme.F_SMALL.deriveFont(Font.BOLD));
        sendLbl.setForeground(UITheme.TEXT_MID);
        sendLbl.setPreferredSize(new Dimension(140, 70));

        inputArea.add(sendLbl,       BorderLayout.WEST);
        inputArea.add(msgInputScroll,BorderLayout.CENTER);
        inputArea.add(btnSend,       BorderLayout.EAST);
        p.add(inputArea, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildHistoryTab() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UITheme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(55, 65, 85));
        header.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel hl = new JLabel("Full Ticket Audit Trail");
        hl.setFont(UITheme.F_BUTTON); hl.setForeground(Color.WHITE);
        JLabel hint = UITheme.smallLabel("Every action on this ticket, oldest to newest", UITheme.TEXT_LIGHT);
        hint.setForeground(new Color(190, 200, 220));
        header.add(hl, BorderLayout.WEST); header.add(hint, BorderLayout.EAST);
        p.add(header, BorderLayout.NORTH);

        taHistory = new JTextArea();
        taHistory.setEditable(false);
        taHistory.setFont(new Font("Consolas", Font.PLAIN, 13));
        taHistory.setForeground(UITheme.TEXT_DARK);
        taHistory.setBackground(new Color(252, 252, 255));
        taHistory.setLineWrap(false);
        taHistory.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JScrollPane sp = new JScrollPane(taHistory,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JLabel headerInfoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l.setForeground(UITheme.TEXT_DARK);
        return l;
    }

    private JPanel infoCell(String labelText, JLabel valueLabel, Color accent) {
        JPanel cell = new JPanel(new BorderLayout(0, 5));
        cell.setBackground(Color.WHITE);
        cell.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 234, 242), 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JPanel labelRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        labelRow.setOpaque(false);
        JLabel dot = new JLabel("\u25CF");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 8));
        dot.setForeground(accent);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(UITheme.TEXT_LIGHT);
        labelRow.add(dot); labelRow.add(lbl);

        cell.add(labelRow,   BorderLayout.NORTH);
        cell.add(valueLabel, BorderLayout.CENTER);
        return cell;
    }

  
    private Ticket selectedTicket() {
        int row = table.getSelectedRow();
        if (row < 0 || displayed == null) return null;
        int idx = table.convertRowIndexToModel(row);
        return idx < displayed.size() ? displayed.get(idx) : null;
    }

    private void loadDetail() {
        Ticket t = selectedTicket();

        Object noTicket = workspacePanel.getClientProperty("noTicket");
        Object infoGrid = workspacePanel.getClientProperty("infoGrid");
        if (noTicket instanceof JLabel)   ((JLabel) noTicket).setVisible(t == null);
        if (infoGrid instanceof JComponent) ((JComponent) infoGrid).setVisible(t != null);

        if (t == null) return;

        wId.setText(t.getTicketId());
        wCustomer.setText(t.getCustomerName());
        wCategory.setText(t.getCategory().name());
        wPriority.setText(t.getPriority().name());
        wPriority.setForeground(UITheme.priorityColor(t.getPriority().name()));
        wStatus.setText(t.getStatus().name());
        wStatus.setForeground(UITheme.statusColor(t.getStatus().name()));
        wAssigned.setText(t.getAssignedStaffName() != null ? t.getAssignedStaffName() : "Unassigned");
        wDate.setText(t.getFormattedCreatedAt());
        if (t.getRating() > 0) {
            wCSAT.setText(t.getStarsDisplay() + " (" + t.getRating() + "/5)");
            wCSAT.setForeground(UITheme.SUCCESS);
        } else {
            wCSAT.setText("Not rated"); wCSAT.setForeground(UITheme.TEXT_LIGHT);
        }

        taResponse.setText("");        
        taRemarks.setText(t.getInternalRemarks());
        cbNewStatus.setSelectedItem(t.getStatus().name());
        populateCannedCombo();

        rebuildChatArea(t);

        msgPanel.removeAll();
        msgPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        List<StaffMessage> msgs = ds.getMessagesForTicket(t.getTicketId());
        if (msgs.isEmpty()) {
            JLabel none = UITheme.smallLabel(
                "No internal messages yet for this ticket. Start a team conversation below.",
                UITheme.TEXT_MID);
            none.setBorder(BorderFactory.createEmptyBorder(16, 12, 16, 12));
            none.setFont(UITheme.F_LABEL.deriveFont(Font.ITALIC));
            msgPanel.add(none);
        } else {
            for (StaffMessage m : msgs) {
                msgPanel.add(buildStaffMsgBubble(m));
                msgPanel.add(Box.createVerticalStrut(8));
            }
        }
        msgPanel.revalidate(); msgPanel.repaint();

        StringBuilder sb = new StringBuilder();
        for (TicketHistory h : t.getHistory()) sb.append(h.toString()).append("\n\n");
        taHistory.setText(sb.toString());
        taHistory.setCaretPosition(0);

        workspaceTabs.setSelectedIndex(0);
    }

    private JPanel buildStaffMsgBubble(StaffMessage m) {
        boolean mine = m.getSenderId().equals(staff.getUserId());
        Color bubbleBg = mine ? new Color(0, 95, 184) : Color.WHITE;
        Color bubbleFg = mine ? Color.WHITE            : UITheme.TEXT_DARK;
        Color senderFg = mine ? new Color(180, 215, 255) : UITheme.TEXT_MID;
        Color timeFg   = mine ? new Color(180, 215, 255) : UITheme.TEXT_LIGHT;

        JPanel bubble = new JPanel(new BorderLayout(0, 4));
        bubble.setBackground(bubbleBg);
        bubble.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(mine ? new Color(0, 70, 155) : new Color(215, 220, 230), 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JLabel senderLbl = new JLabel(m.getSenderName() + (mine ? "  (You)" : "  (Colleague)"));
        senderLbl.setFont(UITheme.F_SMALL.deriveFont(Font.BOLD));
        senderLbl.setForeground(senderFg);

        JLabel msgLbl = new JLabel("<html><body style='width:340px'>" + m.getMessage() + "</body></html>");
        msgLbl.setFont(UITheme.F_LABEL); msgLbl.setForeground(bubbleFg);

        JLabel timeLbl = new JLabel(m.getFormattedTime());
        timeLbl.setFont(UITheme.F_SMALL); timeLbl.setForeground(timeFg);

        bubble.add(senderLbl, BorderLayout.NORTH);
        bubble.add(msgLbl,    BorderLayout.CENTER);
        bubble.add(timeLbl,   BorderLayout.SOUTH);

        JPanel outer = new JPanel(new FlowLayout(mine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        outer.setOpaque(false);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.setMaximumSize(new Dimension(480, Integer.MAX_VALUE));
        bubble.setPreferredSize(new Dimension(480, bubble.getPreferredSize().height));
        outer.add(bubble);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return outer;
    }

    
    private void populateCannedCombo() {
        cbCanned.removeAllItems();
        cbCanned.addItem(new CannedResponse("", "-- Select a response template --", "", ""));
        for (CannedResponse cr : ds.getCannedResponses())
            if (cr.isActive()) cbCanned.addItem(cr);
    }

    private void insertCannedResponse() {
        Object sel = cbCanned.getSelectedItem();
        if (sel instanceof CannedResponse) {
            CannedResponse cr = (CannedResponse) sel;
            if (!cr.getId().isEmpty()) {
                String existing = taResponse.getText().trim();
                taResponse.setText(existing.isEmpty() ? cr.getBody() : existing + "\n\n" + cr.getBody());
                taResponse.setCaretPosition(0);
            }
        }
    }

    
    private void doRespond() {
        Ticket t = selectedTicket();
        if (t == null) { MainFrame.showError(this, "Select a ticket first."); return; }
        String resp = taResponse.getText().trim();
        if (resp.isEmpty()) { MainFrame.showError(this, "Response cannot be empty."); return; }
        String err = ts.respondToTicket(t.getTicketId(), staff.getUserId(), staff.getFullName(),
            resp, (String) cbNewStatus.getSelectedItem());
        if (err != null) { MainFrame.showError(this, err); return; }
        taResponse.setText("");              
        applyFilterKeepSelection();          
        loadDetail();                       
        workspaceTabs.setSelectedIndex(0);   
    }

    private void doUpdateStatus() {
        Ticket t = selectedTicket();
        if (t == null) { MainFrame.showError(this, "Select a ticket first."); return; }
        String err = ts.updateStatus(t.getTicketId(), staff.getUserId(), staff.getFullName(),
            (String) cbNewStatus.getSelectedItem());
        if (err != null) { MainFrame.showError(this, err); return; }
        applyFilterKeepSelection(); loadDetail();
    }

    private void doSaveRemarks() {
        Ticket t = selectedTicket();
        if (t == null) { MainFrame.showError(this, "Select a ticket first."); return; }
        String r = taRemarks.getText().trim();
        if (r.isEmpty()) { MainFrame.showError(this, "Remarks cannot be empty."); return; }
        String err = ts.addInternalRemarks(t.getTicketId(), staff.getUserId(), staff.getFullName(), r);
        if (err != null) { MainFrame.showError(this, err); return; }
        MainFrame.showInfo(this, "Remarks saved."); applyFilterKeepSelection(); loadDetail();
    }

    private void doReopen() {
        Ticket t = selectedTicket();
        if (t == null) { MainFrame.showError(this, "Select a ticket first."); return; }
        if (!MainFrame.confirm(this, "Reopen ticket " + t.getTicketId() + "?")) return;
        String err = ts.reopenTicket(t.getTicketId(), staff.getUserId(), staff.getFullName());
        if (err != null) { MainFrame.showError(this, err); return; }
        MainFrame.showInfo(this, "Ticket reopened."); applyFilterKeepSelection(); loadDetail();
    }

    private void doSendInternalMessage() {
        Ticket t = selectedTicket();
        if (t == null) { MainFrame.showError(this, "Select a ticket first."); return; }
        String msg = taMsgInput.getText().trim();
        if (msg.isEmpty()) { MainFrame.showError(this, "Message cannot be empty."); return; }
        StaffMessage sm = new StaffMessage(ds.nextMsgId(), t.getTicketId(),
            staff.getUserId(), staff.getFullName(), msg);
        ds.addStaffMessage(sm);
        t.addHistory(new TicketHistory("Internal Message Added",
            staff.getUserId(), staff.getFullName(), "(private)"));
        taMsgInput.setText("");
        loadDetail();                        // no filter needed — table doesn't change
        workspaceTabs.setSelectedIndex(1);   // switch to Internal Notes tab
    }

   
    private void applyFilter() {
        String search = tfSearch.getText().trim();
        refreshTable(search.isEmpty() ? null : search,
            (String) cbStatus.getSelectedItem(), (String) cbPriority.getSelectedItem(),
            (String) cbCategory.getSelectedItem(), (String) cbSort.getSelectedItem());
    }

   
    private void applyFilterKeepSelection() {
        Ticket current = selectedTicket();
        String currentId = current != null ? current.getTicketId() : null;

        applyFilter();

        if (currentId != null) {
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                if (currentId.equals(tableModel.getValueAt(row, 0))) {
                    table.setRowSelectionInterval(row, row);
                    table.scrollRectToVisible(table.getCellRect(row, 0, true));
                    break;
                }
            }
        }
    }

    private void refreshTable(String search, String status, String priority,
                               String category, String sort) {
        List<Ticket> all = search != null ? ts.searchByKeyword(search) : ts.getAllTickets();
        all = ts.filterTickets(
                status   == null ? "ALL" : status,
                priority == null ? "ALL" : priority,
                category == null ? "ALL" : category)
              .stream().filter(all::contains).collect(Collectors.toList());
        displayed = ts.sortTickets(all, sort == null ? "Date Desc" : sort);
        tableModel.setRowCount(0);
        for (Ticket t : displayed)
            tableModel.addRow(new Object[]{
                t.getTicketId(), t.getCustomerName(), t.getCategory().name(),
                t.getPriority().name(), t.getStatus().name(), t.getFormattedCreatedAt()});
    }

    
    private void styleTable(JTable t) {
        UITheme.styleTable(t);
        JTableHeader h = t.getTableHeader();
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                l.setBackground(UITheme.PRIMARY); l.setForeground(Color.WHITE);
                l.setFont(UITheme.F_TABLE_H);
                l.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                l.setOpaque(true); return l;
            }
        });
        h.setBackground(UITheme.PRIMARY); h.setForeground(Color.WHITE); h.setOpaque(true);
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
