package bankcrm.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StaffMessage {

    private String id;
    private String ticketId;
    private String senderId;
    private String senderName;
    private String message;
    private LocalDateTime sentAt;
    
    public StaffMessage() {
        this.id = "";
        this.ticketId = "";
        this.senderId = "";
        this.senderName = "";
        this.message = "";
        this.sentAt = LocalDateTime.now();
    }

    public StaffMessage(String id, String ticketId,
                        String senderId, String senderName, String message) {
        this.id = id;
        this.ticketId = ticketId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
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