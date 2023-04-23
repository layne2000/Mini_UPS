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

import java.time.LocalDateTime;

@Component
@Scope("prototype")
public class AmazonResponseHandler implements Runnable{
    private final ApplicationContext applicationContext;
    private AmazonHandler amazonHandler;
    private WorldHandler worldHandler;
    private final UserService userService;
    private final TruckService truckService;
    private final OrderService orderService;
    AmazonUPSProto.AUMessages auMessages;

    @Autowired
    AmazonResponseHandler(ApplicationContext applicationContext, AmazonHandler amazonHandler, WorldHandler worldHandler,  UserService userService, TruckService truckService, OrderService orderService){
        this.applicationContext = applicationContext;
        this.amazonHandler = amazonHandler;
        this.worldHandler=worldHandler;
        this.userService=userService;
        this.truckService=truckService;
        this.orderService=orderService;
    }

    public void setAuMessages(AmazonUPSProto.AUMessages auMessages) {
        this.auMessages = auMessages;
    }

    public void handleAck(Long ack){
        amazonHandler.getUnAckedNums().remove(ack);
    }

    public void handleErr(AmazonUPSProto.Err err){
        //send ack back first
        AmazonMessageSender amazonMessageSender=applicationContext.getBean(AmazonMessageSender.class);
        amazonMessageSender.setAck(err.getSeqnum());
        Thread msgSenderThread = new Thread(amazonMessageSender);
        msgSenderThread.start();
        System.out.println(err.getErr()+" on the msg of "+err.getOriginseqnum());
        amazonHandler.getUnAckedNums().remove(err.getOriginseqnum());
    }

