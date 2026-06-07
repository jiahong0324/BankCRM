package bankcrm.gui.manager;

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

public class ManagerDashboard extends JPanel {

    private final Manager       manager;
    private final TicketService ts = TicketService.getInstance();
    private final UserService   us = UserService.getInstance();
    private final AuthService   as = AuthService.getInstance();
    private final DataStore     ds = DataStore.getInstance();

    public ManagerDashboard(Manager manager) {
        this.manager = manager;
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        buildUI();
    }

  
    private void buildUI() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("  SecureBank CRM  -  Manager Portal");
        title.setFont(UITheme.F_TITLE);
        title.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel user = new JLabel(manager.getFullName() + "  |  " + manager.getManagerLevel());
        user.setFont(UITheme.F_LABEL);
        user.setForeground(new Color(200, 220, 255));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UITheme.F_BUTTON);
        btnLogout.setBackground(new Color(220, 80, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setOpaque(true);
        btnLogout.setBorderPainted(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(88, 32));
        btnLogout.addActionListener(e -> {
            if (MainFrame.confirm(this, "Are you sure you want to logout?"))
                MainFrame.getInstance().logout();
        });

        right.add(user);
        right.add(btnLogout);
        header.add(title, BorderLayout.WEST);
        header.add(right,  BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.F_BUTTON);
        tabs.addTab("  Dashboard",          buildDashboardTab());
        tabs.addTab("  Ticket Management",  buildTicketTab());
        tabs.addTab("  Staff Management",   buildStaffTab());
        tabs.addTab("  Customer Management", new bankcrm.gui.customer.CustomerManagementPanel(
                                                bankcrm.gui.customer.CustomerManagementPanel.Mode.MANAGER_VIEW, manager));
        tabs.addTab("  Reports",            buildReportsTab());
        tabs.addTab("  Announcements",      buildAnnouncementsTab());
        add(tabs, BorderLayout.CENTER);

        tabs.addChangeListener(e -> {
            switch (tabs.getSelectedIndex()) {
                case 1: refreshTicketTable(null, "ALL", "ALL", "Date Desc"); break;
                case 2: refreshStaffTable(); break;
                
                case 5: refreshAnnouncements(); break;
            }
        });
    }

    
    private JPanel buildDashboardTab() {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcome = new JLabel("Manager Dashboard  -  Overview");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcome.setForeground(UITheme.PRIMARY);
        p.add(welcome, BorderLayout.NORTH);

        JPanel stats = new JPanel(new GridLayout(2, 3, 12, 12));
        stats.setOpaque(false);
        stats.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        long   total    = ts.getAllTickets().size();
        long   pending  = ts.countByStatus(Ticket.Status.PENDING);
        long   inProg   = ts.countByStatus(Ticket.Status.IN_PROGRESS);
        long   resolved = ts.countByStatus(Ticket.Status.RESOLVED);
        long   closed   = ts.countByStatus(Ticket.Status.CLOSED);
        double avgMin   = ts.avgResponseMinutes();
        String avgStr   = avgMin < 60
            ? String.format("%.0f min", avgMin)
            : String.format("%.1f hrs", avgMin / 60);

        stats.add(UITheme.statCard("Total Tickets",     String.valueOf(total),    UITheme.INFO));
        stats.add(UITheme.statCard("Pending",           String.valueOf(pending),  UITheme.WARNING));
        stats.add(UITheme.statCard("In Progress",       String.valueOf(inProg),   UITheme.ACCENT));
        stats.add(UITheme.statCard("Resolved",          String.valueOf(resolved), UITheme.SUCCESS));
        stats.add(UITheme.statCard("Closed",            String.valueOf(closed),   UITheme.NEUTRAL));
        stats.add(UITheme.statCard("Avg Response Time", avgStr,                   UITheme.PRIMARY));
        p.add(stats, BorderLayout.CENTER);

        JPanel staffSnap = UITheme.card(16);
        staffSnap.setLayout(new BorderLayout(0, 8));
        JLabel snapTitle = UITheme.sectionTitle("Staff Performance Snapshot");
        staffSnap.add(snapTitle, BorderLayout.NORTH);

        String[] cols = {"Staff Name","Department","Tickets Handled","Status"};
        DefaultTableModel snapModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable snapTable = new JTable(snapModel);
        styleTable(snapTable);   // use local styleTable that forces header colours

        for (Staff s : ds.getStaffList()) {
            if (s instanceof Manager) continue;
            snapModel.addRow(new Object[]{
                s.getFullName(), s.getDepartment(),
                s.getHandledTickets(), s.isActive() ? "Active" : "Inactive"
            });
        }
        staffSnap.add(UITheme.scrollPane(snapTable), BorderLayout.CENTER);
        staffSnap.setPreferredSize(new Dimension(0, 220));
        p.add(staffSnap, BorderLayout.SOUTH);
        return p;
    }

    
    private DefaultTableModel ticketModel;
    private JTable            ticketTable;
    private List<Ticket>      displayedTickets;
    private JTextField        tfTSearch;
    private JComboBox<String> cbTStatus, cbTPriority, cbTSort;
    private JLabel     dtId, dtCustomer, dtCat, dtPriority, dtStatus, dtAssigned, dtDate, dtRating;
    private JTextArea  taDetailDesc, taDetailResp, taDetailHistory;
    private JComboBox<String> cbAssignStaff;

    private JPanel buildTicketTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setBackground(UITheme.CARD_BG);
        toolbar.setBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));

        tfTSearch   = UITheme.textField(18);
        cbTStatus   = UITheme.comboBox(new String[]{"ALL","PENDING","IN_PROGRESS","RESOLVED","CLOSED"});
        cbTPriority = UITheme.comboBox(new String[]{"ALL","LOW","MEDIUM","HIGH"});
        cbTSort     = UITheme.comboBox(new String[]{"Date Desc","Date Asc","Priority Desc","Priority"});

        JButton btnSearch  = UITheme.primaryBtn("Search");
        JButton btnRefresh = UITheme.secondaryBtn("Refresh");
        btnSearch.setPreferredSize(new Dimension(80, 28));
        btnRefresh.setPreferredSize(new Dimension(80, 28));

        toolbar.add(UITheme.label("Search:")); toolbar.add(tfTSearch); toolbar.add(btnSearch);
        toolbar.add(new JSeparator(JSeparator.VERTICAL));
        toolbar.add(UITheme.label("Status:")); toolbar.add(cbTStatus);
        toolbar.add(UITheme.label("Priority:")); toolbar.add(cbTPriority);
        toolbar.add(UITheme.label("Sort:")); toolbar.add(cbTSort);
        toolbar.add(btnRefresh);
        panel.add(toolbar, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.45);
        split.setDividerSize(6);

        String[] cols = {"Ticket ID","Customer","Category","Priority","Status","Assigned To","Date"};
        ticketModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        ticketTable = new JTable(ticketModel);
        styleTable(ticketTable);
        ticketTable.getColumnModel().getColumn(3).setCellRenderer(priorityRenderer());
        ticketTable.getColumnModel().getColumn(4).setCellRenderer(statusRenderer());
        split.setLeftComponent(UITheme.scrollPane(ticketTable));
        split.setRightComponent(buildTicketDetailPane());
        panel.add(split, BorderLayout.CENTER);

        ticketTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadTicketDetail();
        });
        btnSearch.addActionListener(e  -> applyTicketFilter());
        btnRefresh.addActionListener(e -> refreshTicketTable(null, "ALL", "ALL", "Date Desc"));
        cbTStatus.addActionListener(e  -> applyTicketFilter());
        cbTPriority.addActionListener(e-> applyTicketFilter());
        cbTSort.addActionListener(e    -> applyTicketFilter());
        tfTSearch.addActionListener(e  -> applyTicketFilter());

        refreshTicketTable(null, "ALL", "ALL", "Date Desc");
        return panel;
    }

    private JPanel buildTicketDetailPane() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel sec = UITheme.sectionTitle("Ticket Details & Actions");
        p.add(sec, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JPanel info = UITheme.card(10);
        info.setLayout(new GridLayout(4, 4, 6, 4));
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        dtId       = UITheme.label("-"); dtCustomer = UITheme.label("-");
        dtCat      = UITheme.label("-"); dtPriority = UITheme.label("-");
        dtStatus   = UITheme.label("-"); dtAssigned = UITheme.label("-");
        dtDate     = UITheme.label("-"); dtRating   = UITheme.label("-");
        info.add(UITheme.label("Ticket ID:")); info.add(dtId);
        info.add(UITheme.label("Customer:"));  info.add(dtCustomer);
        info.add(UITheme.label("Category:"));  info.add(dtCat);
        info.add(UITheme.label("Priority:"));  info.add(dtPriority);
        info.add(UITheme.label("Status:"));    info.add(dtStatus);
        info.add(UITheme.label("Assigned:"));  info.add(dtAssigned);
        info.add(UITheme.label("Submitted:")); info.add(dtDate);
        info.add(UITheme.label("Rating:"));    info.add(dtRating);

        JPanel assignRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        assignRow.setOpaque(false);
        assignRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbAssignStaff = new JComboBox<>();
        cbAssignStaff.setFont(UITheme.F_LABEL);
        populateStaffCombo();
        JButton btnAssign = UITheme.primaryBtn("Assign Staff");
        btnAssign.setPreferredSize(new Dimension(120, 30));
        assignRow.add(UITheme.label("Assign to:")); assignRow.add(cbAssignStaff); assignRow.add(btnAssign);
        btnAssign.addActionListener(e -> doAssign());

        taDetailDesc    = UITheme.textArea(3, 30); taDetailDesc.setEditable(false); taDetailDesc.setBackground(UITheme.ROW_ALT);
        taDetailResp    = UITheme.textArea(3, 30); taDetailResp.setEditable(false); taDetailResp.setBackground(UITheme.ROW_ALT);
        taDetailHistory = UITheme.textArea(8, 30); taDetailHistory.setEditable(false);
        taDetailHistory.setBackground(new Color(252,252,255));
        taDetailHistory.setFont(new Font("Consolas", Font.PLAIN, 14));
        taDetailHistory.setLineWrap(false);

        body.add(info);
        body.add(Box.createVerticalStrut(8));
        body.add(assignRow);
        body.add(Box.createVerticalStrut(8));
        addSec(body, "Description:",    UITheme.scrollPane(taDetailDesc),    80);
        addSec(body, "Staff Response:", UITheme.scrollPane(taDetailResp),    80);
        JScrollPane histSP = new JScrollPane(taDetailHistory, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); histSP.getVerticalScrollBar().setUnitIncrement(16); histSP.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR)); addSec(body, "Ticket History:", histSP, 240);

        JPanel bodyWrapper = new JPanel(new BorderLayout());
        bodyWrapper.setOpaque(false);
        bodyWrapper.add(body, BorderLayout.NORTH);
        JScrollPane bodyScroll = new JScrollPane(bodyWrapper,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bodyScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        bodyScroll.getVerticalScrollBar().setUnitIncrement(16);
        p.add(bodyScroll, BorderLayout.CENTER);
        return p;
    }

    private void addSec(JPanel p, String t, JComponent c, int h) {
        JLabel l = UITheme.label(t); l.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setAlignmentX(Component.LEFT_ALIGNMENT); c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
        p.add(l); p.add(Box.createVerticalStrut(3)); p.add(c); p.add(Box.createVerticalStrut(6));
    }

    private void populateStaffCombo() {
        cbAssignStaff.removeAllItems();
        for (Staff s : ds.getStaffList())
            if (!(s instanceof Manager) && s.isActive())
                cbAssignStaff.addItem(s.getFullName() + " [" + s.getUserId() + "]");
    }

    private void loadTicketDetail() {
        int row = ticketTable.getSelectedRow();
        if (row < 0 || displayedTickets == null) return;
        int idx = ticketTable.convertRowIndexToModel(row);
        if (idx >= displayedTickets.size()) return;
        Ticket t = displayedTickets.get(idx);

        dtId.setText(t.getTicketId()); dtCustomer.setText(t.getCustomerName());
        dtCat.setText(t.getCategory().name()); dtPriority.setText(t.getPriority().name());
        dtStatus.setText(t.getStatus().name());
        dtAssigned.setText(t.getAssignedStaffName() != null ? t.getAssignedStaffName() : "Unassigned");
        dtDate.setText(t.getFormattedCreatedAt());
        dtRating.setText(t.getRating() > 0 ? t.getStarsDisplay() + " (" + t.getRating() + "/5)" : "Not rated");
        taDetailDesc.setText(t.getDescription());
        taDetailResp.setText(t.getResponse().isEmpty() ? "No response yet." : t.getResponse());
        StringBuilder sb = new StringBuilder();
        for (TicketHistory h : t.getHistory()) sb.append(h.toString()).append("\n\n");
        taDetailHistory.setText(sb.toString()); taDetailHistory.setCaretPosition(0);
        populateStaffCombo();
    }

    private void doAssign() {
        int row = ticketTable.getSelectedRow();
        if (row < 0) { MainFrame.showError(this, "Select a ticket first."); return; }
        int idx = ticketTable.convertRowIndexToModel(row);
        Ticket t = displayedTickets.get(idx);
        String sel = (String) cbAssignStaff.getSelectedItem();
        if (sel == null) { MainFrame.showError(this, "No staff available."); return; }
        String staffId = sel.substring(sel.lastIndexOf('[') + 1, sel.lastIndexOf(']'));
        String err = ts.assignTicket(t.getTicketId(), manager.getUserId(), manager.getFullName(), staffId);
        if (err != null) { MainFrame.showError(this, err); return; }
        MainFrame.showInfo(this, "Ticket assigned successfully.");
        applyTicketFilter(); loadTicketDetail();
    }

    private void applyTicketFilter() {
        String search = tfTSearch.getText().trim();
        refreshTicketTable(search.isEmpty() ? null : search,
            (String) cbTStatus.getSelectedItem(),
            (String) cbTPriority.getSelectedItem(),
            (String) cbTSort.getSelectedItem());
    }

    private void refreshTicketTable(String search, String status, String priority, String sort) {
        List<Ticket> all = (search != null && !search.isBlank())
            ? ts.searchByKeyword(search) : ts.getAllTickets();
        if (status   != null && !status.equals("ALL"))
            all = all.stream().filter(t -> t.getStatus().name().equals(status)).collect(Collectors.toList());
        if (priority != null && !priority.equals("ALL"))
            all = all.stream().filter(t -> t.getPriority().name().equals(priority)).collect(Collectors.toList());
        displayedTickets = ts.sortTickets(all, sort != null ? sort : "Date Desc");
        ticketModel.setRowCount(0);
        for (Ticket t : displayedTickets)
            ticketModel.addRow(new Object[]{
                t.getTicketId(), t.getCustomerName(), t.getCategory().name(),
                t.getPriority().name(), t.getStatus().name(),
                t.getAssignedStaffName() != null ? t.getAssignedStaffName() : "-",
                t.getFormattedCreatedAt()
            });
    }

  
    private DefaultTableModel staffModel;
    private JTable            staffTable;
    private JTextField        sfName, sfUsername, sfEmail, sfPhone, sfDept;
    private JPasswordField    sfPassword, sfConfirm;
    private JLabel            sfErr;
    private String            editingStaffId = null;

    private JPanel buildStaffTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"Name","Username","Dept","Tickets Handled","Status"};
        staffModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        staffTable = new JTable(staffModel);
        styleTable(staffTable);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);
        tablePanel.add(UITheme.sectionTitle("Staff Members"), BorderLayout.NORTH);
        tablePanel.add(UITheme.scrollPane(staffTable), BorderLayout.CENTER);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        tableActions.setOpaque(false);
        JButton btnNew    = UITheme.successBtn("+ New Staff");
        JButton btnEdit   = UITheme.primaryBtn("Edit");
        JButton btnToggle = UITheme.warningBtn("Toggle Active");
        JButton btnDelete = UITheme.dangerBtn("Delete");
        for (JButton b : new JButton[]{btnNew, btnEdit, btnToggle, btnDelete})
            b.setPreferredSize(new Dimension(125, 30));
        tableActions.add(btnNew); tableActions.add(btnEdit);
        tableActions.add(btnToggle); tableActions.add(btnDelete);
        tablePanel.add(tableActions, BorderLayout.SOUTH);
        panel.add(tablePanel, BorderLayout.CENTER);

        JPanel form = UITheme.card(18);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(360, 0));
        JLabel formTitle = UITheme.sectionTitle("Add / Edit Staff");
        formTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(formTitle); form.add(Box.createVerticalStrut(12));

        sfName     = UITheme.textField(20); sfUsername = UITheme.textField(20);
        sfEmail    = UITheme.textField(20); sfPhone    = UITheme.textField(20);
        sfDept     = UITheme.textField(20); sfPassword = UITheme.passwordField(20);
        sfConfirm  = UITheme.passwordField(20);

        for (Object[] r : new Object[][]{
                {"Full Name *", sfName}, {"Username *", sfUsername},
                {"Email *", sfEmail}, {"Phone *", sfPhone}, {"Department *", sfDept},
                {"Password *", sfPassword}, {"Confirm Pwd *", sfConfirm}}) {
            JPanel row = UITheme.formRow((String) r[0], (JComponent) r[1]);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            form.add(row); form.add(Box.createVerticalStrut(6));
        }
        JLabel pwdNote = UITheme.smallLabel("Leave password blank when editing.", UITheme.TEXT_MID);
        pwdNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(pwdNote); form.add(Box.createVerticalStrut(8));

        sfErr = new JLabel(" ");
        sfErr.setFont(UITheme.F_SMALL); sfErr.setForeground(UITheme.DANGER);
        sfErr.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sfErr); form.add(Box.createVerticalStrut(8));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.setOpaque(false); btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnSave = UITheme.primaryBtn("Save"); JButton btnClear = UITheme.secondaryBtn("Clear");
        btnSave.setPreferredSize(new Dimension(100, 32)); btnClear.setPreferredSize(new Dimension(100, 32));
        btnRow.add(btnSave); btnRow.add(btnClear);
        form.add(btnRow);
        panel.add(form, BorderLayout.EAST);

        refreshStaffTable();

        staffTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = staffTable.getSelectedRow();
                if (row >= 0) {
                    String name = (String) staffModel.getValueAt(staffTable.convertRowIndexToModel(row), 0);
                    ds.getStaffList().stream().filter(s -> s.getFullName().equals(name)).findFirst()
                        .ifPresent(found -> {
                            Staff s = (Staff) found;
                            editingStaffId = s.getUserId();
                            sfName.setText(s.getFullName()); sfUsername.setText(s.getUsername());
                            sfEmail.setText(s.getEmail()); sfPhone.setText(s.getPhone());
                            sfDept.setText(s.getDepartment());
                            sfPassword.setText(""); sfConfirm.setText(""); sfErr.setText(" ");
                        });
                }
            }
        });

        btnNew.addActionListener(e    -> clearStaffForm());
        btnClear.addActionListener(e  -> clearStaffForm());
        btnSave.addActionListener(e   -> doSaveStaff());
        btnDelete.addActionListener(e -> doDeleteStaff());
        btnToggle.addActionListener(e -> doToggleStaff());
        return panel;
    }

    private void clearStaffForm() {
        editingStaffId = null;
        sfName.setText(""); sfUsername.setText(""); sfEmail.setText("");
        sfPhone.setText(""); sfDept.setText(""); sfPassword.setText(""); sfConfirm.setText("");
        sfErr.setText(" "); staffTable.clearSelection();
    }

    private void doSaveStaff() {
        String name  = sfName.getText().trim(),  uname = sfUsername.getText().trim();
        String email = sfEmail.getText().trim(),  phone = sfPhone.getText().trim();
        String dept  = sfDept.getText().trim();
        String pwd   = new String(sfPassword.getPassword()), conf = new String(sfConfirm.getPassword());
        String err   = editingStaffId == null
            ? us.addStaff(uname, pwd, name, email, phone, dept)
            : us.updateStaff(editingStaffId, name, email, phone, dept);
        if (err == null && editingStaffId != null && !pwd.isEmpty())
            err = as.resetPassword(editingStaffId, pwd, conf);
        if (err != null) { sfErr.setText(err); return; }
        sfErr.setText(" ");
        MainFrame.showInfo(this, editingStaffId == null ? "Staff added." : "Staff updated.");
        clearStaffForm(); refreshStaffTable();
    }

    private void doDeleteStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) { MainFrame.showError(this, "Select a staff member first."); return; }
        String name = (String) staffModel.getValueAt(staffTable.convertRowIndexToModel(row), 0);
        ds.getStaffList().stream().filter(s -> s.getFullName().equals(name)).findFirst().ifPresent(found -> {
            if (!MainFrame.confirm(this, "Delete staff: " + found.getFullName() + "?")) return;
            String err = us.deleteStaff(found.getUserId());
            if (err != null) { MainFrame.showError(this, err); return; }
            clearStaffForm(); refreshStaffTable();
            MainFrame.showInfo(this, "Staff deleted.");
        });
    }

    private void doToggleStaff() {
        int row = staffTable.getSelectedRow();
        if (row < 0) { MainFrame.showError(this, "Select a staff member first."); return; }
        String name = (String) staffModel.getValueAt(staffTable.convertRowIndexToModel(row), 0);
        ds.getStaffList().stream().filter(s -> s.getFullName().equals(name)).findFirst()
            .ifPresent(s -> { us.toggleStaffActive(s.getUserId()); refreshStaffTable(); });
    }

    private void refreshStaffTable() {
        staffModel.setRowCount(0);
        for (Staff s : ds.getStaffList()) {
            if (s instanceof Manager) continue;
            staffModel.addRow(new Object[]{
                s.getFullName(), s.getUsername(), s.getDepartment(),
                s.getHandledTickets(), s.isActive() ? "Active" : "Inactive"
            });
        }
    }

   
    private JPanel reportBody;

    private JPanel buildReportsTab() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = UITheme.sectionTitle("Monthly Reports & Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p.add(title, BorderLayout.NORTH);

        reportBody = new JPanel();
        reportBody.setLayout(new BoxLayout(reportBody, BoxLayout.Y_AXIS));
        reportBody.setBackground(UITheme.BACKGROUND);

       
        JPanel reportWrapper = new JPanel(new BorderLayout());
        reportWrapper.setBackground(UITheme.BACKGROUND);
        reportWrapper.add(reportBody, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(reportWrapper,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        JScrollBar vsb = scroll.getVerticalScrollBar();
        vsb.setUnitIncrement(30);     
        vsb.setBlockIncrement(120);    


        scroll.getVerticalScrollBar().setUnitIncrement(30);
        scroll.setWheelScrollingEnabled(true);

        SwingUtilities.invokeLater(() -> vsb.setValue(0));
        p.add(scroll, BorderLayout.CENTER);

        JButton btnGen = UITheme.primaryBtn("Generate Report");
        btnGen.setPreferredSize(new Dimension(170, 38));
        JLabel lastGenLabel = UITheme.smallLabel("", UITheme.TEXT_MID);
        btnGen.addActionListener(e -> {
            btnGen.setEnabled(false);
            btnGen.setText("Generating...");
            SwingUtilities.invokeLater(() -> {
                refreshReports();
                String ts2 = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                lastGenLabel.setText("Last generated: " + ts2);
                btnGen.setText("Generate Report");
                btnGen.setEnabled(true);
                JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(
                    JScrollPane.class, reportBody);
                if (sp != null) sp.getVerticalScrollBar().setValue(0);
            });
        });
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(lastGenLabel);
        btnRow.add(btnGen);
        p.add(btnRow, BorderLayout.SOUTH);

        reportBody.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        JLabel placeholder = new JLabel("Click  \"Generate Report\"  to load the latest analytics.");
        placeholder.setFont(UITheme.F_LABEL.deriveFont(Font.ITALIC));
        placeholder.setForeground(UITheme.TEXT_LIGHT);
        placeholder.setAlignmentX(Component.LEFT_ALIGNMENT);
        reportBody.add(placeholder);

        return p;
    }

    private void refreshReports() {
        reportBody.removeAll();
        reportBody.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        long   total    = ts.getAllTickets().size();
        long   pending  = ts.countByStatus(Ticket.Status.PENDING);
        long   inProg   = ts.countByStatus(Ticket.Status.IN_PROGRESS);
        long   resolved = ts.countByStatus(Ticket.Status.RESOLVED);
        long   closed   = ts.countByStatus(Ticket.Status.CLOSED);
        long   monthly  = ts.countThisMonth();
        double avgMin   = ts.avgResponseMinutes();
        long   hiPri    = ts.countByPriority(Ticket.Priority.HIGH);
        long   medPri   = ts.countByPriority(Ticket.Priority.MEDIUM);
        long   lowPri   = ts.countByPriority(Ticket.Priority.LOW);

        addReportCard("Monthly Summary",
            new String[]{"Tickets This Month","Total Tickets","Avg Response Time"},
            new String[]{String.valueOf(monthly), String.valueOf(total),
                avgMin < 60 ? String.format("%.0f min", avgMin) : String.format("%.1f hrs", avgMin / 60)},
            new Color[]{UITheme.INFO, UITheme.NEUTRAL, UITheme.ACCENT});

        addReportCard("Tickets by Status",
            new String[]{"Pending","In Progress","Resolved","Closed"},
            new String[]{String.valueOf(pending),String.valueOf(inProg),String.valueOf(resolved),String.valueOf(closed)},
            new Color[]{UITheme.WARNING, UITheme.ACCENT, UITheme.SUCCESS, UITheme.NEUTRAL});

        addReportCard("Tickets by Priority",
            new String[]{"High Priority","Medium Priority","Low Priority"},
            new String[]{String.valueOf(hiPri),String.valueOf(medPri),String.valueOf(lowPri)},
            new Color[]{UITheme.DANGER, UITheme.WARNING, UITheme.SUCCESS});

        JPanel staffCard = buildTableCard("Staff Performance", new String[]{"Staff Name","Department","Tickets Handled","Status"});
        DefaultTableModel sm = (DefaultTableModel)((JTable)((JViewport)
            ((JScrollPane) staffCard.getComponent(1)).getViewport()).getView()).getModel();
        JTable sTable = (JTable)((JViewport)((JScrollPane) staffCard.getComponent(1)).getViewport()).getView();
        for (Staff s : ds.getStaffList()) {
            if (s instanceof Manager) continue;
            sm.addRow(new Object[]{s.getFullName(), s.getDepartment(), s.getHandledTickets(),
                s.isActive() ? "Active" : "Inactive"});
        }
        reportBody.add(staffCard);
        reportBody.add(Box.createVerticalStrut(14));

        JPanel fbCard = buildTableCard("Customer Feedback & Ratings",
            new String[]{"Ticket ID","Customer","Rating (stars)","Feedback"});
        DefaultTableModel fm = (DefaultTableModel)((JTable)((JViewport)
            ((JScrollPane) fbCard.getComponent(1)).getViewport()).getView()).getModel();
        for (Ticket t : ts.getAllTickets())
            if (t.getRating() > 0)
                fm.addRow(new Object[]{t.getTicketId(), t.getCustomerName(),
                    t.getRating() + " / 5", t.getFeedback()});
        reportBody.add(fbCard);
        reportBody.add(Box.createVerticalStrut(12));

        reportBody.revalidate();
        reportBody.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollPane sp = (JScrollPane) SwingUtilities.getAncestorOfClass(
                JScrollPane.class, reportBody);
            if (sp != null) sp.getVerticalScrollBar().setValue(0);
        });
    }

    private void addReportCard(String title, String[] labels, String[] values, Color[] colors) {
        JPanel card = UITheme.card(14);
        card.setLayout(new BorderLayout(0, 10));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
       
        JLabel sec = UITheme.sectionTitle(title);
        card.add(sec, BorderLayout.NORTH);

        JPanel row = new JPanel(new GridLayout(1, labels.length, 12, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 90));  
        for (int i = 0; i < labels.length; i++)
            row.add(UITheme.statCard(labels[i], values[i], colors[i]));
        card.add(row, BorderLayout.CENTER);

        reportBody.add(card);
        reportBody.add(Box.createVerticalStrut(14));
    }

    private JPanel buildTableCard(String title, String[] columns) {
        JPanel card = UITheme.card(14);
        card.setLayout(new BorderLayout(0, 8));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setPreferredSize(new Dimension(0, 200));

        JLabel lbl = UITheme.sectionTitle(title);
        card.add(lbl, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);  
        card.add(UITheme.scrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void styleTable(JTable t) {
        UITheme.styleTable(t);   

        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        tbl, val, sel, foc, row, col);
                lbl.setBackground(UITheme.PRIMARY);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(UITheme.F_TABLE_H);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(255,255,255,60)),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
                lbl.setOpaque(true);
                return lbl;
            }
        });
        header.setBackground(UITheme.PRIMARY);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.repaint();
    }

    private TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel && v != null) {
                    l.setForeground(UITheme.statusColor(v.toString()));
                    l.setFont(UITheme.F_TABLE.deriveFont(Font.BOLD));
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return l;
            }
        };
    }

    private TableCellRenderer priorityRenderer() {
        return new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel && v != null) l.setForeground(UITheme.priorityColor(v.toString()));
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return l;
            }
        };
    }

  
    private JTextField     tfAnnTitle;
    private JTextArea      taAnnBody;
    private JComboBox<String> cbAnnType;
    private JLabel         lblAnnErr;
    private JPanel         annListPanel;

    private JPanel buildAnnouncementsTab() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(UITheme.BACKGROUND);
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel compose = UITheme.card(18);
        compose.setLayout(new BoxLayout(compose, BoxLayout.Y_AXIS));
        compose.setPreferredSize(new Dimension(360, 0));

        JLabel compTitle = UITheme.sectionTitle("Post New Announcement");
        compTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        compose.add(compTitle); compose.add(Box.createVerticalStrut(14));

        tfAnnTitle = UITheme.textField(25);
        cbAnnType  = UITheme.comboBox(new String[]{"INFO","WARNING","MAINTENANCE","URGENT"});
        cbAnnType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        for (Object[] row : new Object[][]{{"Title *", tfAnnTitle}, {"Type *", cbAnnType}}) {
            JPanel r = UITheme.formRow((String) row[0], (JComponent) row[1]);
            r.setAlignmentX(Component.LEFT_ALIGNMENT); r.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
            compose.add(r); compose.add(Box.createVerticalStrut(10));
        }

        JLabel bodyLbl = UITheme.label("Message Body *"); bodyLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        taAnnBody = UITheme.textArea(6, 28);
        JScrollPane bodyScroll = UITheme.scrollPane(taAnnBody);
        bodyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        compose.add(bodyLbl); compose.add(Box.createVerticalStrut(4)); compose.add(bodyScroll);
        compose.add(Box.createVerticalStrut(10));

        lblAnnErr = new JLabel(" ");
        lblAnnErr.setFont(UITheme.F_SMALL); lblAnnErr.setForeground(UITheme.DANGER);
        lblAnnErr.setAlignmentX(Component.LEFT_ALIGNMENT);
        compose.add(lblAnnErr);

        JButton btnPost = UITheme.primaryBtn("Broadcast Announcement");
        btnPost.setPreferredSize(new Dimension(210, 36));
        btnPost.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPost.addActionListener(e -> doPostAnnouncement());
        compose.add(Box.createVerticalStrut(8)); compose.add(btnPost);
        p.add(compose, BorderLayout.WEST);

        JPanel listPanel = new JPanel(new BorderLayout(0, 8));
        listPanel.setOpaque(false);
        listPanel.add(UITheme.sectionTitle("Active Announcements"), BorderLayout.NORTH);

        annListPanel = new JPanel();
        annListPanel.setLayout(new BoxLayout(annListPanel, BoxLayout.Y_AXIS));
        annListPanel.setBackground(UITheme.BACKGROUND);
        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.setBackground(UITheme.BACKGROUND); listWrapper.add(annListPanel, BorderLayout.NORTH);
        JScrollPane listScroll = UITheme.scrollPane(listWrapper);
        listScroll.getVerticalScrollBar().setUnitIncrement(14);
        listPanel.add(listScroll, BorderLayout.CENTER);
        p.add(listPanel, BorderLayout.CENTER);

        refreshAnnouncements();
        return p;
    }

    private void doPostAnnouncement() {
        String title = tfAnnTitle.getText().trim();
        String body  = taAnnBody.getText().trim();
        String type  = (String) cbAnnType.getSelectedItem();

        if (title.isEmpty()) { lblAnnErr.setText("Title is required."); return; }
        if (title.length() < 5) { lblAnnErr.setText("Title must be at least 5 characters."); return; }
        if (body.isEmpty()) { lblAnnErr.setText("Message body is required."); return; }
        if (body.length() < 10) { lblAnnErr.setText("Message body must be at least 10 characters."); return; }

        Announcement.AnnType annType = Announcement.AnnType.valueOf(type);
        Announcement ann = new Announcement(ds.nextAnnId(), manager.getUserId(),
            manager.getFullName(), title, body, annType);
        ds.addAnnouncement(ann);

        for (Customer c : ds.getCustomers())
            NotificationService.getInstance().notify(c.getUserId(),
                "[Announcement] " + title, "");

        lblAnnErr.setText(" ");
        tfAnnTitle.setText(""); taAnnBody.setText("");
        MainFrame.showInfo(this, "Announcement broadcast to all customers.");
        refreshAnnouncements();
    }

    private void refreshAnnouncements() {
        if (annListPanel == null) return;
        annListPanel.removeAll();
        annListPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        List<Announcement> anns = ds.getAnnouncements();
        if (anns.isEmpty()) {
            JLabel none = UITheme.label("No announcements posted yet.");
            none.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            annListPanel.add(none);
        } else {
            for (int i = anns.size()-1; i >= 0; i--) {
                Announcement a = anns.get(i);
                JPanel card = new JPanel(new BorderLayout(0, 6));
                card.setBackground(UITheme.CARD_BG);
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UITheme.BORDER_COLOR, 1, true),
                    BorderFactory.createCompoundBorder(
                        new MatteBorder(0, 5, 0, 0, a.typeColor()),
                        BorderFactory.createEmptyBorder(10, 12, 10, 12))));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);

                JPanel topRow = new JPanel(new BorderLayout(8, 0)); topRow.setOpaque(false);
                JLabel ttl = new JLabel(a.getTitle());
                ttl.setFont(UITheme.F_LABEL.deriveFont(Font.BOLD)); ttl.setForeground(a.typeColor());
                JLabel typeLbl = new JLabel("[" + a.getType().name() + "]");
                typeLbl.setFont(UITheme.F_SMALL); typeLbl.setForeground(a.typeColor());
                JLabel dateLbl = UITheme.smallLabel(a.getFormattedDate() + "  by " + a.getPostedByName(), UITheme.TEXT_LIGHT);
                JPanel titleRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); titleRight.setOpaque(false);
                titleRight.add(dateLbl);

                JButton btnToggle = a.isActive()
                    ? UITheme.warningBtn("Deactivate")
                    : UITheme.successBtn("Reactivate");
                btnToggle.setPreferredSize(new Dimension(100, 25));
                btnToggle.setFont(UITheme.F_SMALL);
                btnToggle.addActionListener(e -> { a.setActive(!a.isActive()); refreshAnnouncements(); });
                titleRight.add(btnToggle);

                topRow.add(ttl, BorderLayout.WEST); topRow.add(titleRight, BorderLayout.EAST);

                JLabel bodyLbl = new JLabel("<html><body style='width:500px'>" +
                    (a.isActive() ? a.getBody() : "<s>" + a.getBody() + "</s>  [INACTIVE]") + "</body></html>");
                bodyLbl.setFont(UITheme.F_SMALL);
                bodyLbl.setForeground(a.isActive() ? UITheme.TEXT_MID : UITheme.TEXT_LIGHT);

                card.add(topRow, BorderLayout.NORTH); card.add(bodyLbl, BorderLayout.CENTER);
                annListPanel.add(card); annListPanel.add(Box.createVerticalStrut(8));
            }
        }
        annListPanel.revalidate(); annListPanel.repaint();
    }
}
