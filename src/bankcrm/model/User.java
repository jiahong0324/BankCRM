package bankcrm.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class User {

    protected String userId;
    protected String username;
    protected String password;
    protected String fullName;
    protected String email;
    protected String phone;
    protected LocalDateTime createdAt;
    protected boolean active;

    public User(String userId, String username, String password,
                String fullName, String email, String phone) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public abstract String getRole();

    public abstract String getDisplayInfo();

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}