    public void handleAUPlaceOrder(AmazonUPSProto.AUPlaceOrder auPlaceOrder){
        //act only on the first time receiving this msg. Later only send back error msg or ack, won't modify the DB
        if(amazonHandler.getSeqNumsFromAmazon().add(auPlaceOrder.getSeqnum())){
            if(auPlaceOrder.hasUserid()){//not sure, user on Amazon website specifies the UPS userId
                if(userService.getUserById(auPlaceOrder.getUserid())==null){//UPS userId doesn't exist in DB
                    //send error msg to Amazon
                    Long msgSeqNum=amazonHandler.getAndAddSeqNumToAmazon();
                    amazonHandler.getUnAckedNums().add(msgSeqNum);
                    AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
                    AmazonUPSProto.Err err = AmazonUPSProto.Err.newBuilder()
                            .setErr("This UPS userID doesn't exist!")
                            .setOriginseqnum(auPlaceOrder.getSeqnum())
                            .setSeqnum(msgSeqNum)
                            .build();
                    amazonMessageSender.setErr(err);
                    Thread amazonMsgSenderThread = new Thread(amazonMessageSender);
                    amazonMsgSenderThread.start();
                }
                else{//UPS userId exists in DB
                    //send ack back first
                    AmazonMessageSender amazonMessageSender=applicationContext.getBean(AmazonMessageSender.class);
                    amazonMessageSender.setAck(auPlaceOrder.getSeqnum());
                    Thread msgSenderThread = new Thread(amazonMessageSender);
                    msgSenderThread.start();
                    //insert an order in the DB
                    Order order = new Order(auPlaceOrder.getShipid(),
                            auPlaceOrder.getUserid(),
                            "created",
                            auPlaceOrder.getOrder().getId(),
                            auPlaceOrder.getOrder().getDescription(),
                            auPlaceOrder.getOrder().getX(),
                            auPlaceOrder.getOrder().getY(),
                            auPlaceOrder.getOrder().getWhid(),
                            LocalDateTime.now());
                    try {
                        orderService.addOrder(order);
                    }catch (Exception e){
                        e.printStackTrace();
                        System.out.println("Duplicate shipId in AUPlaceOrder!");
                    }
                }
            }
            else{//user on Amazon website doesn't specifie the UPS userId
                //send ack back first
                AmazonMessageSender amazonMessageSender=applicationContext.getBean(AmazonMessageSender.class);
                amazonMessageSender.setAck(auPlaceOrder.getSeqnum());
                Thread msgSenderThread = new Thread(amazonMessageSender);
                msgSenderThread.start();
                //insert an order in the DB
                Order order = new Order(auPlaceOrder.getShipid(),
                        "created",
                        auPlaceOrder.getOrder().getId(),
                        auPlaceOrder.getOrder().getDescription(),
                        auPlaceOrder.getOrder().getX(),
                        auPlaceOrder.getOrder().getY(),
                        auPlaceOrder.getOrder().getWhid(),
                        LocalDateTime.now());
                try {
                    orderService.addOrder(order);
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("Duplicate shipId in AUPlaceOrder!");
                }
            }
        }
        else{//receive this msg the second time or even more
            //only send ack back when Amazon send this msg multiple times and the previous response is not error
            //if previous response is error, no need to send again, because it's sent by loop
            //while ack response is sent only once at a time
            if(!(auPlaceOrder.hasUserid()&&userService.getUserById(auPlaceOrder.getUserid())==null)){
                //send ack back
                AmazonMessageSender amazonMessageSender=applicationContext.getBean(AmazonMessageSender.class);
                amazonMessageSender.setAck(auPlaceOrder.getSeqnum());
                Thread msgSenderThread = new Thread(amazonMessageSender);
                msgSenderThread.start();
            }
        }
    }
    public void handleAUAssociateUserId(AmazonUPSProto.AUAssociateUserId auAssociateUserId){
        //send ack back first
        AmazonMessageSender amazonMessageSender=applicationContext.getBean(AmazonMessageSender.class);
        amazonMessageSender.setAck(auAssociateUserId.getSeqnum());
        Thread msgSenderThread = new Thread(amazonMessageSender);
        msgSenderThread.start();
        //ack only on the first time
        if(amazonHandler.getSeqNumsFromAmazon().add(auAssociateUserId.getSeqnum())){
            //modify order in the DB
            Order order = orderService.getOrderByShipId(auAssociateUserId.getShipid());
            order.setUserId(auAssociateUserId.getUserid());
            try {
                orderService.updateOrder(order);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Mistaken shipId in AUAssociateUserId!");
            }
        }
    }
    public void handleAUCallTruck(AmazonUPSProto.AUCallTruck auCallTruck){
        //send ack back first
        AmazonMessageSender amazonMessageSender=applicationContext.getBean(AmazonMessageSender.class);
        amazonMessageSender.setAck(auCallTruck.getSeqnum());
        Thread msgSenderThread = new Thread(amazonMessageSender);
        msgSenderThread.start();
        if(amazonHandler.getSeqNumsFromAmazon().add(auCallTruck.getSeqnum())){
            //identify and update the appropriate truck in the DB, TODO: To be updated
            Truck chosenTruck = null;
            while(true){//if all the trucks are busy, wait for 3s and check again
                for(Truck truck: truckService.getAllTrucks()){
                    String truckStatus = truck.getStatus();
                    if(truckStatus.equals("idle")||truckStatus.equals("loaded")||truckStatus.equals("delivering")){
                        chosenTruck=truck;
                        chosenTruck.setStatus("traveling");
                        truckService.updateTruck(chosenTruck);
                    }
                    if(chosenTruck!=null){
                        break;
                    }
                }
                if(chosenTruck!=null){
                    break;
                }
                try {
                    Thread.sleep(3000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            //send pickup msg to the world
            Long msgSeqNum=worldHandler.getAndAddSeqNumToWorld();
            worldHandler.getUnAckedNums().add(msgSeqNum);
            WorldCommandSender worldCommandSender = applicationContext.getBean(WorldCommandSender.class);
            WorldUPSProto.UGoPickup uGoPickup = WorldUPSProto.UGoPickup.newBuilder()
                    .setTruckid(chosenTruck.getTruckId())
                    .setWhid(auCallTruck.getWhnum())
                    .setSeqnum(msgSeqNum)
                    .build();
            worldCommandSender.setUGoPickup(uGoPickup);
            Thread worldMsgSenderThread = new Thread(worldCommandSender);
            worldMsgSenderThread.start();
            //update orders in the DB
            for(Long shipId: auCallTruck.getShipidList()){//can't be empty list
                Order order = orderService.getOrderByShipId(shipId);
                order.setTruckId(chosenTruck.getTruckId());
                order.setWhId(auCallTruck.getWhnum());
                order.setShipmentStatus("truck en route to warehouse");
                try {
                    orderService.updateOrder(order);
                }catch (Exception e){//needed?
                    e.printStackTrace();
                    System.out.println("Incorrect shipId in handleAUCallTruck");
                }
            }
        }

    }
    public void handleAUUpdateTruckStatus(AmazonUPSProto.AUUpdateTruckStatus auUpdateTruckStatus){
        //send ack back first
        AmazonMessageSender amazonMessageSender=applicationContext.getBean(AmazonMessageSender.class);
        amazonMessageSender.setAck(auUpdateTruckStatus.getSeqnum());
        Thread msgSenderThread = new Thread(amazonMessageSender);
        msgSenderThread.start();
        if(amazonHandler.getSeqNumsFromAmazon().add(auUpdateTruckStatus.getSeqnum())){
            //update truck in the DB
            Truck truck=truckService.getTruckById(auUpdateTruckStatus.getTruckid());
            truck.setStatus(auUpdateTruckStatus.getStatus());
            truckService.updateTruck(truck);
        }
    }
    public void handleAUTruckGoDeliver(AmazonUPSProto.AUTruckGoDeliver auTruckGoDeliver){
        //send ack back first
        AmazonMessageSender amazonMessageSender=applicationContext.getBean(AmazonMessageSender.class);
        amazonMessageSender.setAck(auTruckGoDeliver.getSeqnum());
        Thread msgSenderThread = new Thread(amazonMessageSender);
        msgSenderThread.start();
        if(amazonHandler.getSeqNumsFromAmazon().add(auTruckGoDeliver.getSeqnum())){
            //update the truck in the DB, TODO: To be updated
            Truck truck = null;
            while(true) {//if the truck is busy at the moment, wait for 1s
                truck = truckService.getTruckById(auTruckGoDeliver.getTruckid());
                String truckStatus = truck.getStatus();
                if (truckStatus.equals("idle")||truckStatus.equals("loaded")||truckStatus.equals("delivering")){
                    truck.setStatus("delivering");
                    truckService.updateTruck(truck);
                    break;
                }
                try{
                    Thread.sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            Long msgSeqNum=worldHandler.getAndAddSeqNumToWorld();
            worldHandler.getUnAckedNums().add(msgSeqNum);
            WorldCommandSender worldCommandSender = applicationContext.getBean(WorldCommandSender.class);
            WorldUPSProto.UGoDeliver.Builder uGoDeliverBuilder = WorldUPSProto.UGoDeliver.newBuilder()
                    .setTruckid(truck.getTruckId())
                    .setSeqnum(msgSeqNum);
            //update the orders in the DB and build the msg sent to the world
            for(Long shipId: auTruckGoDeliver.getShipidList()){
                Order order = orderService.getOrderByShipId(shipId);
                order.setTruckId(truck.getTruckId());
                order.setDeliveringTime(LocalDateTime.now());
                order.setShipmentStatus("out for delivery");
                orderService.updateOrder(order);
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
            Thread worldMsgSenderThread = new Thread(worldCommandSender);
            worldMsgSenderThread.start();
        }
    }

    @Override
    public void run() {
        for(Long ack: auMessages.getAcksList()){
            handleAck(ack);
        }
        for(AmazonUPSProto.Err err: auMessages.getErrorList()){
            handleErr(err);
        }
        for(AmazonUPSProto.AUPlaceOrder auPlaceOrder: auMessages.getOrderList()){
            handleAUPlaceOrder(auPlaceOrder);
        }
        for(AmazonUPSProto.AUAssociateUserId auAssociateUserId: auMessages.getAssociateUserIdList()){
            handleAUAssociateUserId(auAssociateUserId);
        }
        for(AmazonUPSProto.AUCallTruck auCallTruck: auMessages.getCallTruckList()){
            handleAUCallTruck(auCallTruck);
        }
        for(AmazonUPSProto.AUUpdateTruckStatus auUpdateTruckStatus: auMessages.getUpdateTruckStatusList()){
            handleAUUpdateTruckStatus(auUpdateTruckStatus);
        }
        for(AmazonUPSProto.AUTruckGoDeliver auTruckGoDeliver: auMessages.getTruckGoDeliverList()){
            handleAUTruckGoDeliver(auTruckGoDeliver);
        }
    }
}
