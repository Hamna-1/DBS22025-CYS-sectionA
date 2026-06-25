package validator;

import exception.Validation;
import java.util.regex.Pattern;

public class CustomerValidation {
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String PHONE_PATTERN = "^03\\d{9}$";

    public static void validateEmail(String email) throws Validation {
        if (email == null || email.trim().isEmpty()) {
            throw new Validation("Email cannot be empty!");
        }
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            throw new Validation("Invalid email format!");
        }
    }

    public static void validatePhone(String phone) throws Validation{
        if (phone != null && !phone.trim().isEmpty()) {
            if (!Pattern.matches(PHONE_PATTERN, phone)) {
                throw new Validation("Invalid phone format! Use: 03XXXXXXXXX");
            }
        }
    }

    public static void validateFullName(String fullName) throws Validation {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new Validation("Full name cannot be empty!");
        }
    }
}