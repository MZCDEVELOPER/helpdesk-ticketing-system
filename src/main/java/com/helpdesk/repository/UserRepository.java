package com.helpdesk.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.helpdesk.database.DatabaseManager;
import com.helpdesk.model.Administrator;
import com.helpdesk.model.Employee;
import com.helpdesk.model.Technician;
import com.helpdesk.model.User;
import com.helpdesk.security.PasswordUtils;

public class UserRepository {

    /*
     * Saves a new user to the database.
     *
     * The user's password is hashed before it is stored.
     *
     * Returns the generated database ID if successful.
     * Returns -1 if the user could not be saved.
     */
    public int save(User user) {

        String sql = """
                INSERT INTO users
                (name, username, password, role)
                VALUES (?, ?, ?, ?)
                """;

        String hashedPassword =
                PasswordUtils.hashPassword(
                        user.getPassword()
                );

        try (Connection connection =
                     DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(
                    1,
                    user.getName()
            );

            statement.setString(
                    2,
                    user.getUsername()
            );

            statement.setString(
                    3,
                    hashedPassword
            );

            statement.setString(
                    4,
                    user.getRole()
            );

            int rowsAffected =
                    statement.executeUpdate();

            if (rowsAffected > 0) {

                try (ResultSet generatedKeys =
                             statement.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        int generatedId =
                                generatedKeys.getInt(1);

                        System.out.println(
                                "User saved successfully. ID: "
                                        + generatedId
                        );

                        return generatedId;
                    }
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error saving user: "
                            + e.getMessage()
            );
        }

        return -1;
    }

    /*
     * Finds a user by username.
     */
    public User findByUsername(String username) {

        String sql = """
                SELECT
                    id,
                    name,
                    username,
                    password,
                    role
                FROM users
                WHERE username = ?
                """;

        try (Connection connection =
                     DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    username
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (resultSet.next()) {

                    return createUserFromResultSet(
                            resultSet
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error finding user by username: "
                            + e.getMessage()
            );
        }

        return null;
    }

    /*
     * Finds a user using the user's database ID.
     *
     * This is used by the ticket interface to convert
     * created_by and assigned_to IDs into readable
     * user information.
     */
    public User findById(int userId) {

        if (userId <= 0) {
            return null;
        }

        String sql = """
                SELECT
                    id,
                    name,
                    username,
                    password,
                    role
                FROM users
                WHERE id = ?
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

                if (resultSet.next()) {

                    return createUserFromResultSet(
                            resultSet
                    );
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Error finding user by ID: "
                            + e.getMessage()
            );
        }

        return null;
    }

    /*
     * Authenticates a user.
     *
     * The entered password is checked against the
     * hashed password stored in SQLite.
     */
    public User authenticate(
            String username,
            String password) {

        if (username == null
                || username.isBlank()
                || password == null
                || password.isBlank()) {

            return null;
        }

        User user =
                findByUsername(
                        username.trim()
                );

        if (user == null) {

            System.out.println(
                    "Authentication failed: "
                            + "user not found."
            );

            return null;
        }

        boolean validPassword =
                PasswordUtils.verifyPassword(
                        password,
                        user.getPassword()
                );

        if (!validPassword) {

            System.out.println(
                    "Authentication failed: "
                            + "incorrect password."
            );

            return null;
        }

        System.out.println(
                "Login successful: "
                        + user.getUsername()
                        + " ("
                        + user.getRole()
                        + ")"
        );

        return user;
    }

    /*
     * Returns all Technician accounts.
     *
     * This is used by the ticket assignment interface.
     */
    public List<User> findTechnicians() {

        List<User> technicians =
                new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    name,
                    username,
                    password,
                    role
                FROM users
                WHERE role = ?
                ORDER BY name ASC
                """;

        try (Connection connection =
                     DatabaseManager.connect();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    "Technician"
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    User user =
                            createUserFromResultSet(
                                    resultSet
                            );

                    if (user != null) {
                        technicians.add(user);
                    }
                }
            }

            System.out.println(
                    "Technicians loaded: "
                            + technicians.size()
            );

        } catch (SQLException e) {

            System.err.println(
                    "Error loading technicians: "
                            + e.getMessage()
            );
        }

        return technicians;
    }

    /*
     * Creates the development/test accounts if they
     * do not already exist.
     */
    public void createDefaultUsers() {

        createDefaultUserIfMissing(
                new Employee(
                        0,
                        "Test Employee",
                        "employee",
                        "Employee123!"
                )
        );

        createDefaultUserIfMissing(
                new Technician(
                        0,
                        "IT Technician",
                        "technician",
                        "Tech123!"
                )
        );

        createDefaultUserIfMissing(
                new Administrator(
                        0,
                        "System Administrator",
                        "admin",
                        "Admin123!"
                )
        );
    }

    /*
     * Prevents the default users from being inserted
     * every time the application starts.
     */
    private void createDefaultUserIfMissing(
            User user) {

        User existingUser =
                findByUsername(
                        user.getUsername()
                );

        if (existingUser == null) {

            int generatedId =
                    save(user);

            if (generatedId > 0) {

                System.out.println(
                        "Default user created: "
                                + user.getUsername()
                );
            }

        }
    }

    /*
     * Converts a database row into the correct
     * User subclass.
     */
    private User createUserFromResultSet(
            ResultSet resultSet)
            throws SQLException {

        int id =
                resultSet.getInt("id");

        String name =
                resultSet.getString("name");

        String username =
                resultSet.getString("username");

        String password =
                resultSet.getString("password");

        String role =
                resultSet.getString("role");

        return createUserFromRole(
                id,
                name,
                username,
                password,
                role
        );
    }

    /*
     * Creates the appropriate User subclass based
     * on the role stored in the database.
     */
    private User createUserFromRole(
            int id,
            String name,
            String username,
            String password,
            String role) {

        if (role == null) {

            System.err.println(
                    "Cannot create user: role is null."
            );

            return null;
        }

        return switch (role) {

            case "Employee" ->
                    new Employee(
                            id,
                            name,
                            username,
                            password
                    );

            case "Technician" ->
                    new Technician(
                            id,
                            name,
                            username,
                            password
                    );

            case "Administrator" ->
                    new Administrator(
                            id,
                            name,
                            username,
                            password
                    );

            default -> {

                System.err.println(
                        "Unknown user role: "
                                + role
                );

                yield null;
            }
        };
    }
}