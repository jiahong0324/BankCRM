package bankcrm.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Ticket {

    public enum Category { ACCOUNT, CARD, LOAN, TRANSACTION, GENERAL }
    public enum Priority { LOW, MEDIUM, HIGH }
    public enum Status { PENDING, IN_PROGRESS, RESOLVED, CLOSED }

    private String ticketId;
    private String customerId;
    private String customerName;
    private String assignedStaffId;
    private String assignedStaffName;
    private Category category;
    private String description;
    private Priority priority;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TicketHistory> history;
    private List<StaffMessage> staffMessages;
    private List<CustomerReply> customerReplies;
    private String response;
    private String internalRemarks;
    private int rating;
    private String feedback;
    
    public Ticket() {
        this.ticketId = "";
        this.customerId = "";
        this.customerName = "";
        this.category = Category.GENERAL;
        this.description = "";
        this.priority = Priority.LOW;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.history = new ArrayList<>();
        this.staffMessages = new ArrayList<>();
        this.customerReplies = new ArrayList<>();
        this.response = "";
        this.internalRemarks = "";
        this.rating = 0;
        this.feedback = "";
    }

    public Ticket(String ticketId, String customerId, String customerName,
                  Category category, String description, Priority priority) {
        this.ticketId = ticketId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.category = category;
        this.description = description;
        this.priority = priority;
        this.status = Status.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.history = new ArrayList<>();
        this.staffMessages = new ArrayList<>();
        this.customerReplies = new ArrayList<>();
        this.response = "";
        this.internalRemarks = "";
        this.rating = 0;
        this.feedback = "";
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setAssignedStaffId(String assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
        touch();
    }

    public void setAssignedStaffName(String assignedStaffName) {
        this.assignedStaffName = assignedStaffName;
        touch();
    }

    public void setCategory(Category category) {
        this.category = category;
        touch();
    }

    public void setDescription(String description) {
        this.description = description;
        touch();
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
        touch();
    }

    public void setStatus(Status status) {
        this.status = status;
        touch();
    }

    public void setResponse(String response) {
        this.response = response;
        touch();
    }

    public void setInternalRemarks(String internalRemarks) {
        this.internalRemarks = internalRemarks;
        touch();
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void addHistory(TicketHistory h) {
        this.history.add(h);
    }

    public void addStaffMessage(StaffMessage m) {
        this.staffMessages.add(m);
    }

    public void addCustomerReply(CustomerReply r) {
        this.customerReplies.add(r);
        touch();
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

    public String getAssignedStaffId() {
        return assignedStaffId;
    }

    public String getAssignedStaffName() {
        return assignedStaffName;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Priority getPriority() {
        return priority;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<TicketHistory> getHistory() {
        return history;
    }

    public List<StaffMessage> getStaffMessages() {
        return staffMessages;
    }

    public List<CustomerReply> getCustomerReplies() {
        return customerReplies;
    }

    public String getResponse() {
        return response;
    }

    public String getInternalRemarks() {
        return internalRemarks;
    }

    public int getRating() {
        return rating;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getFormattedUpdatedAt() {
        return updatedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getStarsDisplay() {
        if (rating == 0) {
            return "Not Rated";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < rating ? "★" : "☆");
        }
        return sb.toString();
    }
}