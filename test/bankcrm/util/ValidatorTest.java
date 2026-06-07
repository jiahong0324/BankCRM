package bankcrm.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ValidatorTest {

    @Test
    public void testValidateUsername_Valid() {
        assertNull(Validator.validateUsername("user123"));
        assertNull(Validator.validateUsername("admin_user"));
    }

    @Test
    public void testValidateUsername_NullOrBlank() {
        assertEquals("Username is required.", Validator.validateUsername(null));
        assertEquals("Username is required.", Validator.validateUsername("   "));
    }

    @Test
    public void testValidateUsername_InvalidCharacters OrLength() {
        // Too short
        assertEquals("Username must be 4–20 alphanumeric characters (underscores allowed).", Validator.validateUsername("abc"));
        // Too long
        assertEquals("Username must be 4–20 alphanumeric characters (underscores allowed).", Validator.validateUsername("a".repeat(21)));
        // Invalid special characters
        assertEquals("Username must be 4–20 alphanumeric characters (underscores allowed).", Validator.validateUsername("user@name"));
    }

    @Test
    public void testValidatePassword_Valid() {
        assertNull(Validator.validatePassword("Secure@123"));
    }

    @Test
    public void testValidatePassword_Invalid() {
        assertEquals("Password is required.", Validator.validatePassword(null));
        assertEquals("Password must be at least 8 characters.", Validator.validatePassword("S@12"));
        assertEquals("Password must contain an uppercase letter.", Validator.validatePassword("secure@123"));
        assertEquals("Password must contain a lowercase letter.", Validator.validatePassword("SECURE@123"));
        assertEquals("Password must contain a digit.", Validator.validatePassword("Secure@pass"));
        assertEquals("Password must contain a special character.", Validator.validatePassword("Secure123"));
    }

    @Test
    public void testValidateFullName_Valid() {
        assertNull(Validator.validateFullName("John Doe"));
        assertNull(Validator.validateFullName("Mary Jane O'Connor"));
    }

    @Test
    public void testValidateFullName_Invalid() {
        assertEquals("Full name is required.", Validator.validateFullName(null));
        assertEquals("Full name must be at least 2 characters.", Validator.validateFullName("A"));
        assertEquals("Full name contains invalid characters.", Validator.validateFullName("John123"));
    }

    @Test
    public void testValidateEmail_Valid() {
        assertNull(Validator.validateEmail("test@example.com"));
        assertNull(Validator.validateEmail("user.name+tag@domain.co.uk"));
    }

    @Test
    public void testValidateEmail_Invalid() {
        assertEquals("Email is required.", Validator.validateEmail(null));
        assertEquals("Invalid email address.", Validator.validateEmail("plainaddress"));
        assertEquals("Invalid email address.", Validator.validateEmail("test@example"));
        assertEquals("Invalid email address.", Validator.validateEmail("@example.com"));
    }

    @Test
    public void testValidatePhone_Valid() {
        assertNull(Validator.validatePhone("0123456789"));
        assertNull(Validator.validatePhone("012-345 6789")); // Spaces/hyphens should be ignored
    }

    @Test
    public void testValidatePhone_Invalid() {
        assertEquals("Phone number is required.", Validator.validatePhone(null));
        assertEquals("Phone must be 10–11 digits (numbers only).", Validator.validatePhone("12345"));
        assertEquals("Phone must be 10–11 digits (numbers only).", Validator.validatePhone("0123456789ab"));
    }
}
