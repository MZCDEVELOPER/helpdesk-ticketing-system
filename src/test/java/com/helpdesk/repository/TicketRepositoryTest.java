
package com.helpdesk.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.helpdesk.database.DatabaseManager;
import com.helpdesk.model.Ticket;

class TicketRepositoryTest {

    private static final String DB_PROPERTY = "helpdesk.db.url";

    @TempDir
    Path temporaryDirectory;

    private String previousDatabaseUrl;

    @BeforeEach
    void setUp() {

        previousDatabaseUrl = System.getProperty(DB_PROPERTY);

        Path testDatabase =
                temporaryDirectory.resolve("test-helpdesk.db");

        String expectedUrl =
                "jdbc:sqlite:" + testDatabase.toAbsolutePath();

        System.setProperty(DB_PROPERTY, expectedUrl);

        assertEquals(
                expectedUrl,
                System.getProperty(DB_PROPERTY),
                "Tests must use the temporary SQLite database."
        );

        DatabaseManager.initializeDatabase();
    }

    @AfterEach
    void tearDown() {

        if (previousDatabaseUrl == null) {
            System.clearProperty(DB_PROPERTY);
        } else {
            System.setProperty(
                    DB_PROPERTY,
                    previousDatabaseUrl
            );
        }
    }

    /*
     * Test 1:
     * The temporary database should start empty.
     */
    @Test
    void temporaryDatabaseStartsWithNoTickets()
            throws Exception {

        try (Connection connection = DatabaseManager.connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT COUNT(*) FROM tickets"
             )) {

            assertTrue(resultSet.next());
            assertEquals(0, resultSet.getInt(1));
        }
    }

    /*
     * Test 2:
     * Save a ticket and retrieve its stored values.
     */
    @Test
    void saveAndRetrieveTicket() {

        TicketRepository repository = new TicketRepository();

        Ticket newTicket = new Ticket(
                0,
                "Test Printer Issue",
                "The office printer is not responding.",
                "Hardware",
                "High",
                "Open",
                1,
                0
        );

        int generatedId = repository.save(newTicket);

        assertTrue(
                generatedId > 0,
                "Saving a ticket should generate an ID."
        );

        List<Ticket> tickets = repository.findAll();

        assertEquals(1, tickets.size());

        Ticket savedTicket = tickets.get(0);

        assertEquals(generatedId, savedTicket.getId());
        assertEquals("Test Printer Issue", savedTicket.getTitle());
        assertEquals(
                "The office printer is not responding.",
                savedTicket.getDescription()
        );
        assertEquals("Hardware", savedTicket.getCategory());
        assertEquals("High", savedTicket.getPriority());
        assertEquals("Open", savedTicket.getStatus());
        assertEquals(1, savedTicket.getCreatedBy());
        assertEquals(0, savedTicket.getAssignedTo());
    }

    /*
     * Test 3:
     * Update an existing ticket while preserving its creator.
     */
    @Test
    void updateExistingTicketPreservesCreator() {

        TicketRepository repository = new TicketRepository();

        Ticket originalTicket = new Ticket(
                0,
                "Network Connection Issue",
                "The office network connection is unavailable.",
                "Network",
                "Medium",
                "Open",
                1,
                0
        );

        int ticketId = repository.save(originalTicket);

        assertTrue(ticketId > 0);

        Ticket updatedTicket = new Ticket(
                ticketId,
                "Network Connection Restored",
                "The network connection has been restored.",
                "Network",
                "High",
                "In Progress",
                999,
                2
        );

        boolean updated = repository.update(updatedTicket);

        assertTrue(
                updated,
                "Updating an existing ticket should succeed."
        );

        List<Ticket> tickets = repository.findAll();

        assertEquals(1, tickets.size());

        Ticket savedTicket = tickets.get(0);

        assertEquals(ticketId, savedTicket.getId());
        assertEquals(
                "Network Connection Restored",
                savedTicket.getTitle()
        );
        assertEquals(
                "The network connection has been restored.",
                savedTicket.getDescription()
        );
        assertEquals("Network", savedTicket.getCategory());
        assertEquals("High", savedTicket.getPriority());
        assertEquals("In Progress", savedTicket.getStatus());
        assertEquals(2, savedTicket.getAssignedTo());

        // The original creator must remain unchanged.
        assertEquals(1, savedTicket.getCreatedBy());
    }

    /*
     * Test 4:
     * Employees should retrieve only their own tickets.
     */
    @Test
    void findByCreatedByReturnsOnlyEmployeeTickets() {

        TicketRepository repository = new TicketRepository();

        Ticket employeeOneTicket = new Ticket(
                0,
                "Printer Issue",
                "The printer is not responding.",
                "Hardware",
                "Medium",
                "Open",
                1,
                0
        );

        Ticket employeeTwoTicket = new Ticket(
                0,
                "Network Issue",
                "The network connection is unavailable.",
                "Network",
                "High",
                "Open",
                2,
                0
        );

        Ticket employeeOneSecondTicket = new Ticket(
                0,
                "Software Issue",
                "The application will not launch.",
                "Software",
                "Low",
                "Open",
                1,
                0
        );

        assertTrue(repository.save(employeeOneTicket) > 0);
        assertTrue(repository.save(employeeTwoTicket) > 0);
        assertTrue(repository.save(employeeOneSecondTicket) > 0);

        List<Ticket> employeeOneTickets =
                repository.findByCreatedBy(1);

        assertEquals(2, employeeOneTickets.size());

        for (Ticket ticket : employeeOneTickets) {
            assertEquals(1, ticket.getCreatedBy());
        }

        List<Ticket> employeeTwoTickets =
                repository.findByCreatedBy(2);

        assertEquals(1, employeeTwoTickets.size());
        assertEquals(
                2,
                employeeTwoTickets.get(0).getCreatedBy()
        );

        List<Ticket> unknownEmployeeTickets =
                repository.findByCreatedBy(999);

        assertTrue(unknownEmployeeTickets.isEmpty());

        // Verify all three tickets still exist in the database.
        assertEquals(3, repository.findAll().size());
        
    }
    @Test 
    void findByIdReturnsCorrectTicket() {

        TicketRepository repository = new TicketRepository();

        Ticket ticket = new Ticket(
                0,
                "Database Lookup Test",
                "Testing ticket retrieval by ID",
                "Hardware",
                "Medium",
                "Open",
                1,
                0
        );

        int ticketId = repository.save(ticket);

        assertTrue(ticketId > 0);

        Ticket retrievedTicket = repository.findById(ticketId);

        assertNotNull(retrievedTicket);
        assertEquals(ticketId, retrievedTicket.getId());
        assertEquals("Database Lookup Test", retrievedTicket.getTitle());
        assertEquals(1, retrievedTicket.getCreatedBy());
    }
    /*
     * Test 6:
     * A nonexistent ticket should return null.
     */
    @Test
    void findByIdReturnsNullForNonexistentTicket() {

        TicketRepository repository = new TicketRepository();

        Ticket retrievedTicket = repository.findById(99999);

        assertNull(
                retrievedTicket,
                "A nonexistent ticket should return null."
        );
    }
}

