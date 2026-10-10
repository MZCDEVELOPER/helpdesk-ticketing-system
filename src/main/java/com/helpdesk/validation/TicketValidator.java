package com.helpdesk.validation;

public final class TicketValidator {

    private TicketValidator() {
        // Utility class; prevent instantiation.
    }

    public static boolean isValidTitle(String title) {
        if (title == null) {
            return false;
        }

        int length = title.trim().length();

        return length >= 3 && length <= 100;
    }

    public static boolean isValidDescription(String description) {
        if (description == null) {
            return false;
        }

        int length = description.trim().length();

        return length >= 10 && length <= 1000;
    }

    public static boolean isValidTicket(
            String title,
            String description) {

        return isValidTitle(title)
                && isValidDescription(description);
    }
}