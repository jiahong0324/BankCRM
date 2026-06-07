package bankcrm.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {

    private String notificationId;
    private String userId;
    private String message;
    private String ticketId;
    private boolean read;
    private LocalDateTime createdAt;
    
    public Notification() {
        this.notificationId = "";
        this.userId = "";
        this.message = "";
        this.ticketId = "";
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(String notificationId, String userId,
                        String message, String ticketId) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.ticketId = ticketId;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public String getTicketId() {
        return ticketId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}