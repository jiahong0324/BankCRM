package bankcrm.service;

import bankcrm.model.*;
import bankcrm.util.Validator;
import java.util.List;

public class UserService {

    private static final UserService instance = new UserService();
    private UserService() {}
    public static UserService getInstance() {
        return instance;
    }

    private final DataStore ds = DataStore.getInstance();

    public String[] getCustomerNamesArray() {
        List<Customer> customers = ds.getCustomers();
        String[] names = new String[customers.size()];
        for (int i = 0; i < customers.size(); i++) {
            names[i] = customers.get(i).getFullName();
        }
        return names;
    }

    public String[] getAllUserIdsArray() {
        List<User> users = ds.getUsers();
        String[] ids = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            ids[i] = users.get(i).getUserId();
        }
        return ids;
    }

    public String registerCustomer(String username, String password, String confirm,
                                   String fullName, String email, String phone,
                                   String address) {
        String err;
        if ((err = Validator.validateUsername(username)) != null) {
            return err;
        }
        if (ds.usernameExists(username)) {
            return "Username already taken.";
        }
        if ((err = Validator.validatePassword(password)) != null) {
            return err;
        }
        if (!password.equals(confirm)) {
            return "Passwords do not match.";
        }
        if ((err = Validator.validateFullName(fullName)) != null) {
            return err;
        }
        if ((err = Validator.validateEmail(email)) != null) {
            return err;
        }
        if ((err = Validator.validatePhone(phone)) != null) {
            return err;
        }
        if (address == null || address.isBlank()) {
            return "Address is required.";
        }

        String uid = ds.nextUserId();
        String accNo = "ACC-" + String.format("%04d", Integer.parseInt(uid.substring(1)));
        String cardNo = bankcrm.model.Customer.generateMaskedCard(accNo);
        double balance = 1000 + Math.round(Math.random() * 490) * 100.0;
        String accType = (Math.random() < 0.6) ? "SAVINGS" : "CURRENT";
        ds.addUser(new bankcrm.model.Customer(uid, username, password, fullName, email, phone,
                address, accNo, cardNo, balance, accType));
        return null;
    }

    public String updateCustomerProfile(String userId, String fullName,
                                        String email, String phone, String address) {
        User u = ds.findUserById(userId);
        if (!(u instanceof Customer)) {
            return "User not found.";
        }
        String err;
        if ((err = Validator.validateFullName(fullName)) != null) {
            return err;
        }
        if ((err = Validator.validateEmail(email)) != null) {
            return err;
        }
        if ((err = Validator.validatePhone(phone)) != null) {
            return err;
        }
        if (address == null || address.isBlank()) {
            return "Address is required.";
        }
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(phone);
        ((Customer) u).setAddress(address);
        return null;
    }

    public String addStaff(String username, String password, String fullName,
                           String email, String phone, String department) {
        String err;
        if ((err = Validator.validateUsername(username)) != null) {
            return err;
        }
        if (ds.usernameExists(username)) {
            return "Username already taken.";
        }
        if ((err = Validator.validatePassword(password)) != null) {
            return err;
        }
        if ((err = Validator.validateFullName(fullName)) != null) {
            return err;
        }
        if ((err = Validator.validateEmail(email)) != null) {
            return err;
        }
        if ((err = Validator.validatePhone(phone)) != null) {
            return err;
        }
        if (department == null || department.isBlank()) {
            return "Department is required.";
        }

        String uid = ds.nextUserId();
        String staffId = "STF" + uid.substring(1);
        ds.addUser(new Staff(uid, username, password, fullName, email, phone, department, staffId));
        return null;
    }

    public String updateStaff(String userId, String fullName,
                              String email, String phone, String department) {
        User u = ds.findUserById(userId);
        if (!(u instanceof Staff)) {
            return "Staff member not found.";
        }
        String err;
        if ((err = Validator.validateFullName(fullName)) != null) {
            return err;
        }
        if ((err = Validator.validateEmail(email)) != null) {
            return err;
        }
        if ((err = Validator.validatePhone(phone)) != null) {
            return err;
        }
        if (department == null || department.isBlank()) {
            return "Department is required.";
        }
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(phone);
        ((Staff) u).setDepartment(department);
        return null;
    }

    public String deleteStaff(String userId) {
        User u = ds.findUserById(userId);
        try {
            if (u == null) {
                throw new InvalidUserException("User not found", userId);
            }
            if (u instanceof Manager) {
                throw new InvalidUserException("Cannot delete a manager account", userId);
            }
            if (!(u instanceof Staff)) {
                throw new InvalidUserException("Not a staff account", userId);
            }
        } catch (InvalidUserException e) {
            return e.getMessage();
        }
        ds.removeUser(userId);
        return null;
    }

    public String toggleStaffActive(String userId) {
        User u = ds.findUserById(userId);
        if (u == null) {
            return "User not found.";
        }
        if (u instanceof Manager) {
            return "Cannot deactivate a manager.";
        }
        u.setActive(!u.isActive());
        return null;
    }
}