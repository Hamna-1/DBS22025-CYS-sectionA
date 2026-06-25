package validator;

import exception.Validation;
import java.util.regex.Pattern;

public class EmployeeValidation {
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String CNIC_PATTERN = "^\\d{5}-\\d{7}-\\d{1}$";

    public static void validateEmail(String email) throws Validation {
        if (email == null || email.trim().isEmpty()) {
            throw new Validation("Email cannot be empty!");
        }
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            throw new Validation("Invalid email format!");
        }
    }

    public static void validateCNIC(String cnic) throws Validation {
        if (cnic == null || cnic.trim().isEmpty()) {
            throw new Validation("CNIC cannot be empty!");
        }
        if (!Pattern.matches(CNIC_PATTERN, cnic)) {
            throw new Validation("Invalid CNIC format! Use: XXXXX-XXXXXXX-X");
        }
    }

    public static void validateSalary(double salary) throws Validation {
        if (salary <= 0) {
            throw new Validation("Salary must be greater than 0!");
        }
    }

    public static void validateFullName(String fullName) throws Validation {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new Validation("Full name cannot be empty!");
        }
        if (fullName.length() < 3) {
            throw new Validation("Full name must be at least 3 characters!");
        }
    }
}