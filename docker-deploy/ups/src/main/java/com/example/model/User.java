package com.example.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class User {
    private final String userId;
    private String email;
    private String password;
    private List<Order> orders;


    public User(String userId, String email, String password) {
        this.userId = userId;
        this.email = email;
        this.password = password;
    }

    public User(String userId, String email, String password, List<Order> orders) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.orders = orders;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
