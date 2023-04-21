package com.example.mapper;

import com.example.model.Order;
import org.apache.ibatis.annotations.*;

//essentially is DAO layer.
@Mapper
public interface OrderMapper {
    @Insert("INSERT INTO orders (ship_id, user_id, truck_id, shipment_status, commodity_id, description, x, y, wh_id, created_time, delivering_time, delivered_time) VALUES (#{shipId}, #{userId}, #{truckId}, #{shipmentStatus}, #{id}, #{description}, #{x}, #{y}, #{whId}, #{createdTime}, #{deliveringTime}, #{deliveredTime})")
    void insertOrder(Order order);

    @Select("SELECT * FROM orders WHERE ship_id = #{shipId}")
    @Results({
            @Result(property = "shipId", column = "ship_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "truckId", column = "truck_id"),
            @Result(property = "shipmentStatus", column = "shipment_status"),
            @Result(property = "id", column = "commodity_id"),
            @Result(property = "description", column = "description"),
            @Result(property = "x", column = "x"),
            @Result(property = "y", column = "y"),
            @Result(property = "whId", column = "wh_id"),
            @Result(property = "createdTime", column = "created_time"),
            @Result(property = "deliveringTime", column = "delivering_time"),
            @Result(property = "deliveredTime", column = "delivered_time")
    })
    Order getOrderByShipId(Long shipId);

    @Update("UPDATE orders SET user_id = #{userId}, truck_id = #{truckId}, shipment_status = #{shipmentStatus}, commodity_id = #{id}, description = #{description}, x = #{x}, y = #{y}, wh_id = #{whId}, created_time = #{createdTime}, delivering_time = #{deliveringTime}, delivered_time = #{deliveredTime} WHERE ship_id = #{shipId}")
    void updateOrder(Order order);

    @Delete("DELETE FROM orders WHERE ship_id = #{shipId}")
    void deleteOrderByShipId(Long shipId);
}
