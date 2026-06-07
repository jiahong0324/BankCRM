package bankcrm.service;

import bankcrm.model.*;
import java.util.*;
import java.time.Duration;
import java.time.YearMonth;

public class TicketService {

    private static final TicketService instance = new TicketService();
    private TicketService() {}
    public static TicketService getInstance() {
        return instance;
    }

    private final DataStore           ds = DataStore.getInstance();
    private final NotificationService ns = NotificationService.getInstance();

    public String createTicket(String customerId, String customerName,
                               String categoryStr, String description,
                               String priorityStr) {
        if (description == null || description.isBlank()) {
            return "Description cannot be empty.";
        }
        if (description.length() < 10) {
            return "Description must be at least 10 characters.";
        }

        Ticket.Category category;
        Ticket.Priority priority;
        try {
            category = Ticket.Category.valueOf(categoryStr);
            priority = Ticket.Priority.valueOf(priorityStr);
        } catch (IllegalArgumentException e) {
            return "Invalid category or priority.";
        }

        String id = ds.nextTicketId();
        Ticket t = new Ticket(id, customerId, customerName, category, description, priority);
        t.addHistory(new TicketHistory("Ticket Created", customerId, customerName, ""));
        ds.addTicket(t);

        User u = ds.findUserById(customerId);
        if (u instanceof Customer) {
            ((Customer) u).addTicketId(id);
        }
        return null;
    }

    public Ticket getById(String id) {
        return ds.findTicketById(id);
    }

    public Ticket findTicketOrThrow(String ticketId) throws TicketNotFoundException {
        Ticket t = ds.findTicketById(ticketId);
        if (t == null) {
            throw new TicketNotFoundException(ticketId);
        }
        return t;
    }

    public List<Ticket> getAllTickets() {
        return new ArrayList<>(ds.getTickets());
    }

    public List<Ticket> getTicketsByCustomer(String cid) {
        return ds.getTicketsByCustomerId(cid);
    }

    public Ticket[] getTicketsAsArray() {
        List<Ticket> all = ds.getTickets();
        return all.toArray(new Ticket[0]);
    }

    public String[] getStatusSummaryArray() {
        Ticket.Status[] statuses = Ticket.Status.values();
        String[] summary = new String[statuses.length];
        for (int i = 0; i < statuses.length; i++) {
            Ticket.Status current = statuses[i];
            long count = 0;
            for (Ticket t : ds.getTickets()) {
                if (t.getStatus() == current) {
                    count++;
                }
            }
            summary[i] = current.name() + ":" + count;
        }
        return summary;
    }

    public String[] getTicketIdsForCustomer(String customerId) {
        List<Ticket> tickets = ds.getTicketsByCustomerId(customerId);
        String[] ids = new String[tickets.size()];
        for (int i = 0; i < tickets.size(); i++) {
            ids[i] = tickets.get(i).getTicketId();
        }
        return ids;
    }

    public String respondToTicket(String ticketId, String staffId, String staffName,
                                  String response, String newStatus) {
        Ticket t = ds.findTicketById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }
        if (response == null || response.isBlank()) {
            return "Response cannot be empty.";
        }

        Ticket.Status status;
        try {
            status = Ticket.Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            return "Invalid status.";
        }

        Ticket.Status prev = t.getStatus();
        t.setResponse(response);
        t.setStatus(status);
        t.addHistory(new TicketHistory("Response Added & Status -> " + status,
                staffId, staffName, response));

        if (status == Ticket.Status.RESOLVED && prev != Ticket.Status.RESOLVED) {
            User s = ds.findUserById(staffId);
            if (s instanceof Staff) {
                ((Staff) s).incrementHandledTickets();
            }
        }

