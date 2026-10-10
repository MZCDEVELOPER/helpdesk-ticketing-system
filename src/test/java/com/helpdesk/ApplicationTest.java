package com.helpdesk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ApplicationTest {

    @Test
    void junitIsConfiguredCorrectly() {

        int expected = 8;
        int actual = 5 + 3;

        assertEquals(expected, actual);
    }
}