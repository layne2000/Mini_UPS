package com.example.mapper;

import com.example.model.Order;
import com.example.model.Truck;
import org.apache.ibatis.annotations.*;

import java.util.HashSet;
import java.util.List;

@Mapper
public interface TruckMapper {
    @Insert("INSERT INTO trucks (truck_id, status, x, y) VALUES (#{truckId}, #{status}, #{x}, #{y})")
    void insertTruck(Truck truck);

    @Select("SELECT * FROM trucks WHERE truck_id = #{truckId}")
    @Results({
            @Result(property = "truckId", column = "truck_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "x", column = "x"),
            @Result(property = "y", column = "y"),
            @Result(property = "orders", column = "truck_id", javaType = List.class, many = @Many(select = "getOrdersByTruckId"))
    })
    Truck getTruckById(int truckId);

    @Select("SELECT * FROM trucks")
    List<Truck> getAllTrucks();

    @Update("UPDATE trucks SET status = #{status}, x = #{x}, y = #{y} WHERE truck_id = #{truckId}")
    void updateTruck(Truck truck);

    @Select("SELECT * FROM orders WHERE truck_id = #{truckId}")
    List<Order> getOrdersByTruckId(Integer truckId);
}
