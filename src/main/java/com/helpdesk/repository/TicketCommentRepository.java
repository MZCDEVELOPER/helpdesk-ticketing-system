package com.helpdesk.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.helpdesk.database.DatabaseManager;
import com.helpdesk.model.TicketComment;

public class TicketCommentRepository {

    public int save(TicketComment comment) {
        String sql = """
                INSERT INTO ticket_comments
                (ticket_id, user_id, comment_text, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(
                             sql,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(
                    1,
                    comment.getTicketId()
            );

            statement.setInt(
                    2,
                    comment.getUserId()
            );

            statement.setString(
                    3,
                    comment.getCommentText()
            );

            statement.setString(
                    4,
                    comment.getCreatedAt().toString()
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
                                "Comment saved successfully. ID: "
                                        + generatedId
                        );

                        return generatedId;
                    }
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Database error while saving comment."
            );

            e.printStackTrace();
        }

        return -1;
    }

    public List<TicketComment> findByTicketId(
            int ticketId) {
        List<TicketComment> comments =
                new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    ticket_id,
                    user_id,
                    comment_text,
                    created_at
                FROM ticket_comments
                WHERE ticket_id = ?
                ORDER BY created_at ASC
                """;

        try (Connection connection =
                     DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, ticketId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    TicketComment comment =
                            createCommentFromResultSet(
                                    resultSet
                            );

                    comments.add(comment);
                }
            }

        } catch (SQLException e) {

            System.err.println(
                    "Database error while loading comments."
            );

            throw new IllegalStateException(
                    "Unable to load comments from the database.",
                    e
            );
        }

        return comments;
    }

    private TicketComment createCommentFromResultSet(
            ResultSet resultSet)
            throws SQLException {

        int id =
                resultSet.getInt("id");

        int ticketId =
                resultSet.getInt("ticket_id");

        int userId =
                resultSet.getInt("user_id");

        String commentText =
                resultSet.getString("comment_text");

        String createdAtText =
                resultSet.getString("created_at");

        LocalDateTime createdAt =
                LocalDateTime.parse(createdAtText);

        return new TicketComment(
                id,
                ticketId,
                userId,
                commentText,
                createdAt
        );
    }
}