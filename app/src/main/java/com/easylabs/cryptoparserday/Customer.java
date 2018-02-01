package com.easylabs.cryptoparserday;

/**
 * Created by Maxim on 01.02.2018.
 */

public class Customer {
    private String name;
    private String lastName;
    private String email;

    public String getName() {
        return name;
    }

    public Customer setName(String name) {
        this.name = name;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public Customer setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Customer setEmail(String email) {
        this.email = email;
        return this;
    }
}
