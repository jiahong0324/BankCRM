package bankcrm.model;

public class Manager extends Staff {

    private String managerLevel;
    
    public Manager() {
        super("", "", "", "", "", "", "", "");
        this.managerLevel = "";
    }

    public Manager(String userId, String username, String password,
                   String fullName, String email, String phone,
                   String department, String staffId, String managerLevel) {
        super(userId, username, password, fullName, email, phone, department, staffId);
        this.managerLevel = managerLevel;
    }

    @Override
    public String getRole() {
        return "MANAGER";
    }

    @Override
    public String getDisplayInfo() {
        return "Manager: " + fullName + "  |  " + managerLevel;
    }

    public String getManagerLevel() {
        return managerLevel;
    }

    public void setManagerLevel(String level) {
        this.managerLevel = level;
    }
}