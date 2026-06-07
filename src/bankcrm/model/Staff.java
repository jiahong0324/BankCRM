package bankcrm.model;

public class Staff extends User {

    private String department;
    private String staffId;
    private int handledTickets;
    
    public Staff() {
        super("", "", "", "", "", "");
        this.department = "";
        this.staffId = "";
        this.handledTickets = 0;
    }


    public Staff(String userId, String username, String password,
                 String fullName, String email, String phone,
                 String department, String staffId) {
        super(userId, username, password, fullName, email, phone);
        this.department = department;
        this.staffId = staffId;
        this.handledTickets = 0;
    }

    @Override
    public String getRole() {
        return "STAFF";
    }

    @Override
    public String getDisplayInfo() {
        return "Staff: " + fullName
             + "  |  Dept: " + department
             + "  |  Handled: " + handledTickets;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getStaffId() {
        return staffId;
    }

    public int getHandledTickets() {
        return handledTickets;
    }

    public void setHandledTickets(int n) {
        this.handledTickets = n;
    }

    public void incrementHandledTickets() {
        this.handledTickets++;
    }
}