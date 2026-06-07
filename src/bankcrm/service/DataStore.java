package bankcrm.service;

import bankcrm.model.*;

import java.util.ArrayList;
import java.util.List;

public class DataStore {

    private static DataStore instance;

    private final List<User> users = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();
    private final List<Notification> notifications = new ArrayList<>();
    private final List<FAQItem> faqItems = new ArrayList<>();
    private final List<CannedResponse> cannedResponses = new ArrayList<>();
    private final List<Announcement> announcements = new ArrayList<>();
    private final List<StaffMessage> staffMessages = new ArrayList<>();

    private int ticketCounter = 1;
    private int userCounter = 100;
    private int notifCounter = 1;
    private int faqCounter = 1;
    private int cannedCounter = 1;
    private int annCounter = 1;
    private int msgCounter = 1;
    private int replyCounter = 1;

    private DataStore() {
        initSampleData();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public String nextUserId() {
        return "U" + String.format("%03d", userCounter++);
    }

    public String nextNotifId() {
        return "N" + String.format("%04d", notifCounter++);
    }

    public String nextFaqId() {
        return "FAQ" + String.format("%03d", faqCounter++);
    }

    public String nextCannedId() {
        return "CR" + String.format("%03d", cannedCounter++);
    }

    public String nextAnnId() {
        return "ANN" + String.format("%03d", annCounter++);
    }

    public String nextMsgId() {
        return "MSG" + String.format("%04d", msgCounter++);
    }

    public String nextReplyId() {
        return "RPL" + String.format("%04d", replyCounter++);
    }

    public String nextTicketId() {
        return "TKT-" + java.time.LocalDate.now().getYear()
             + "-" + String.format("%05d", ticketCounter++);
    }

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User u) {
        users.add(u);
    }

