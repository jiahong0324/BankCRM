package bankcrm.model;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {

    private String address;
    private String accountNumber;
    private String cardNumber;
    private double balance;
    private String accountType;
    private List<String> ticketIds;
    
    public Customer() {
        super("", "", "", "", "", "");
        this.address = "";
        this.accountNumber = "";
        this.cardNumber = "";
        this.balance = 0.0;
        this.accountType = "";
        this.ticketIds = new ArrayList<>();
    }

    public Customer(String userId, String username, String password,
                    String fullName, String email, String phone,
                    String address, String accountNumber,
                    String cardNumber, double balance, String accountType) {
        super(userId, username, password, fullName, email, phone);
        this.address = address;
        this.accountNumber = accountNumber;
        this.cardNumber = cardNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.ticketIds = new ArrayList<>();
    }

    public Customer(String userId, String username, String password,
                    String fullName, String email, String phone,
                    String address, String accountNumber) {
        this(userId, username, password, fullName, email, phone,
             address, accountNumber,
             generateMaskedCard(accountNumber),
             Math.round(1000 + Math.random() * 49000) / 100.0 * 100,
             "SAVINGS");
    }

    public static String generateMaskedCard(String accountNumber) {
        int seed = 0;
        for (char c : accountNumber.toCharArray()) {
            if (Character.isDigit(c)) {
                seed = seed * 10 + (c - '0');
            }
        }
        String prefix = "4532";
        String last4 = String.format("%04d", (seed * 7919 + 1234) % 10000);
        return prefix + " **** **** " + last4;
    }

    @Override
    public String getRole() {
        return "CUSTOMER";
    }

    @Override
    public String getDisplayInfo() {
        return "Customer: " + fullName + "  |  Account: " + accountNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getFormattedBalance() {
        return String.format("RM %,.2f", balance);
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public List<String> getTicketIds() {
        return ticketIds;
    }

    public void addTicketId(String id) {
        ticketIds.add(id);
    }

    public void removeTicketId(String id) {
        ticketIds.remove(id);
    }
}