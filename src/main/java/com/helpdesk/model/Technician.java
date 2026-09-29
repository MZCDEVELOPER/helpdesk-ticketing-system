package com.helpdesk.model;

public class Technician extends User {

    public Technician(
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
        return "Technician";
    }
}