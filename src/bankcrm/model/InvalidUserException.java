package bankcrm.model;

public class InvalidUserException extends Exception {

    private String userId;
    
    public InvalidUserException() {
        super();
        this.userId = "";
    }

    public InvalidUserException(String message, String userId) {
        super(message + " (userId: " + userId + ")");
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}