        String notifMsg = "Your ticket " + ticketId + " status changed to " + status + ".";
        ns.notify(t.getCustomerId(), notifMsg, ticketId);
        return null;
    }

    public String addInternalRemarks(String ticketId, String staffId,
                                     String staffName, String remarks) {
        Ticket t = ds.findTicketById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }
        if (remarks == null || remarks.isBlank()) {
            return "Remarks cannot be empty.";
        }
        t.setInternalRemarks(remarks);
        t.addHistory(new TicketHistory("Internal Remarks Updated", staffId, staffName, remarks));
        return null;
    }

    public String updateStatus(String ticketId, String staffId, String staffName,
                               String newStatus) {
        Ticket t = ds.findTicketById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }

        Ticket.Status status;
        try {
            status = Ticket.Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            return "Invalid status.";
        }

        Ticket.Status prev = t.getStatus();
        t.setStatus(status);
        t.addHistory(new TicketHistory("Status Changed to " + status, staffId, staffName, ""));

        if (status == Ticket.Status.RESOLVED && prev != Ticket.Status.RESOLVED) {
            User s = ds.findUserById(staffId);
            if (s instanceof Staff) {
                ((Staff) s).incrementHandledTickets();
            }
        }

        String notifMsg = "Your ticket " + ticketId + " has been updated to " + status + ".";
        ns.notify(t.getCustomerId(), notifMsg, ticketId);
        return null;
    }

    public String reopenTicket(String ticketId, String staffId, String staffName) {
        Ticket t = ds.findTicketById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }
        if (t.getStatus() == Ticket.Status.PENDING || t.getStatus() == Ticket.Status.IN_PROGRESS) {
            return "Ticket is not closed/resolved yet.";
        }
        t.setStatus(Ticket.Status.PENDING);
        t.addHistory(new TicketHistory("Ticket Reopened", staffId, staffName, ""));
        String notifMsg = "Your ticket " + ticketId + " has been reopened.";
        ns.notify(t.getCustomerId(), notifMsg, ticketId);
        return null;
    }

    public String assignTicket(String ticketId, String managerId, String managerName,
                               String staffId) {
        Ticket t = ds.findTicketById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }

        User s = ds.findUserById(staffId);
        if (!(s instanceof Staff)) {
            try {
                throw new InvalidUserException("Not a valid staff member", staffId);
            } catch (InvalidUserException e) {
                return e.getMessage();
            }
        }
        if (t.getStatus() == Ticket.Status.CLOSED) {
            return "Cannot assign a closed ticket.";
        }

        t.setAssignedStaffId(staffId);
        t.setAssignedStaffName(s.getFullName());
        if (t.getStatus() == Ticket.Status.PENDING) {
            t.setStatus(Ticket.Status.IN_PROGRESS);
        }
        t.addHistory(new TicketHistory("Assigned to " + s.getFullName(), managerId, managerName, ""));
        String notifMsg = "Your ticket " + ticketId + " has been assigned and is IN_PROGRESS.";
        ns.notify(t.getCustomerId(), notifMsg, ticketId);
        return null;
    }

    public String closeTicket(String ticketId, String customerId, int rating, String feedback) {
        Ticket t = ds.findTicketById(ticketId);
        if (t == null) {
            return "Ticket not found.";
        }
        if (!t.getCustomerId().equals(customerId)) {
            return "Unauthorised.";
        }

        try {
            if (t.getStatus() == Ticket.Status.CLOSED) {
                throw new InvalidTicketOperationException("close", ticketId,
                        "ticket is already closed");
            }
            if (t.getStatus() == Ticket.Status.PENDING || t.getStatus() == Ticket.Status.IN_PROGRESS) {
                throw new InvalidTicketOperationException("close", ticketId,
                        "only RESOLVED tickets can be closed by the customer");
            }
            if (rating < 1 || rating > 5) {
                throw new InvalidTicketOperationException("rate", ticketId,
                        "rating must be between 1 and 5, got: " + rating);
            }
        } catch (InvalidTicketOperationException e) {
            return e.getMessage();
        }

        t.setStatus(Ticket.Status.CLOSED);
        t.setRating(rating);
        String trimmedFeedback = feedback == null ? "" : feedback.trim();
        t.setFeedback(trimmedFeedback);
        t.addHistory(new TicketHistory("Ticket Closed by Customer", customerId, "Customer", ""));
        return null;
    }

    public List<Ticket> searchByKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllTickets();
        }
        String kw = keyword.trim().toLowerCase();
        List<Ticket> result = new ArrayList<>();
        for (Ticket t : ds.getTickets()) {
            boolean matchId   = t.getTicketId().toLowerCase().contains(kw);
            boolean matchName = t.getCustomerName().toLowerCase().contains(kw);
            boolean matchDesc = t.getDescription().toLowerCase().contains(kw);
            if (matchId || matchName || matchDesc) {
                result.add(t);
            }
        }
        return result;
    }

    public List<Ticket> filterTickets(String status, String priority, String category) {
        List<Ticket> result = new ArrayList<>();
        for (Ticket t : ds.getTickets()) {
            if (!status.equals("ALL") && !t.getStatus().name().equals(status)) continue;
            if (!priority.equals("ALL") && !t.getPriority().name().equals(priority)) continue;
            if (!category.equals("ALL") && !t.getCategory().name().equals(category)) continue;
            result.add(t);
        }
        return result;
    }

    public List<Ticket> sortTickets(List<Ticket> list, String sortBy) {
        List<Ticket> sorted = new ArrayList<>(list);
        switch (sortBy) {
            case "Date Asc":
                sorted.sort(Comparator.comparing(Ticket::getCreatedAt));
                break;
            case "Priority":
                sorted.sort(Comparator.comparingInt(t -> t.getPriority().ordinal()));
                break;
            case "Priority Desc":
                sorted.sort((a, b) -> Integer.compare(b.getPriority().ordinal(), a.getPriority().ordinal()));
                break;
            default:
                sorted.sort(Comparator.comparing(Ticket::getCreatedAt).reversed());
                break;
        }
        return sorted;
    }

    public long countByStatus(Ticket.Status s) {
        long count = 0;
        for (Ticket t : ds.getTickets()) {
            if (t.getStatus() == s) {
                count++;
            }
        }
        return count;
    }

    public long countByPriority(Ticket.Priority p) {
        long count = 0;
        for (Ticket t : ds.getTickets()) {
            if (t.getPriority() == p) {
                count++;
            }
        }
        return count;
    }

    public double avgResponseMinutes() {
        List<Ticket> tickets = ds.getTickets();
        long total = 0;
        int count = 0;
        for (Ticket t : tickets) {
            if (t.getStatus() == Ticket.Status.RESOLVED || t.getStatus() == Ticket.Status.CLOSED) {
                total += Duration.between(t.getCreatedAt(), t.getUpdatedAt()).toMinutes();
                count++;
            }
        }
        if (count == 0) {
            return 0;
        }
        return (double) total / count;
    }

    public long countThisMonth() {
        YearMonth now = YearMonth.now();
        long count = 0;
        for (Ticket t : ds.getTickets()) {
            if (YearMonth.from(t.getCreatedAt()).equals(now)) {
                count++;
            }
        }
        return count;
    }
}