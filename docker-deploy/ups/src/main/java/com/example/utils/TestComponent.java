package com.example.utils;

import com.example.model.Order;
import com.example.model.Truck;
import com.example.model.User;
import com.example.service.OrderService;
import com.example.service.TruckService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.lang.System.exit;

@Component
public class TestComponent {
    private final UserService userService;
    private final OrderService orderService;
    private final TruckService truckService;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public TestComponent(UserService userService, OrderService orderService, TruckService truckService, PlatformTransactionManager transactionManager) {
        this.userService = userService;
        this.orderService = orderService;
        this.truckService = truckService;
        this.transactionManager = transactionManager;
    }

    public void test() throws SQLException {
        Order order1 = null;
        Order order2 = null;
        //test transaction
        DefaultTransactionDefinition definition0 = new DefaultTransactionDefinition();
        TransactionStatus status0 = transactionManager.getTransaction(definition0);
        try {
            //create
            User user = new User("user001", "test@233", "password");
            userService.addUser(user);
            order1 = new Order(11L, "created", 1L, "description1", 11, 11, 1, LocalDateTime.now());
            order2 = new Order(22L, "user001", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
            orderService.addOrder(order1);
            orderService.addOrder(order2);
            transactionManager.commit(status0);
        } catch (Exception e) {
            // Roll back the transaction in case of an error
            transactionManager.rollback(status0);
            throw e;
        }
        DefaultTransactionDefinition definition1 = new DefaultTransactionDefinition();
        TransactionStatus status1 = transactionManager.getTransaction(definition1);
        try {
            Truck truck = new Truck(1, "idle", 0, 0);
            truckService.addTruck(truck);

            //update
            order1.setUserId("user001");
            order1.setTruckId(1);
            orderService.updateOrder(order1);
            order2.setShipmentStatus("out for delivery");
            order2.setDeliveringTime(LocalDateTime.now());
            order2.setTruckId(1);
            orderService.updateOrder(order2);

            //retrieve
            User testUser = userService.getUserById("user001");
            //as long as order's userId and truckId are set properly, there will be list of orders for user and truck
            //the following statements are useless because update sql statement doesn't involve list update (actually, it's not stored in DB)
//            ArrayList<Order> userOrders = new ArrayList<>();
//            userOrders.add(order1);
//            userOrders.add(order2);
//            testUser.setOrders(userOrders);
//            userService.updateUser(testUser);
            List<Order> orders = userService.getOrdersByUserId("user001");
            for (Order order : userService.getOrdersByUserId("user001")) {
                System.out.println(order.getShipId());
            }
//            System.out.println(testUser.getPassword());
//            List<Order> userOrders = testUser.getOrders();
//            for(Order userOrder: userOrders){
//                System.out.println(userOrder.getDescription());
//            }

            //delete
            orderService.deleteOrderByShipId(22L);

            //error cases
            //userService.addUser(new User("user001", "test@23", "pass")); // throw exception
            userService.deleteUserById("233"); //will NOT throw exception, do nothing
            if (userService.getUserById("233") == null) {//null
                System.out.println("it's null, not empty");
            } else {
                System.out.println("it's empty, not null");
            }
            if (userService.getOrdersByUserId("233").isEmpty()) {//empty list
                System.out.println("it's a empty list, not null");
            } else {
                System.out.println("it's null, not empty list");
            }
            transactionManager.commit(status1);
        } catch (Exception e) {
            // Roll back the transaction in case of an error
            transactionManager.rollback(status1);
            throw e;
        }
    }
}
