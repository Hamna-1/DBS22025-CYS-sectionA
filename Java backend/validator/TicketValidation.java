package validator;

import exception.Validation;

public class TicketValidation {
    public static void validateSeatNumber(String seatNumber) throws Validation {
        if (seatNumber == null || seatNumber.trim().isEmpty()) {
            throw new Validation("Seat number cannot be empty!");
        }
    }

    public static void validatePrice(double price) throws Validation{
        if (price < 0) {
            throw new Validation("Price cannot be negative!");
        }
    }

    public static void validateScheduleId(int scheduleId) throws Validation {
        if (scheduleId <= 0) {
            throw new Validation("Invalid schedule ID!");
        }
    }
}