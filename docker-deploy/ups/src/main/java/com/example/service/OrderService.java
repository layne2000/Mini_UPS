package com.example.service;

import com.example.mapper.OrderMapper;
import com.example.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class OrderService {

    //Spring will automatically create a concrete implementation of the mapper interface at runtime
    private final OrderMapper orderMapper;

    @Autowired
    public OrderService(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    public void addOrder(Order order) throws SQLException {//not dark?
        orderMapper.insertOrder(order);
    }

    public Order getOrderByShipId(Long shipId){//return null if doesn't exist
        return orderMapper.getOrderByShipId(shipId);
    }

    public void updateOrder(Order order)throws SQLException{
        orderMapper.updateOrder(order);
    }

    public void deleteOrderByShipId(Long shipId)throws SQLException{
        orderMapper.deleteOrderByShipId(shipId);
    }

}
