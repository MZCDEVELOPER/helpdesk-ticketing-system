package com.helpdesk.model;

public class Ticket {

    private int id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;

    /*
     * Database ID of the user who
     * originally created the ticket.
     *
     * 0 means no creator is recorded,
     * such as tickets created before
     * ownership was implemented.
     */
    private int createdBy;

    /*
     * Database ID of the user currently
     * assigned to the ticket.
     *
     * 0 means the ticket is unassigned.
     */
    private int assignedTo;

    public Ticket(
            int id,
            String title,
            String description,
            String category,
            String priority,
            String status,
            int createdBy,
            int assignedTo) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.status = status;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public int getAssignedTo() {
        return assignedTo;
    }

    public void setTitle(
            String title) {

        this.title = title;
    }

    public void setDescription(
            String description) {

        this.description = description;
    }

    public void setCategory(
            String category) {

        this.category = category;
    }

    public void setPriority(
            String priority) {

        this.priority = priority;
    }

    public void setStatus(
            String status) {

        this.status = status;
    }

    public void setCreatedBy(
            int createdBy) {

        this.createdBy = createdBy;
    }

    public void setAssignedTo(
            int assignedTo) {

        this.assignedTo = assignedTo;
    }
}
