
package com.helpdesk.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TicketStatusValidatorTest {

    @Test
    void openCanMoveToInProgress() {
        assertTrue(TicketStatusValidator.isValidStatusTransition(
                "Open", "In Progress"));
    }

    @Test
    void inProgressCanMoveToResolved() {
        assertTrue(TicketStatusValidator.isValidStatusTransition(
                "In Progress", "Resolved"));
    }

    @Test
    void resolvedCanMoveToClosed() {
        assertTrue(TicketStatusValidator.isValidStatusTransition(
                "Resolved", "Closed"));
    }

    @Test
    void resolvedCanBeReopened() {
        assertTrue(TicketStatusValidator.isValidStatusTransition(
                "Resolved", "In Progress"));
    }

    @Test
    void unchangedStatusesAreAllowed() {
        String[] statuses = {
                "Open", "In Progress", "Resolved", "Closed"
        };

        for (String status : statuses) {
            assertTrue(TicketStatusValidator.isValidStatusTransition(
                    status, status));
        }
    }

    @Test
    void openCannotMoveDirectlyToResolved() {
        assertFalse(TicketStatusValidator.isValidStatusTransition(
                "Open", "Resolved"));
    }

    @Test
    void openCannotMoveDirectlyToClosed() {
        assertFalse(TicketStatusValidator.isValidStatusTransition(
                "Open", "Closed"));
    }

    @Test
    void inProgressCannotMoveBackToOpen() {
        assertFalse(TicketStatusValidator.isValidStatusTransition(
                "In Progress", "Open"));
    }

    @Test
    void closedTicketsCannotBeReopened() {
        assertFalse(TicketStatusValidator.isValidStatusTransition(
                "Closed", "In Progress"));

        assertFalse(TicketStatusValidator.isValidStatusTransition(
                "Closed", "Resolved"));

        assertFalse(TicketStatusValidator.isValidStatusTransition(
                "Closed", "Open"));
    }

    @Test
    void nullStatusesAreRejected() {
        assertFalse(TicketStatusValidator.isValidStatusTransition(
                null, "Open"));

        assertFalse(TicketStatusValidator.isValidStatusTransition(
                "Open", null));

        assertFalse(TicketStatusValidator.isValidStatusTransition(
                null, null));
    }

    @Test
    void unknownStatusesAreRejected() {
        assertFalse(TicketStatusValidator.isValidStatusTransition(
                "Pending", "Open"));

        assertFalse(TicketStatusValidator.isValidStatusTransition(
                "Open", "Pending"));

        assertFalse(TicketStatusValidator.isValidStatusTransition(
                "Pending", "Pending"));
    }
}
