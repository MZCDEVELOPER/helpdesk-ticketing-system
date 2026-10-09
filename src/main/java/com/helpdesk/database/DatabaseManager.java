package com.helpdesk.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
	private static final Logger LOGGER =
	        Logger.getLogger(DatabaseManager.class.getName());

    private static final String DB_URL =
            "jdbc:sqlite:helpdesk.db";

    /*
     * Creates and returns a connection to the database.
     *
     * TicketRepository and UserRepository use this method.
     */
    public static Connection connect()
            throws SQLException {

        return DriverManager.getConnection(DB_URL);
    }

    /*
     * Also returns a database connection.
     *
     * TicketCommentRepository currently uses this method.
     * It delegates to connect() so every repository uses
     * the same database configuration.
     */
    public static Connection getConnection()
            throws SQLException {

        return connect();
    }

    /*
     * Creates the database tables and performs any required
     * migrations without deleting existing data.
     */
    public static void initializeDatabase() {
    	try (Connection connection = connect();
             Statement statement =
                     connection.createStatement()) {

            /*
             * USERS TABLE
             */
            String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL
                    )
                    """;

            statement.execute(createUsersTable);

            /*
             * TICKETS TABLE
             */
            String createTicketsTable = """
                    CREATE TABLE IF NOT EXISTS tickets (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        category TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        status TEXT NOT NULL,
                        created_by INTEGER,
                        assigned_to INTEGER
                    )
                    """;

            statement.execute(createTicketsTable);

            /*
             * MIGRATION:
             * Add created_by if this database was created
             * before ticket ownership was implemented.
             */
            if (!columnExists(
                    connection,
                    "tickets",
                    "created_by")) {

                statement.execute(
                        "ALTER TABLE tickets "
                        + "ADD COLUMN created_by INTEGER"
                );

                System.out.println(
                        "Database migration complete: "
                        + "created_by added."
                );
            }

            /*
             * MIGRATION:
             * Add assigned_to if this database was created
             * before technician assignment was implemented.
             */
            if (!columnExists(
                    connection,
                    "tickets",
                    "assigned_to")) {

                statement.execute(
                        "ALTER TABLE tickets "
                        + "ADD COLUMN assigned_to INTEGER"
                );

                System.out.println(
                        "Database migration complete: "
                        + "assigned_to added."
                );
            }

            /*
             * TICKET COMMENTS TABLE
             *
             * Each comment belongs to a ticket and the user
             * who submitted the comment.
             */
            String createCommentsTable = """
                    CREATE TABLE IF NOT EXISTS ticket_comments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        ticket_id INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        comment_text TEXT NOT NULL,
                        created_at TEXT NOT NULL,
                        FOREIGN KEY (ticket_id)
                            REFERENCES tickets(id),
                        FOREIGN KEY (user_id)
                            REFERENCES users(id)
                    )
                    """;

            statement.execute(createCommentsTable);

            LOGGER.info("Database initialized successfully.");

        } catch (SQLException e) {

            LOGGER.log(
                    Level.SEVERE,
                    "Database initialization failed.",
                    e
            );

            throw new IllegalStateException(
                    "Unable to initialize the application database.",
                    e
            );
        }
    }

    /*
     * Checks whether a column already exists in a table.
     *
     * This prevents migrations from attempting to add
     * created_by or assigned_to every time the application
     * starts.
     */
    private static boolean columnExists(
            Connection connection,
            String tableName,
            String columnName)
            throws SQLException {

        String sql =
                "PRAGMA table_info(" + tableName + ")";

        try (Statement statement =
                     connection.createStatement();
             ResultSet resultSet =
                     statement.executeQuery(sql)) {

            while (resultSet.next()) {

                String existingColumn =
                        resultSet.getString("name");

                if (columnName.equalsIgnoreCase(
                        existingColumn)) {

                    return true;
                }
            }
        }

        return false;
    }
}