package com.example.model;

import java.time.LocalDateTime;

public class Order{
    private final Long shipId;
    private String userId;
    private Integer truckId;
    //CREATED, TRUCK EN ROUTE TO WAREHOUSE, TRUCK WAITING FOR PACKAGE, OUT FOR DELIVERY, DELIVERED
    private String shipmentStatus;
    private Long id;//commodity id
    private String description;
    private int x;//destination x
    private int y;//destination x
    private int whId;
    private LocalDateTime createdTime;//LocalDateTime.now()
    private LocalDateTime deliveringTime;
    private LocalDateTime deliveredTime;

    public Order(Long shipId, String shipmentStatus, Long id, String description, int x, int y, int whId, LocalDateTime createdTime) {
        this.shipId = shipId;
        this.shipmentStatus = shipmentStatus;
        this.id = id;
        this.description = description;
        this.x = x;
        this.y = y;
        this.whId = whId;
        this.createdTime=createdTime;
    }

    public Order(Long shipId, String userId, String shipmentStatus, Long id, String description, int x, int y, int whId, LocalDateTime createdTime) {
        this.shipId = shipId;
        this.userId = userId;
        this.shipmentStatus = shipmentStatus;
        this.id = id;
        this.description = description;
        this.x = x;
        this.y = y;
        this.whId = whId;
        this.createdTime=createdTime;
    }

    public Order(Long shipId, String userId, Integer truckId, String shipmentStatus, Long id, String description, int x, int y, int whId, LocalDateTime createdTime, LocalDateTime deliveringTime, LocalDateTime deliveredTime) {
        this.shipId = shipId;
        this.userId = userId;
        this.truckId = truckId;
        this.shipmentStatus = shipmentStatus;
        this.id = id;
        this.description = description;
        this.x = x;
        this.y = y;
        this.whId = whId;
        this.createdTime = createdTime;
        this.deliveringTime = deliveringTime;
        this.deliveredTime = deliveredTime;
    }

    public Long getShipId() {
        return shipId;
    }

    public String getUserId() {
        return userId;
    }

    public Integer getTruckId() {
        return truckId;
    }

    public String getShipmentStatus() {
        return shipmentStatus;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWhId() {
        return whId;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public LocalDateTime getDeliveringTime() {
        return deliveringTime;
    }

    public LocalDateTime getDeliveredTime() {
        return deliveredTime;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTruckId(Integer truckId) {
        this.truckId = truckId;
    }

    public void setShipmentStatus(String shipmentStatus) {
        this.shipmentStatus = shipmentStatus;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setWhId(int whId) {
        this.whId = whId;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public void setDeliveringTime(LocalDateTime deliveringTime) {
        this.deliveringTime = deliveringTime;
    }

    public void setDeliveredTime(LocalDateTime deliveredTime) {
        this.deliveredTime = deliveredTime;
    }
}
