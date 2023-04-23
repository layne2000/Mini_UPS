package com.example.model;

import java.util.HashSet;
import java.util.List;

public class Truck {
    private final int truckId;
    private String status;
    private int x;
    private int y;
    //target whId this truck is currently heading, not null only when truck is at the status of traveling, arrive warehouse, loading or loaded
    private Integer whId;
    private List<Order> orders;

    public Truck(int truckId, String status, int x, int y) {
        this.truckId = truckId;
        this.status = status;
        this.x = x;
        this.y = y;
    }

    public Truck(int truckId, String status, int x, int y, List<Order> orders) {
        this.truckId = truckId;
        this.status = status;
        this.x = x;
        this.y = y;
        this.orders = orders;
    }

    public int getTruckId() {
        return truckId;
    }

    public String getStatus() {
        return status;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public Integer getWhId() {
        return whId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void setWhId(Integer whId) {
        this.whId = whId;
    }
}
