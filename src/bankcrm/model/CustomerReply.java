package bankcrm.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomerReply {

    private String id;
    private String ticketId;
    private String customerId;
    private String customerName;
    private String message;
    private LocalDateTime sentAt;
    
    public CustomerReply() {
        this.id = "";
        this.ticketId = "";
        this.customerId = "";
        this.customerName = "";
        this.message = "";
        this.sentAt = LocalDateTime.now();
    }

    public CustomerReply(String id, String ticketId,
                         String customerId, String customerName, String message) {
        this.id = id;
        this.ticketId = ticketId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public String getFormattedTime() {
        return sentAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}