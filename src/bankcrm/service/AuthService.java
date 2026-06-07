package bankcrm.service;

import bankcrm.model.User;
import bankcrm.util.Validator;

public class AuthService {

    private static final AuthService instance = new AuthService();

    private AuthService() {}

    public static AuthService getInstance() {
        return instance;
    }

    private final DataStore ds = DataStore.getInstance();

    public User login(String username, String password) {
        if (username == null || username.isBlank()) {
            return null;
        }
        if (password == null || password.isBlank()) {
            return null;
        }

        User user = ds.findUserByUsername(username);
        if (user == null) {
            return null;
        }
        if (!user.isActive()) {
            return null;
        }
        if (!user.getPassword().equals(password)) {
            return null;
        }

        return user;
    }

    public String changePassword(String userId, String currentPwd,
                                 String newPwd, String confirmPwd) {
        User user = ds.findUserById(userId);
        if (user == null) {
            return "User not found.";
        }
        if (!user.getPassword().equals(currentPwd)) {
            return "Current password is incorrect.";
        }
        if (!newPwd.equals(confirmPwd)) {
            return "New passwords do not match.";
        }

        String err = Validator.validatePassword(newPwd);
        if (err != null) {
            return err;
        }

        user.setPassword(newPwd);
        return null;
    }

    public String resetPassword(String userId, String newPwd, String confirmPwd) {
        User user = ds.findUserById(userId);
        if (user == null) {
            return "User not found.";
        }
        if (!newPwd.equals(confirmPwd)) {
            return "Passwords do not match.";
        }

        String err = Validator.validatePassword(newPwd);
        if (err != null) {
            return err;
        }

        user.setPassword(newPwd);
        return null;
    }
}