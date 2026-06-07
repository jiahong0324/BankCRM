package bankcrm.model;

public class TicketNotFoundException extends Exception {

    private String ticketId;
    
    public TicketNotFoundException() {
        super();
        this.ticketId = "";
    }

    public TicketNotFoundException(String ticketId) {
        super("Ticket not found: " + ticketId);
        this.ticketId = ticketId;
    }

    public String getTicketId() {
        return ticketId;
    }
}