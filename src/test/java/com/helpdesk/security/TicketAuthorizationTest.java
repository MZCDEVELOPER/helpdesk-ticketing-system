package com.helpdesk.security;

import com.helpdesk.model.Ticket;
import com.helpdesk.model.User;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TicketAuthorizationTest {

    private static class TestUser extends User {

        private final String role;

        TestUser(int id, String role) {
            super(id, "Test User", "testuser", "testpassword");
            this.role = role;
        }

        @Override
        public String getRole() {
            return role;
        }
    }

    private Ticket createTicket(int ownerId) {
        return new Ticket(
                1,
                "Test Ticket",
                "Testing ticket authorization",
                "Hardware",
                "Medium",
                "Open",
                ownerId,
                0
        );
    }

    @Test
    void employeeCanViewOwnTicket() {
        User employee = new TestUser(1, "Employee");
        Ticket ticket = createTicket(1);

        assertTrue(
                TicketAuthorization.canViewTicket(employee, ticket)
        );
    }

    @Test
    void employeeCannotViewAnotherUsersTicket() {
        User employee = new TestUser(1, "Employee");
        Ticket ticket = createTicket(2);

        assertFalse(
                TicketAuthorization.canViewTicket(employee, ticket)
        );
    }

    @Test
    void technicianCanViewAnyTicket() {
        User technician = new TestUser(2, "Technician");

        assertTrue(
                TicketAuthorization.canViewTicket(
                        technician, createTicket(1))
        );
    }

    @Test
    void administratorCanViewAnyTicket() {
        User administrator = new TestUser(3, "Administrator");

        assertTrue(
                TicketAuthorization.canViewTicket(
                        administrator, createTicket(1))
        );
    }

    @Test
    void employeeCanCommentOnOwnTicket() {
        User employee = new TestUser(1, "Employee");

        assertTrue(
                TicketAuthorization.canCommentOnTicket(
                        employee, createTicket(1))
        );
    }

    @Test
    void employeeCannotCommentOnAnotherUsersTicket() {
        User employee = new TestUser(1, "Employee");

        assertFalse(
                TicketAuthorization.canCommentOnTicket(
                        employee, createTicket(2))
        );
    }

    @Test
    void technicianCanCommentOnAnyTicket() {
        User technician = new TestUser(2, "Technician");

        assertTrue(
                TicketAuthorization.canCommentOnTicket(
                        technician, createTicket(1))
        );
    }

    @Test
    void administratorCanCommentOnAnyTicket() {
        User administrator = new TestUser(3, "Administrator");

        assertTrue(
                TicketAuthorization.canCommentOnTicket(
                        administrator, createTicket(1))
        );
    }

    @Test
    void employeeCannotManageTickets() {
        User employee = new TestUser(1, "Employee");

        assertFalse(
                TicketAuthorization.canManageTickets(employee)
        );
    }

    @Test
    void technicianCanManageTickets() {
        User technician = new TestUser(2, "Technician");

        assertTrue(
                TicketAuthorization.canManageTickets(technician)
        );
    }

    @Test
    void administratorCanManageTickets() {
        User administrator = new TestUser(3, "Administrator");

        assertTrue(
                TicketAuthorization.canManageTickets(administrator)
        );
    }

    @Test
    void unknownRoleIsDenied() {
        User unknown = new TestUser(4, "Guest");
        Ticket ticket = createTicket(4);

        assertAll(
                () -> assertFalse(
                        TicketAuthorization.canViewTicket(unknown, ticket)),
                () -> assertFalse(
                        TicketAuthorization.canCommentOnTicket(unknown, ticket)),
                () -> assertFalse(
                        TicketAuthorization.canManageTickets(unknown))
        );
    }

    @Test
    void nullUserIsDenied() {
        Ticket ticket = createTicket(1);

        assertAll(
                () -> assertFalse(
                        TicketAuthorization.canViewTicket(null, ticket)),
                () -> assertFalse(
                        TicketAuthorization.canCommentOnTicket(null, ticket)),
                () -> assertFalse(
                        TicketAuthorization.canManageTickets(null))
        );
    }

    @Test
    void nullTicketIsDenied() {
        User employee = new TestUser(1, "Employee");

        assertAll(
                () -> assertFalse(
                        TicketAuthorization.canViewTicket(employee, null)),
                () -> assertFalse(
                        TicketAuthorization.canCommentOnTicket(employee, null))
        );
    }
}