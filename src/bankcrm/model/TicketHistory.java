package bankcrm.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TicketHistory {

    private String action;
    private String performedById;
    private String performedByName;
    private String notes;
    private LocalDateTime timestamp;
    
    public TicketHistory() {
        this.action = "";
        this.performedById = "";
        this.performedByName = "";
        this.notes = "";
        this.timestamp = LocalDateTime.now();
    }

    public TicketHistory(String action, String performedById,
                         String performedByName, String notes) {
        this.action = action;
        this.performedById = performedById;
        this.performedByName = performedByName;
        this.notes = notes;
        this.timestamp = LocalDateTime.now();
    }

    public String getAction() {
        return action;
    }

    public String getPerformedById() {
        return performedById;
    }

    public String getPerformedByName() {
        return performedByName;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    @Override
    public String toString() {
        String result = "[" + getFormattedTimestamp() + "]  " + action + "  by  " + performedByName;
        if (!notes.isEmpty()) {
            result += "\n    → " + notes;
        }
        return result;
    }
}