package bankcrm.model;

public class InvalidTicketOperationException extends Exception {

    private String ticketId;
    private String operation;
    
    public InvalidTicketOperationException() {
        super();
        this.ticketId = "";
        this.operation = "";
    }

    public InvalidTicketOperationException(String operation, String ticketId, String reason) {
        super("Cannot perform '" + operation + "' on ticket " + ticketId + ": " + reason);
        this.ticketId = ticketId;
        this.operation = operation;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getOperation() {
        return operation;
    }
}