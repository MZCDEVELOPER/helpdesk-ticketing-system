package com.helpdesk.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TicketValidatorTest {

    @Test
    void validTitleIsAccepted() {
        assertTrue(TicketValidator.isValidTitle("Printer not working"));
    }

    @Test
    void titleTooShortIsRejected() {
        assertFalse(TicketValidator.isValidTitle("Hi"));
    }

    @Test
    void titleTooLongIsRejected() {
        assertFalse(TicketValidator.isValidTitle("A".repeat(101)));
    }

    @Test
    void titleBoundaryLengthsAreAccepted() {
        assertTrue(TicketValidator.isValidTitle("A".repeat(3)));
        assertTrue(TicketValidator.isValidTitle("A".repeat(100)));
    }

    @Test
    void emptyOrNullTitleIsRejected() {
        assertFalse(TicketValidator.isValidTitle(""));
        assertFalse(TicketValidator.isValidTitle(null));
        assertFalse(TicketValidator.isValidTitle("   "));
    }

    @Test
    void validDescriptionIsAccepted() {
        assertTrue(TicketValidator.isValidDescription(
                "The office printer is not responding."));
    }

    @Test
    void descriptionTooShortIsRejected() {
        assertFalse(TicketValidator.isValidDescription("Too short"));
    }

    @Test
    void descriptionTooLongIsRejected() {
        assertFalse(TicketValidator.isValidDescription(
                "A".repeat(1001)));
    }

    @Test
    void descriptionBoundaryLengthsAreAccepted() {
        assertTrue(TicketValidator.isValidDescription(
                "A".repeat(10)));
        assertTrue(TicketValidator.isValidDescription(
                "A".repeat(1000)));
    }

    @Test
    void emptyOrNullDescriptionIsRejected() {
        assertFalse(TicketValidator.isValidDescription(""));
        assertFalse(TicketValidator.isValidDescription(null));
        assertFalse(TicketValidator.isValidDescription("   "));
    }

    @Test
    void validTicketIsAccepted() {
        assertTrue(TicketValidator.isValidTicket(
                "Network issue",
                "The office network is unavailable."));
    }

    @Test
    void invalidTicketIsRejected() {
        assertFalse(TicketValidator.isValidTicket(
                "Hi",
                "The office network is unavailable."));

        assertFalse(TicketValidator.isValidTicket(
                "Network issue",
                "Short"));
    }
}