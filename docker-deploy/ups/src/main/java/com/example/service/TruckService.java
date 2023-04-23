package com.example.service;

import com.example.mapper.TruckMapper;
import com.example.model.Order;
import com.example.model.Truck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class TruckService {
    private final TruckMapper truckMapper;

    @Autowired
    public TruckService(TruckMapper truckMapper) {
        this.truckMapper = truckMapper;
    }

    public void addTruck(Truck truck)throws SQLException {
        truckMapper.insertTruck(truck);
    }

    public Truck getTruckById(int truckId){
        return truckMapper.getTruckById(truckId);
    }

    public List<Truck> getAllTrucks(){
        return truckMapper.getAllTrucks();
    }

    public void updateTruck(Truck truck){//throw?
        truckMapper.updateTruck(truck);
    }

    public List<Order> getOrdersByTruckId(Integer truckId){
        return truckMapper.getOrdersByTruckId(truckId);
    }

}