    public boolean removeUser(String id) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(id)) {
                users.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean usernameExists(String username) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }

    public User findUserByUsername(String username) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }

    public User findUserById(String id) {
        for (User u : users) {
            if (u.getUserId().equals(id)) {
                return u;
            }
        }
        return null;
    }

    public List<Staff> getStaffList() {
        List<Staff> list = new ArrayList<>();
        for (User u : users) {
            if (u instanceof Staff) {
                list.add((Staff) u);
            }
        }
        return list;
    }

    public List<Customer> getCustomers() {
        List<Customer> list = new ArrayList<>();
        for (User u : users) {
            if (u instanceof Customer) {
                list.add((Customer) u);
            }
        }
        return list;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void addTicket(Ticket t) {
        tickets.add(t);
    }

    public Ticket findTicketById(String id) {
        for (Ticket t : tickets) {
            if (t.getTicketId().equals(id)) {
                return t;
            }
        }
        return null;
    }

    public List<Ticket> getTicketsByCustomerId(String cid) {
        List<Ticket> list = new ArrayList<>();
        for (Ticket t : tickets) {
            if (t.getCustomerId().equals(cid)) {
                list.add(t);
            }
        }
        return list;
    }

    public List<Ticket> getTicketsByStaffId(String sid) {
        List<Ticket> list = new ArrayList<>();
        for (Ticket t : tickets) {
            if (sid.equals(t.getAssignedStaffId())) {
                list.add(t);
            }
        }
        return list;
    }

    public List<Notification> getNotificationsForUser(String uid) {
        List<Notification> list = new ArrayList<>();
        for (Notification n : notifications) {
            if (n.getUserId().equals(uid)) {
                list.add(n);
            }
        }
        return list;
    }

    public void addNotification(Notification n) {
        notifications.add(n);
    }

    public int unreadCount(String uid) {
        int count = 0;
        for (Notification n : notifications) {
            if (n.getUserId().equals(uid) && !n.isRead()) {
                count++;
            }
        }
        return count;
    }

    public List<FAQItem> getFaqItems() {
        return faqItems;
    }

    public void addFaqItem(FAQItem f) {
        faqItems.add(f);
    }

    public FAQItem findFaqById(String id) {
        for (FAQItem f : faqItems) {
            if (f.getId().equals(id)) {
                return f;
            }
        }
        return null;
    }

    public boolean removeFaq(String id) {
        for (int i = 0; i < faqItems.size(); i++) {
            if (faqItems.get(i).getId().equals(id)) {
                faqItems.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<CannedResponse> getCannedResponses() {
        return cannedResponses;
    }

    public void addCannedResponse(CannedResponse cr) {
        cannedResponses.add(cr);
    }

    public boolean removeCannedResponse(String id) {
        for (int i = 0; i < cannedResponses.size(); i++) {
            if (cannedResponses.get(i).getId().equals(id)) {
                cannedResponses.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<Announcement> getAnnouncements() {
        return announcements;
    }

    public List<Announcement> getActiveAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        for (Announcement a : announcements) {
            if (a.isActive()) {
                list.add(a);
            }
        }
        return list;
    }

    public void addAnnouncement(Announcement a) {
        announcements.add(a);
    }

    public boolean removeAnnouncement(String id) {
        for (int i = 0; i < announcements.size(); i++) {
            if (announcements.get(i).getId().equals(id)) {
                announcements.remove(i);
                return true;
            }
        }
        return false;
    }

    public void addStaffMessage(StaffMessage m) {
        staffMessages.add(m);
    }

    public List<StaffMessage> getMessagesForTicket(String ticketId) {
        List<StaffMessage> list = new ArrayList<>();
        for (StaffMessage m : staffMessages) {
            if (m.getTicketId().equals(ticketId)) {
                list.add(m);
            }
        }
        return list;
    }

    private void initSampleData() {

        Manager mgr = new Manager("U001", "manager", "BankManager@324", "Zhi Bin", "zhibin@securebank.com", "0123456789", "Management", "STF001", "Senior Manager");
        users.add(mgr);
        Staff s1 = new Staff("U002", "staff1", "Staff1@324", "Jia Hong", "jiahong@securebank.com", "0123456780", "Customer Support", "STF002");
        users.add(s1);
        Staff s2 = new Staff("U003", "staff2", "Staff2@324", "Bob Williams", "bob@securebank.com", "0123456781", "Loan Department", "STF003");
        users.add(s2);
        Customer c1 = new Customer("U004", "customer1", "Customer1@324", "Qiu Yong", "qiuyong@email.com", "0123456782", "123 Main Street, KL", "ACC-0001",
            Customer.generateMaskedCard("ACC-0001"), 12500.00, "SAVINGS");
        users.add(c1);
        Customer c2 = new Customer("U005", "customer2", "Customer2@324", "Christine", "christine@email.com", "0123456783", "456 Park Ave, PJ", "ACC-0002",
            Customer.generateMaskedCard("ACC-0002"), 38750.50, "CURRENT");
        users.add(c2);

        Ticket t1 = new Ticket("TKT-2026-00001", "U004", "Qiu Yong", Ticket.Category.ACCOUNT, "Unable to access my online banking account. I keep getting an 'Invalid credentials' error even though my password is correct.", Ticket.Priority.HIGH);
        t1.addHistory(new TicketHistory("Ticket Created", "U004", "Qiu Yong", ""));
        tickets.add(t1);
        c1.addTicketId(t1.getTicketId());

        Ticket t2 = new Ticket("TKT-2026-00002", "U005", "Christine", Ticket.Category.LOAN, "I applied for a personal loan two weeks ago but have not received any update on my application status.", Ticket.Priority.MEDIUM);
        t2.setAssignedStaffId("U002");
        t2.setAssignedStaffName("Jia Hong");
        t2.setStatus(Ticket.Status.RESOLVED);
        t2.setResponse("Your loan application has been reviewed and approved. Our loan officer will contact you within 2 business days.");
        t2.addHistory(new TicketHistory("Ticket Created", "U005", "Christine", ""));
        t2.addHistory(new TicketHistory("Assigned to Staff", "U001", "Zhi Bin", "Assigned to Jia Hong"));
        t2.addHistory(new TicketHistory("Status Changed to IN_PROGRESS", "U002", "Jia Hong", ""));
        t2.addHistory(new TicketHistory("Response Added", "U002", "Jia Hong", "Loan approved, contact within 2 days"));
        t2.addHistory(new TicketHistory("Status Changed to RESOLVED", "U002", "Jia Hong", ""));
        t2.addCustomerReply(new CustomerReply("RPL0001", "TKT-2026-00002", "U005", "Christine", "Thank you! When exactly will the loan officer call me?"));
        t2.setRating(4);
        t2.setFeedback("Very helpful and quick response. Thank you!");
        tickets.add(t2);
        c2.addTicketId(t2.getTicketId());
        s1.incrementHandledTickets();

        Ticket t3 = new Ticket("TKT-2026-00003", "U004", "Qiu Yong", Ticket.Category.CARD, "My debit card was declined at the ATM even though I have sufficient balance.", Ticket.Priority.LOW);
        t3.setAssignedStaffId("U003");
        t3.setAssignedStaffName("Bob Williams");
        t3.setStatus(Ticket.Status.IN_PROGRESS);
        t3.setResponse("We are currently investigating your card issue with our operations team.");
        t3.setInternalRemarks("Card may be blocked due to suspicious activity. Need to verify with card operations team.");
        t3.addHistory(new TicketHistory("Ticket Created", "U004", "Qiu Yong", ""));
        t3.addHistory(new TicketHistory("Assigned to Staff", "U001", "Zhi Bin", "Assigned to Bob Williams"));
        t3.addHistory(new TicketHistory("Status Changed to IN_PROGRESS", "U003", "Bob Williams", ""));
        t3.addHistory(new TicketHistory("Response Added", "U003", "Bob Williams", "Investigating card issue with operations team"));
        t3.addCustomerReply(new CustomerReply("RPL0002", "TKT-2026-00003", "U004", "Qiu Yong", "It is very urgent, I need the card for an important payment tomorrow."));
        tickets.add(t3);
        c1.addTicketId(t3.getTicketId());
        s2.incrementHandledTickets();

        Ticket t4 = new Ticket("TKT-2026-00004", "U005", "Christine", Ticket.Category.TRANSACTION, "I notice an unauthorised transaction of RM 250 on 03/04/2026. I did not make this transaction.", Ticket.Priority.HIGH);
        t4.addHistory(new TicketHistory("Ticket Created", "U005", "Christine", ""));
        tickets.add(t4);
        c2.addTicketId(t4.getTicketId());

        ticketCounter = 5;
        userCounter = 6;
        replyCounter = 3;

        staffMessages.add(new StaffMessage("MSG0001", "TKT-2026-00003", "U003", "Bob Williams", "Jia Hong, have you dealt with a card block case like this before? The customer says it is urgent."));
        staffMessages.add(new StaffMessage("MSG0002", "TKT-2026-00003", "U002", "Jia Hong", "Yes — contact card ops on extension 204. They can unblock within the hour if no fraud is confirmed."));
        msgCounter = 3;

        notifications.add(new Notification("N001", "U004", "Your ticket TKT-2026-00003 has been updated to IN_PROGRESS.", "TKT-2026-00003"));
        notifications.add(new Notification("N002", "U005", "Your ticket TKT-2026-00002 has been RESOLVED. Please close it when satisfied.", "TKT-2026-00002"));
        notifCounter = 3;

        faqItems.add(new FAQItem("FAQ001", FAQItem.FAQCategory.ACCOUNT, "How do I reset my online banking password?", "You can reset your password by clicking 'Forgot Password' on the login page. A reset link will be sent to your registered email address. If you do not receive the email within 5 minutes, check your spam folder or contact support."));
        faqItems.add(new FAQItem("FAQ002", FAQItem.FAQCategory.ACCOUNT, "How do I update my registered email or phone number?", "Log in to online banking, go to Profile Settings > Contact Details, and update your information. Changes take effect immediately after saving. You may need to verify the new contact via OTP."));
        faqItems.add(new FAQItem("FAQ003", FAQItem.FAQCategory.ACCOUNT, "Why is my account temporarily locked?", "Accounts are automatically locked after 5 consecutive failed login attempts as a security measure. Please wait 30 minutes or contact support to unlock your account manually."));
        faqItems.add(new FAQItem("FAQ004", FAQItem.FAQCategory.CARD, "How do I activate a new debit or credit card?", "You can activate your card via online banking under Cards > Activate Card, through our mobile app, at any ATM using your PIN, or by calling our 24-hour card activation line at 1-800-XXX-XXXX."));
        faqItems.add(new FAQItem("FAQ005", FAQItem.FAQCategory.CARD, "What should I do if my card is lost or stolen?", "Immediately block your card via online banking (Cards > Block Card) or call our 24-hour hotline. We will issue a replacement card within 5–7 business days. Any fraudulent transactions should be reported within 30 days."));
        faqItems.add(new FAQItem("FAQ006", FAQItem.FAQCategory.CARD, "Why was my card declined even though I have sufficient funds?", "Card declines can occur due to: incorrect PIN entry, international transactions not enabled, daily limit exceeded, or a temporary security hold. Log in to check your card status or contact support."));
        faqItems.add(new FAQItem("FAQ007", FAQItem.FAQCategory.LOAN, "How long does a loan application take to process?", "Personal loan applications are typically processed within 3–5 business days. Home loan applications may take up to 14 business days. You will receive an SMS/email update at each stage of the process."));
        faqItems.add(new FAQItem("FAQ008", FAQItem.FAQCategory.LOAN, "What documents are required for a personal loan application?", "You will need: a copy of your NRIC/passport, latest 3 months' payslips, latest 6 months' bank statements, and your EPF statement. Self-employed applicants require additional business documents."));
        faqItems.add(new FAQItem("FAQ009", FAQItem.FAQCategory.LOAN, "Can I make early repayment on my loan?", "Yes. Early repayment is allowed without penalty for personal loans. For home loans, an early settlement fee of 2% may apply within the first 5 years. Contact our loan department for an exact settlement amount."));
        faqItems.add(new FAQItem("FAQ010", FAQItem.FAQCategory.TRANSACTION, "What is the daily transaction limit for online transfers?", "The default daily limit is RM 10,000 for interbank transfers and RM 50,000 for own-account transfers. You can request a temporary limit increase via online banking or at any branch."));
        faqItems.add(new FAQItem("FAQ011", FAQItem.FAQCategory.TRANSACTION, "How long does an interbank transfer take?", "Transfers via IBG (Interbank GIRO) take up to 1 business day. Instant Transfer (IBFT) transactions are processed within seconds. Both services are available 24/7 through online banking."));
        faqItems.add(new FAQItem("FAQ012", FAQItem.FAQCategory.TRANSACTION, "What should I do if I suspect an unauthorised transaction?", "Report it immediately via online banking (Transactions > Dispute) or call our fraud hotline. Freeze your account if necessary. All disputes must be reported within 30 days of the transaction date."));
        faqItems.add(new FAQItem("FAQ013", FAQItem.FAQCategory.SECURITY, "How do I protect my account from phishing scams?", "SecureBank will NEVER ask for your full password or TAC code via email or phone. Always access online banking by typing our URL directly. Enable login notifications and use a strong, unique password."));
        faqItems.add(new FAQItem("FAQ014", FAQItem.FAQCategory.SECURITY, "What is a TAC code and when do I need it?", "A Transaction Authorisation Code (TAC) is a one-time password sent via SMS to your registered number. It is required for high-value transfers, password changes, and adding new payees. Each TAC expires within 5 minutes."));
        faqItems.add(new FAQItem("FAQ015", FAQItem.FAQCategory.GENERAL, "What are the branch operating hours?", "Most branches are open Monday–Friday 9:30am–4:30pm and Saturday 9:30am–12:30pm. Some branches have extended hours. Use our Branch Locator at securebank.com for specific hours and locations."));
        faqCounter = 16;

        cannedResponses.add(new CannedResponse("CR001", "Account Login Issue", "Thank you for contacting SecureBank support. I understand you are experiencing login difficulties. Please try clearing your browser cache and cookies, then attempt to log in again. If the issue persists, I can arrange for a password reset link to be sent to your registered email address.", "ACCOUNT"));
        cannedResponses.add(new CannedResponse("CR002", "Card Blocked — Verify Identity", "Thank you for reporting this issue. For your security, your card may have been temporarily blocked due to suspicious activity detection. Please verify your identity by confirming your registered phone number and the last 4 digits of your IC/passport so we can proceed with unblocking your card.", "CARD"));
        cannedResponses.add(new CannedResponse("CR003", "Loan Status Update", "Thank you for your patience. I have reviewed your loan application and can confirm it is currently under assessment by our credit team. You will receive a formal decision via email and SMS within 2–3 business days. Please ensure your contact details are up to date.", "LOAN"));
        cannedResponses.add(new CannedResponse("CR004", "Dispute Transaction — Acknowledge", "We take unauthorised transactions very seriously. I have flagged your case with our fraud investigation team. Please be assured that we will conduct a thorough investigation. A provisional credit may be applied to your account within 5 business days while the investigation is ongoing.", "TRANSACTION"));
        cannedResponses.add(new CannedResponse("CR005", "Issue Resolved — Closing", "I am pleased to inform you that your reported issue has now been fully resolved. Please do not hesitate to contact us again if you experience any further difficulties. Thank you for choosing SecureBank.", "GENERAL"));
        cannedResponses.add(new CannedResponse("CR006", "Request More Information", "To assist you effectively, we require some additional information. Could you please provide: (1) your account number, (2) the date the issue occurred, and (3) any error messages received? This will help us resolve your issue as quickly as possible.", "GENERAL"));
        cannedResponses.add(new CannedResponse("CR007", "Transfer Limit Increase", "Thank you for your request. A temporary increase to your daily transfer limit can be approved for up to RM 50,000 for a period of 48 hours. This has been processed and will be effective immediately. Please remember that normal limits will resume automatically after 48 hours.", "TRANSACTION"));
        cannedResponses.add(new CannedResponse("CR008", "Security Alert — Change Password", "We have detected unusual login activity on your account as a security precaution. We strongly recommend that you change your online banking password immediately and ensure your registered mobile number is current. If you did not initiate any unusual activity, please contact us urgently.", "ACCOUNT"));
        cannedCounter = 9;

        announcements.add(new Announcement("ANN001", "U001", "Zhi Bin", "System Maintenance — 10 April 2026", "SecureBank online banking will undergo scheduled maintenance on Saturday, 10 April 2026, from 12:00 AM to 4:00 AM. All online services including transfers, payments, and account enquiries will be temporarily unavailable. We apologise for any inconvenience. ATM services will remain available throughout.", Announcement.AnnType.MAINTENANCE));
        announcements.add(new Announcement("ANN002", "U001", "Zhi Bin", "New Feature: Instant Loan Top-Up Now Available", "We are pleased to announce that existing loan customers can now apply for an instant top-up loan directly through online banking. Simply log in and navigate to Loans > Apply for Top-Up. Approvals are processed within minutes for eligible customers.", Announcement.AnnType.INFO));
        annCounter = 3;
    }
}