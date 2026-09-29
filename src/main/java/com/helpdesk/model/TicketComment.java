package com.helpdesk.model;

import java.time.LocalDateTime;

public class TicketComment {

    private int id;
    private int ticketId;
    private int userId;
    private String commentText;
    private LocalDateTime createdAt;

    public TicketComment(
            int id,
            int ticketId,
            int userId,
            String commentText,
            LocalDateTime createdAt) {

        this.id = id;
        this.ticketId = ticketId;
        this.userId = userId;
        this.commentText = commentText;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getTicketId() {
        return ticketId;
    }

    public int getUserId() {
        return userId;
    }

    public String getCommentText() {
        return commentText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}
