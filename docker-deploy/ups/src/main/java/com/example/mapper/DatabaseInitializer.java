package com.example.mapper;

import org.apache.ibatis.annotations.*;

//with this annotation MyBatis-Spring-Boot-Starter library automatically scans for mapper interfaces and registers them with MyBatis
@Mapper
public interface DatabaseInitializer {

    @Update("DROP TABLE IF EXISTS users;")
    void dropUsersTable();

    @Update("CREATE TABLE users ("
            + "user_id VARCHAR(255) PRIMARY KEY NOT NULL,"
            + "email VARCHAR(255) NOT NULL,"
            + "password VARCHAR(255) NOT NULL"
            + ");")
    void createUsersTable();

    @Update("DROP TABLE IF EXISTS trucks;")
    void dropTrucksTable();
    @Update("CREATE TABLE trucks ("
            + "truck_id INT PRIMARY KEY NOT NULL,"
            + "status VARCHAR(255) NOT NULL,"
            + "x INT NOT NULL,"
            + "y INT NOT NULL,"
            + "wh_id NULL"
            + ");")
    void createTrucksTable();

    @Update("DROP TABLE IF EXISTS orders;")
    void dropOrdersTable();

    @Update("CREATE TABLE orders ("
            + "ship_id BIGINT PRIMARY KEY NOT NULL,"
            + "user_id VARCHAR(255) NULL,"
            + "truck_id INT NULL,"
            + "shipment_status VARCHAR(255) NOT NULL,"
            + "commodity_id BIGINT NOT NULL,"
            + "description VARCHAR(255) NOT NULL,"
            + "x INT NOT NULL,"
            + "y INT NOT NULL,"
            + "wh_id INT NOT NULL,"
            + "created_time TIMESTAMP NULL,"
            + "delivering_time TIMESTAMP NULL,"
            + "delivered_time TIMESTAMP NULL,"
            + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,"
            + "FOREIGN KEY (truck_id) REFERENCES trucks(truck_id) ON DELETE CASCADE"
            + ");")
    void createOrdersTable();

}
