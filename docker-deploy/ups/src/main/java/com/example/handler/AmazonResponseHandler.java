package com.example.handler;

import com.example.model.Order;
import com.example.model.Truck;
import com.example.proto.amazon_ups.AmazonUPSProto;
import com.example.proto.world_ups.WorldUPSProto;
import com.example.service.OrderService;
import com.example.service.TruckService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Scope("prototype")
public class AmazonResponseHandler implements Runnable {
    private final ApplicationContext applicationContext;
    private AmazonHandler amazonHandler;
    private WorldHandler worldHandler;
    private final UserService userService;
    private final TruckService truckService;
    private final OrderService orderService;
    private final PlatformTransactionManager transactionManager;
    AmazonUPSProto.AUMessages auMessages;

    @Autowired
    AmazonResponseHandler(ApplicationContext applicationContext, AmazonHandler amazonHandler, WorldHandler worldHandler, UserService userService, TruckService truckService, OrderService orderService, PlatformTransactionManager transactionManager) {
        this.applicationContext = applicationContext;
        this.amazonHandler = amazonHandler;
        this.worldHandler = worldHandler;
        this.userService = userService;
        this.truckService = truckService;
        this.orderService = orderService;
        this.transactionManager = transactionManager;
    }

    public void setAuMessages(AmazonUPSProto.AUMessages auMessages) {
        this.auMessages = auMessages;
    }

    public void handleAck(Long ack) {
        amazonHandler.getUnAckedNums().remove(ack);
    }

    public void handleErr(AmazonUPSProto.Err err) {
        //send ack back first
        AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
        amazonMessageSender.setAck(err.getSeqnum());
        Thread msgSenderThread = new Thread(amazonMessageSender);
        msgSenderThread.start();
        System.out.println("handleErr ERROR: " + err.getErr() + " on the msg of " + err.getOriginseqnum());
        amazonHandler.getUnAckedNums().remove(err.getOriginseqnum());
    }

    public void handleAUPlaceOrder(AmazonUPSProto.AUPlaceOrder auPlaceOrder) {
        //act only on the first time receiving this msg. Later only send back error msg or ack, won't modify the DB
        if (amazonHandler.getSeqNumsFromAmazon().add(auPlaceOrder.getSeqnum())) {
            if (auPlaceOrder.hasUserid()) {//not sure, user on Amazon website specifies the UPS userId
                if (userService.getUserById(auPlaceOrder.getUserid()) == null) {//UPS userId doesn't exist in DB
                    //send error msg to Amazon
                    Long msgSeqNum = amazonHandler.getAndAddSeqNumToAmazon();
                    amazonHandler.getUnAckedNums().add(msgSeqNum);
                    AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
                    AmazonUPSProto.Err err = AmazonUPSProto.Err.newBuilder()
                            .setErr("This UPS userID doesn't exist!")
                            .setOriginseqnum(auPlaceOrder.getSeqnum())
                            .setSeqnum(msgSeqNum)
                            .build();
                    amazonMessageSender.setErr(err);
                    amazonMessageSender.setSeqNum(msgSeqNum);//!!
                    Thread amazonMsgSenderThread = new Thread(amazonMessageSender);
                    amazonMsgSenderThread.start();
                } else {//UPS userId exists in DB
                    //send ack back first
                    AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
                    amazonMessageSender.setAck(auPlaceOrder.getSeqnum());
                    Thread msgSenderThread = new Thread(amazonMessageSender);
                    msgSenderThread.start();
                    //insert an order in the DB
                    Order order = new Order(auPlaceOrder.getShipid(),
                            auPlaceOrder.getUserid(),
                            "CREATED",
                            auPlaceOrder.getOrder().getId(),
                            auPlaceOrder.getOrder().getDescription(),
                            auPlaceOrder.getOrder().getX(),
                            auPlaceOrder.getOrder().getY(),
                            auPlaceOrder.getOrder().getWhid(),
                            LocalDateTime.now());
                    try {
                        orderService.addOrder(order);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("handleAUPlaceOrder: Duplicate shipId in AUPlaceOrder!");
                    }
                }
            } else {//user on Amazon website doesn't specify the UPS userId
                //send ack back first
                AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
                amazonMessageSender.setAck(auPlaceOrder.getSeqnum());
                Thread msgSenderThread = new Thread(amazonMessageSender);
                msgSenderThread.start();
                //insert an order in the DB
                Order order = new Order(auPlaceOrder.getShipid(),
                        "CREATED",
                        auPlaceOrder.getOrder().getId(),
                        auPlaceOrder.getOrder().getDescription(),
                        auPlaceOrder.getOrder().getX(),
                        auPlaceOrder.getOrder().getY(),
                        auPlaceOrder.getOrder().getWhid(),
                        LocalDateTime.now());
                try {
                    orderService.addOrder(order);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("handleAUPlaceOrder: Duplicate shipId in AUPlaceOrder!");
                }
            }
        } else {//receive this msg the second time or even more
            //only send ack back when Amazon send this msg multiple times and the previous response is not error
            //if previous response is error, no need to send again, because it's sent by loop
            //while ack response is sent only once at a time
            if (!(auPlaceOrder.hasUserid() && userService.getUserById(auPlaceOrder.getUserid()) == null)) {
                //send ack back
                AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
                amazonMessageSender.setAck(auPlaceOrder.getSeqnum());
                Thread msgSenderThread = new Thread(amazonMessageSender);
                msgSenderThread.start();
            }
        }
    }

