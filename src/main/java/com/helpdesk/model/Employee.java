package com.helpdesk.model;

public class Employee extends User {

    public Employee(
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
        return "Employee";
    }
}