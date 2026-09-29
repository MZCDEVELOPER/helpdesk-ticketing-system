package com.helpdesk.repository;

import com.helpdesk.database.DatabaseManager;
import com.helpdesk.model.Ticket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class TicketRepository {

    /**
     * Saves a new ticket.
     *
     * SQLite generates the ticket ID.
     */
    public int save(Ticket ticket) {

        String sql = """
                INSERT INTO tickets
                (
                    title,
                    description,
                    category,
                    priority,
                    status,
                    created_by,
                    assigned_to
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseManager.connect();

             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            statement.setString(
                    1,
                    ticket.getTitle()
            );

            statement.setString(
                    2,
                    ticket.getDescription()
            );

            statement.setString(
                    3,
                    ticket.getCategory()
            );

            statement.setString(
                    4,
                    ticket.getPriority()
            );

            statement.setString(
                    5,
                    ticket.getStatus()
            );

            statement.setInt(
                    6,
                    ticket.getCreatedBy()
            );

            /*
             * 0 represents an unassigned ticket.
             */
            if (ticket.getAssignedTo() == 0) {

                statement.setNull(
                        7,
                        java.sql.Types.INTEGER
                );

            } else {

                statement.setInt(
                        7,
                        ticket.getAssignedTo()
                );
            }

            int rowsInserted =
                    statement.executeUpdate();

            if (rowsInserted == 0) {

                System.err.println(
                        "Ticket creation failed."
                );

                return -1;
            }

            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {

                    int generatedId =
                            generatedKeys.getInt(1);

                    System.out.println(
                            "Ticket saved successfully. ID: "
                            + generatedId
                    );

                    return generatedId;
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Unable to save ticket: "
                    + e.getMessage()
            );
        }

        return -1;
    }

    /**
     * Retrieves every ticket.
     *
     * Technicians and Administrators use this
     * method to view the complete ticket queue.
     */
    public List<Ticket> findAll() {

        List<Ticket> tickets =
                new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    title,
                    description,
                    category,
                    priority,
                    status,
                    created_by,
                    assigned_to
                FROM tickets
                ORDER BY id
                """;

        try (Connection connection =
                     DatabaseManager.connect();

             PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet resultSet =
                     statement.executeQuery()) {

            while (resultSet.next()) {

                Ticket ticket =
                        createTicketFromResultSet(
                                resultSet
                        );

                tickets.add(
                        ticket
                );
            }

            System.out.println(
                    "Tickets loaded successfully."
            );

        } catch (SQLException e) {

            System.err.println(
                    "Unable to load tickets: "
                    + e.getMessage()
            );
        }

        return tickets;
    }

    /**
     * Retrieves tickets created by one user.
     *
     * Employees use this method so they only
     * see tickets they submitted.
     */
    public List<Ticket> findByCreatedBy(
            int userId) {

        List<Ticket> tickets =
                new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    title,
                    description,
                    category,
                    priority,
                    status,
                    created_by,
                    assigned_to
                FROM tickets
                WHERE created_by = ?
                ORDER BY id
                """;

        try (Connection connection =
                     DatabaseManager.connect();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(
                    1,
                    userId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    Ticket ticket =
                            createTicketFromResultSet(
                                    resultSet
                            );

                    tickets.add(
                            ticket
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Unable to load user tickets: "
                    + e.getMessage()
            );
        }

        return tickets;
    }

    /**
     * Updates an existing ticket.
     *
     * created_by is deliberately not changed.
     * The original creator remains the owner
     * of the ticket.
     *
     * assigned_to may change when a Technician
     * or Administrator assigns the ticket.
     */
    public boolean update(Ticket ticket) {

        String sql = """
                UPDATE tickets
                SET
                    title = ?,
                    description = ?,
                    category = ?,
                    priority = ?,
                    status = ?,
                    assigned_to = ?
                WHERE id = ?
                """;

        try (Connection connection =
                     DatabaseManager.connect();

             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    ticket.getTitle()
            );

            statement.setString(
                    2,
                    ticket.getDescription()
            );

            statement.setString(
                    3,
                    ticket.getCategory()
            );

            statement.setString(
                    4,
                    ticket.getPriority()
            );

            statement.setString(
                    5,
                    ticket.getStatus()
            );

            if (ticket.getAssignedTo() == 0) {

                statement.setNull(
                        6,
                        java.sql.Types.INTEGER
                );

            } else {

                statement.setInt(
                        6,
                        ticket.getAssignedTo()
                );
            }

            statement.setInt(
                    7,
                    ticket.getId()
            );

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated > 0) {

                System.out.println(
                        "Ticket updated successfully."
                );

                return true;
            }

            System.err.println(
                    "Ticket update failed. "
                    + "Ticket not found."
            );

        } catch (SQLException e) {

            System.err.println(
                    "Unable to update ticket: "
                    + e.getMessage()
            );
        }

        return false;
    }

    /**
     * Converts one SQLite result into
     * a Ticket object.
     *
     * JDBC getInt() returns 0 when a nullable
     * INTEGER column contains SQL NULL.
     */
    private Ticket createTicketFromResultSet(
            ResultSet resultSet)
            throws SQLException {

        return new Ticket(
                resultSet.getInt(
                        "id"
                ),

                resultSet.getString(
                        "title"
                ),

                resultSet.getString(
                        "description"
                ),

                resultSet.getString(
                        "category"
                ),

                resultSet.getString(
                        "priority"
                ),

                resultSet.getString(
                        "status"
                ),

                resultSet.getInt(
                        "created_by"
                ),

                resultSet.getInt(
                        "assigned_to"
                )
        );
    }
}