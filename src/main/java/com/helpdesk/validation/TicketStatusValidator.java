package com.helpdesk.validation;

public final class TicketStatusValidator {

    private TicketStatusValidator() {
        // Prevent instantiation.
    }

    public static boolean isValidStatusTransition(
            String currentStatus,
            String newStatus) {

        if (currentStatus == null || newStatus == null) {
            return false;
        }

        if (currentStatus.equals(newStatus)) {
            return switch (currentStatus) {
                case "Open", "In Progress", "Resolved", "Closed" -> true;
                default -> false;
            };
        }

        return switch (currentStatus) {

            case "Open" ->
                    newStatus.equals("In Progress");

            case "In Progress" ->
                    newStatus.equals("Resolved");

            case "Resolved" ->
                    newStatus.equals("Closed")
                    || newStatus.equals("In Progress");

            case "Closed" ->
                    false;

            default ->
                    false;
        };
    }
}