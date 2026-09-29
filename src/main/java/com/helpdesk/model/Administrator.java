package com.helpdesk.model;

public class Administrator extends User {

    public Administrator(
            int id,
            String name,
            String username,
            String password) {

        super(
                id,
                name,
                username,
                password
        );
    }

    @Override
    public String getRole() {
        return "Administrator";
    }
}