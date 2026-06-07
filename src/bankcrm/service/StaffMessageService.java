package bankcrm.service;

import bankcrm.model.StaffMessage;
import bankcrm.model.Ticket;
import bankcrm.model.TicketHistory;

import java.util.List;

public class StaffMessageService {

    private static final StaffMessageService instance = new StaffMessageService();

    private StaffMessageService() {}

    public static StaffMessageService getInstance() {
        return instance;
    }

    private final DataStore ds = DataStore.getInstance();

    public List<StaffMessage> getMessages(String ticketId) {
        return ds.getMessagesForTicket(ticketId);
    }

    public String send(String ticketId, String senderId, String senderName, String message) {
        if (message == null || message.isBlank()) {
            return "Message cannot be empty.";
        }

        Ticket t = ds.findTicketById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }

        String trimmed = message.trim();
        StaffMessage msg = new StaffMessage(ds.nextMsgId(), ticketId, senderId, senderName, trimmed);
        ds.addStaffMessage(msg);
        t.addStaffMessage(msg);

        String historyNote = "[STAFF ONLY] " + trimmed;
        t.addHistory(new TicketHistory("Internal Staff Message", senderId, senderName, historyNote));

        return null;
    }
}