    public void handleAUAssociateUserId(AmazonUPSProto.AUAssociateUserId auAssociateUserId) {

        //ack only on the first time
        if (amazonHandler.getSeqNumsFromAmazon().add(auAssociateUserId.getSeqnum())) {
            if (userService.getUserById(auAssociateUserId.getUserid()) == null) {//This UPS userID doesn't exist
                //send error msg to Amazon
                Long msgSeqNum = amazonHandler.getAndAddSeqNumToAmazon();
                amazonHandler.getUnAckedNums().add(msgSeqNum);
                AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
                AmazonUPSProto.Err err = AmazonUPSProto.Err.newBuilder()
                        .setErr("This UPS userID does not exist!")
                        .setOriginseqnum(auAssociateUserId.getSeqnum())
                        .setSeqnum(msgSeqNum)
                        .build();
                amazonMessageSender.setErr(err);
                amazonMessageSender.setSeqNum(msgSeqNum);//!!
                Thread amazonMsgSenderThread = new Thread(amazonMessageSender);
                amazonMsgSenderThread.start();
            } else {//This UPS userID exists
                //send ack back first
                AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
                amazonMessageSender.setAck(auAssociateUserId.getSeqnum());
                Thread msgSenderThread = new Thread(amazonMessageSender);
                msgSenderThread.start();
                //modify order in the DB
                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                TransactionStatus status = transactionManager.getTransaction(definition);
                try {
                    Order order = orderService.getOrderByShipId(auAssociateUserId.getShipid());
                    order.setUserId(auAssociateUserId.getUserid());
                    orderService.updateOrder(order);
                    transactionManager.commit(status);
                } catch (Exception e) {
                    // Roll back the transaction in case of an error
                    transactionManager.rollback(status);
                    throw e;
                }
            }
        } else {//not the first time receiving this
            if (userService.getUserById(auAssociateUserId.getUserid()) != null) {//exists the user
                //send ack back
                AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
                amazonMessageSender.setAck(auAssociateUserId.getSeqnum());
                Thread msgSenderThread = new Thread(amazonMessageSender);
                msgSenderThread.start();
            }
        }
    }

