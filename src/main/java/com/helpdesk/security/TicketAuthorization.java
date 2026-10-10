package com.helpdesk.security;

import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;

public final class TicketAuthorization {

    private TicketAuthorization() {
        // Utility class.
    }

    public static boolean canViewTicket(User user, Ticket ticket) {

        if (user == null || ticket == null) {
            return false;
        }

        String role = user.getRole();

        if ("Technician".equals(role)
                || "Administrator".equals(role)) {
            return true;
        }

        if ("Employee".equals(role)) {
            return ticket.getCreatedBy() == user.getId();
        }

        return false;
    }

    public static boolean canCommentOnTicket(User user, Ticket ticket) {

        return canViewTicket(user, ticket);
    }

    public static boolean canManageTickets(User user) {

        if (user == null) {
            return false;
        }

        return "Technician".equals(user.getRole())
                || "Administrator".equals(user.getRole());
    }
}