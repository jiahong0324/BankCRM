package bankcrm.util;

import java.util.regex.Pattern;


public final class Validator {

    private Validator() {}

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^\\d{10,11}$");
    private static final Pattern USERNAME_PATTERN =
        Pattern.compile("^[a-zA-Z0-9_]{4,20}$");

    public static String validateUsername(String v) {
        if (v == null || v.isBlank()) return "Username is required.";
        if (!USERNAME_PATTERN.matcher(v.trim()).matches())
            return "Username must be 4–20 alphanumeric characters (underscores allowed).";
        return null;
    }


    public static String validatePassword(String v) {
        if (v == null || v.isBlank()) return "Password is required.";
        if (v.length() < 8)           return "Password must be at least 8 characters.";
        if (!v.matches(".*[A-Z].*"))  return "Password must contain an uppercase letter.";
        if (!v.matches(".*[a-z].*"))  return "Password must contain a lowercase letter.";
        if (!v.matches(".*\\d.*"))    return "Password must contain a digit.";
        if (!v.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))
            return "Password must contain a special character.";
        return null;
    }

    public static String validateFullName(String v) {
        if (v == null || v.isBlank())  return "Full name is required.";
        if (v.trim().length() < 2)     return "Full name must be at least 2 characters.";
        if (v.trim().length() > 80)    return "Full name must not exceed 80 characters.";
        if (!v.matches("[\\p{L} .'-]+")) return "Full name contains invalid characters.";
        return null;
    }

    public static String validateEmail(String v) {
        if (v == null || v.isBlank()) return "Email is required.";
        if (!EMAIL_PATTERN.matcher(v.trim()).matches()) return "Invalid email address.";
        return null;
    }

    public static String validatePhone(String v) {
        if (v == null || v.isBlank()) return "Phone number is required.";
        String digits = v.trim().replaceAll("[\\s-]", "");
        if (!PHONE_PATTERN.matcher(digits).matches())
            return "Phone must be 10–11 digits (numbers only).";
        return null;
    }

    public static String notBlank(String v, String fieldName) {
        if (v == null || v.isBlank()) return fieldName + " is required.";
        return null;
    }

    public static String validateTicketDescription(String v) {
        if (v == null || v.isBlank()) return "Description is required.";
        if (v.trim().length() < 10)   return "Description must be at least 10 characters.";
        if (v.trim().length() > 1000) return "Description must not exceed 1000 characters.";
        return null;
    }

    public static String validateResponse(String v) {
        if (v == null || v.isBlank()) return "Response is required.";
        if (v.trim().length() < 5)    return "Response must be at least 5 characters.";
        return null;
    }
}