    public void handleAUCallTruck(AmazonUPSProto.AUCallTruck auCallTruck) {
        //send ack back first
        AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
        amazonMessageSender.setAck(auCallTruck.getSeqnum());
        Thread msgSenderThread = new Thread(amazonMessageSender);
        msgSenderThread.start();
        if (amazonHandler.getSeqNumsFromAmazon().add(auCallTruck.getSeqnum())) {
            //identify and update the appropriate truck in the DB
            Truck chosenTruck = null;
            while (true) {//if all the trucks are busy, wait for 3s and check again
                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                TransactionStatus status = transactionManager.getTransaction(definition);
                try {
                    List<Truck> trucks = truckService.getAllTrucks();
                    Truck firstLoadedTruck = null;
                    Truck firstDeliveringTruck = null;
                    for (Truck truck : trucks) { //assign truck with the priority order of idle, loaded, delivering
//                        //test
//                        System.out.println("handleAUCallTruck: I'm inside handleAUCallTruck loop, truck info is " + truck);
                        String truckStatus = truck.getStatus();
                        //race problem here: after choosing this truck and haven't been able to modify status, it's used by other thread
                        if (truckStatus.equals("IDLE")) {
                            chosenTruck = truck;
                            chosenTruck.setStatus("TRAVELING");
                            chosenTruck.setWhId(auCallTruck.getWhnum());//newly added!
                            truckService.updateTruck(chosenTruck);
                            break;
                        } else if (firstLoadedTruck == null && truckStatus.equals("LOADED")) {
                            firstLoadedTruck = truck;
                        } else if (firstDeliveringTruck == null && truckStatus.equals("DELIVERING")) {
                            firstDeliveringTruck = truck;
                        }
                    }
                    if (chosenTruck != null) {//find an idle truck
                        transactionManager.commit(status);
                        break;
                    } else if (firstLoadedTruck != null) {//find a loaded truck
                        chosenTruck = firstLoadedTruck;
                        chosenTruck.setStatus("TRAVELING");
                        chosenTruck.setWhId(auCallTruck.getWhnum());//newly added!
                        truckService.updateTruck(chosenTruck);
                        transactionManager.commit(status);
                        break;
                    } else if (firstDeliveringTruck != null) {//find a delivering truck
                        chosenTruck = firstDeliveringTruck;
                        chosenTruck.setStatus("TRAVELING");
                        chosenTruck.setWhId(auCallTruck.getWhnum());//newly added!
                        truckService.updateTruck(chosenTruck);
                        transactionManager.commit(status);
                        break;
                    }
                    transactionManager.commit(status);
                } catch (Exception e) {
                    transactionManager.rollback(status);
                    throw e;
                }
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //send pickup msg to the world
            Long msgSeqNum = worldHandler.getAndAddSeqNumToWorld();
            worldHandler.getUnAckedNums().add(msgSeqNum);
            WorldCommandSender worldCommandSender = applicationContext.getBean(WorldCommandSender.class);
            WorldUPSProto.UGoPickup uGoPickup = WorldUPSProto.UGoPickup.newBuilder()
                    .setTruckid(chosenTruck.getTruckId())
                    .setWhid(auCallTruck.getWhnum())
                    .setSeqnum(msgSeqNum)
                    .build();
            worldCommandSender.setUGoPickup(uGoPickup);
            worldCommandSender.setSeqNum(msgSeqNum);//!!!
//            //test
//            System.out.println("handleAUCallTruck: I'm ready to send a msg!");
            Thread worldMsgSenderThread = new Thread(worldCommandSender);
            worldMsgSenderThread.start();
            //update orders in the DB
            for (Long shipId : auCallTruck.getShipidList()) {//can't be empty list
                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                TransactionStatus status = transactionManager.getTransaction(definition);
                try {
                    Order order = orderService.getOrderByShipId(shipId);
                    order.setTruckId(chosenTruck.getTruckId());
                    order.setShipmentStatus("TRUCK EN ROUTE TO WAREHOUSE");
                    order.setWhId(auCallTruck.getWhnum());
                    orderService.updateOrder(order);
                    transactionManager.commit(status);
                } catch (Exception e) {
                    // Roll back the transaction in case of an error
                    transactionManager.rollback(status);
                    throw e;
                }
            }
        }

    }

    public void handleAUUpdateTruckStatus(AmazonUPSProto.AUUpdateTruckStatus auUpdateTruckStatus) {
        //send ack back first
        AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
        amazonMessageSender.setAck(auUpdateTruckStatus.getSeqnum());
        Thread msgSenderThread = new Thread(amazonMessageSender);
        msgSenderThread.start();
        if (amazonHandler.getSeqNumsFromAmazon().add(auUpdateTruckStatus.getSeqnum())) {
            //update truck in the DB
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
            TransactionStatus status = transactionManager.getTransaction(definition);
            try {
                Truck truck = truckService.getTruckById(auUpdateTruckStatus.getTruckid());
                truck.setStatus(auUpdateTruckStatus.getStatus());
                truckService.updateTruck(truck);
                transactionManager.commit(status);
            } catch (Exception e) {
                // Roll back the transaction in case of an error
                transactionManager.rollback(status);
                throw e;
            }
        }
    }

    public void handleAUTruckGoDeliver(AmazonUPSProto.AUTruckGoDeliver auTruckGoDeliver) {
        //send ack back first
        AmazonMessageSender amazonMessageSender0 = applicationContext.getBean(AmazonMessageSender.class);
        amazonMessageSender0.setAck(auTruckGoDeliver.getSeqnum());
        Thread msgSenderThread = new Thread(amazonMessageSender0);
        msgSenderThread.start();
        if (amazonHandler.getSeqNumsFromAmazon().add(auTruckGoDeliver.getSeqnum())) {
            //update the truck in the DB,
            Truck truck = null;
            while (true) {//if the truck is busy at the moment, wait for 1s
                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                TransactionStatus status = transactionManager.getTransaction(definition);
                try {
                    truck = truckService.getTruckById(auTruckGoDeliver.getTruckid());
                    String truckStatus = truck.getStatus();
                    //race problem here
                    if (truckStatus.equals("IDLE") || truckStatus.equals("LOADED") || truckStatus.equals("DELIVERING")) {
                        truck.setStatus("DELIVERING");
                        truck.setWhId(null);//newly added!
                        truckService.updateTruck(truck);
                        transactionManager.commit(status);
                        break;
                    }
                    transactionManager.commit(status);
                } catch (Exception e) {
                    // Roll back the transaction in case of an error
                    transactionManager.rollback(status);
                    throw e;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Long msgSeqNum0 = worldHandler.getAndAddSeqNumToWorld();
            worldHandler.getUnAckedNums().add(msgSeqNum0);
            WorldCommandSender worldCommandSender = applicationContext.getBean(WorldCommandSender.class);
            WorldUPSProto.UGoDeliver.Builder uGoDeliverBuilder = WorldUPSProto.UGoDeliver.newBuilder()
                    .setTruckid(truck.getTruckId())
                    .setSeqnum(msgSeqNum0);
            //update the orders in the DB and build the msg sent to the world
            for (Long shipId : auTruckGoDeliver.getShipidList()) {
                Order order = null;
                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                TransactionStatus status = transactionManager.getTransaction(definition);
                //race problem here
                try {
                    order = orderService.getOrderByShipId(shipId);
                    order.setTruckId(truck.getTruckId());//because there maybe more packages added to this truck than previous AUCallTruck
                    order.setShipmentStatus("OUT FOR DELIVERY");
                    order.setDeliveringTime(LocalDateTime.now());
                    orderService.updateOrder(order);
                    transactionManager.commit(status);
                } catch (Exception e) {
                    // Roll back the transaction in case of an error
                    transactionManager.rollback(status);
                    throw e;
                }
                WorldUPSProto.UDeliveryLocation uDeliveryLocation = WorldUPSProto.UDeliveryLocation.newBuilder()
                        .setPackageid(order.getShipId())
                        .setX(order.getX())
                        .setY(order.getY())
                        .build();
                uGoDeliverBuilder.addPackages(uDeliveryLocation);
            }
            //send delivering msg to the world
            WorldUPSProto.UGoDeliver uGoDeliver = uGoDeliverBuilder.build();
            worldCommandSender.setUGoDeliver(uGoDeliver);
            worldCommandSender.setSeqNum(msgSeqNum0);//!!!
            //test
            System.out.println("handleAUTruckGoDeliver: I'm ready to send goDeliver msg to world");
            Thread worldMsgSenderThread = new Thread(worldCommandSender);
            worldMsgSenderThread.start();

//            //wait until receiving the ack from the world, send updatePackageStatus msg to the Amazon
//            while(worldHandler.getUnAckedNums().contains(msgSeqNum0)){
//                try {
//                    Thread.sleep(1000);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//            for (Long shipId : auTruckGoDeliver.getShipidList()) {
//                Long msgSeqNum1 = amazonHandler.getAndAddSeqNumToAmazon();
//                amazonHandler.getUnAckedNums().add(msgSeqNum1);
//                AmazonMessageSender amazonMessageSender1 = applicationContext.getBean(AmazonMessageSender.class);
//                AmazonUPSProto.UAUpdatePackageStatus uaUpdatePackageStatus = AmazonUPSProto.UAUpdatePackageStatus.newBuilder()
//                        .setShipid(shipId)
//                        .setStatus("delivering")
//                        .setSeqnum(msgSeqNum1)
//                        .build();
//                amazonMessageSender1.setUaUpdatePackageStatus(uaUpdatePackageStatus);
//                amazonMessageSender1.setSeqNum(msgSeqNum1);//!!!!
//                Thread amazonMsgSenderThread = new Thread(amazonMessageSender1);
//                amazonMsgSenderThread.start();
//            }
        }
    }

    @Override
    public void run() {
        for (Long ack : auMessages.getAcksList()) {
            handleAck(ack);
        }
        for (AmazonUPSProto.Err err : auMessages.getErrorList()) {
            handleErr(err);
        }
        for (AmazonUPSProto.AUPlaceOrder auPlaceOrder : auMessages.getOrderList()) {
            handleAUPlaceOrder(auPlaceOrder);
        }
        for (AmazonUPSProto.AUAssociateUserId auAssociateUserId : auMessages.getAssociateUserIdList()) {
            handleAUAssociateUserId(auAssociateUserId);
        }
        for (AmazonUPSProto.AUCallTruck auCallTruck : auMessages.getCallTruckList()) {
            handleAUCallTruck(auCallTruck);
        }
        for (AmazonUPSProto.AUUpdateTruckStatus auUpdateTruckStatus : auMessages.getUpdateTruckStatusList()) {
            handleAUUpdateTruckStatus(auUpdateTruckStatus);
        }
        for (AmazonUPSProto.AUTruckGoDeliver auTruckGoDeliver : auMessages.getTruckGoDeliverList()) {
            handleAUTruckGoDeliver(auTruckGoDeliver);
        }
    }
}
