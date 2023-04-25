package com.example.mapper;

import com.example.model.Order;
import com.example.model.Truck;
import org.apache.ibatis.annotations.*;

import java.util.HashSet;
import java.util.List;

@Mapper
public interface TruckMapper {
    @Insert("INSERT INTO trucks (truck_id, status, x, y, wh_id) VALUES (#{truckId}, #{status}, #{x}, #{y}, #{whId})")
    void insertTruck(Truck truck);

    @Select("SELECT * FROM trucks WHERE truck_id = #{truckId} FOR UPDATE")
    @Results({
            @Result(property = "truckId", column = "truck_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "x", column = "x"),
            @Result(property = "y", column = "y"),
            @Result(property = "whId", column = "wh_id"),
            @Result(property = "orders", column = "truck_id", javaType = List.class, many = @Many(select = "getOrdersByTruckId"))
    })
    Truck getTruckById(int truckId);

    @Select("SELECT * FROM trucks FOR UPDATE")
    List<Truck> getAllTrucks();

    @Update("UPDATE trucks SET status = #{status}, x = #{x}, y = #{y}, wh_id = #{whId} WHERE truck_id = #{truckId}")
    void updateTruck(Truck truck);

}
