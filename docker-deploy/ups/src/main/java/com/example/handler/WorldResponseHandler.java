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
import java.util.HashSet;
import java.util.List;

@Component
@Scope("prototype")
public class WorldResponseHandler implements Runnable{
    private final ApplicationContext applicationContext;
    private WorldHandler worldHandler;
    private AmazonHandler amazonHandler;
    private final UserService userService;
    private final TruckService truckService;
    private final OrderService orderService;
    WorldUPSProto.UResponses response;

    @Autowired
    WorldResponseHandler(WorldHandler worldHandler, AmazonHandler amazonHandler, ApplicationContext applicationContext, UserService userService, TruckService truckService, OrderService orderService){
        this.worldHandler=worldHandler;
        this.amazonHandler=amazonHandler;
        this.applicationContext=applicationContext;
        this.userService=userService;
        this.truckService=truckService;
        this.orderService=orderService;
    }

    public void setResponse(WorldUPSProto.UResponses response) {
        this.response = response;
    }

    void handleAck(Long ack){
        //if there's no such num in the unAckedNum, it will return false and do nothing
        worldHandler.getUnAckedNums().remove(ack);
    }

    void handleUErr(WorldUPSProto.UErr uerr){//shouldn't happen?
        //send ack back first
        WorldCommandSender worldCommandSender = applicationContext.getBean(WorldCommandSender.class);
        worldCommandSender.setAck(uerr.getSeqnum());
        Thread msgSenderThread = new Thread(worldCommandSender);
        msgSenderThread.start();
        //do the corresponding operations, first time received and later
        System.out.println(uerr.getErr()+" on the msg of "+uerr.getOriginseqnum());
        worldHandler.getUnAckedNums().remove(uerr.getOriginseqnum());
    }

    void handleFinished(Boolean finished){//true or false, null has been excluded in the outer func
//        if(finished){//what if send finished multiple times?
//            try {
//                worldHandler.connectToWorld();//how to solve the concurrency problem here??
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
        System.out.println("World server: finished "+finished);
    }

    void handleUFinished(WorldUPSProto.UFinished uFinished){
        //send ack back first
        WorldCommandSender worldCommandSender = applicationContext.getBean(WorldCommandSender.class);
        worldCommandSender.setAck(uFinished.getSeqnum());
        Thread worldMsgSenderThread = new Thread(worldCommandSender);
        worldMsgSenderThread.start();
        //act only on the first time receiving this msg
        if(worldHandler.getSeqNumsFromWorld().add(uFinished.getSeqnum())){
            if(uFinished.getStatus().equals("arrive warehouse")){
                //TODO: don't need transaction because this truck is at traveling status, won't be influenced by other thread
                //update truck in the DB
                Truck truck = truckService.getTruckById(uFinished.getTruckid());
                truck.setStatus("arrive warehouse");
                truck.setX(uFinished.getX());
                truck.setY(uFinished.getY());
                truckService.updateTruck(truck);
                //update orders in the DB !!
                int whId = truck.getWhId(); // must have whId at this time
                for(Order order: truckService.getOrdersByTruckId(uFinished.getTruckid())){
                    //second check condition is to avoid the situation where there are packages from the same warehouse but are out of delivery
                    //int == int?
                    if(order.getWhId()==whId&&order.getShipmentStatus().equals("truck en route to warehouse")) {
                        order.setShipmentStatus("truck waiting for package");
                        orderService.updateOrder(order);
                    }
                }
                //send msg to Amazon
                Long msgSeqNum=amazonHandler.getAndAddSeqNumToAmazon();
                amazonHandler.getUnAckedNums().add(msgSeqNum);
                AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
                AmazonUPSProto.UATruckArrived uaTruckArrived = AmazonUPSProto.UATruckArrived.newBuilder()
                        .setTruckid(uFinished.getTruckid())
                        .setWhid(whId)
                        .setSeqnum(msgSeqNum)
                        .build();
                amazonMessageSender.setUaTruckArrived(uaTruckArrived);
                amazonMessageSender.setSeqNum(msgSeqNum);//!!!!
                Thread amazonMsgSenderThread = new Thread(amazonMessageSender);
                amazonMsgSenderThread.start();
            }
            else if(uFinished.getStatus().equals("idle")){
                Truck truck = truckService.getTruckById(uFinished.getTruckid());
                truck.setStatus("idle");
                truck.setX(uFinished.getX());
                truck.setY(uFinished.getY());
                truckService.updateTruck(truck);
            }
            else{
                System.out.println("ERROR: UFinished status is "+uFinished.getStatus());
            }
        }
    }

    void handleDelivered(WorldUPSProto.UDeliveryMade uDeliveryMade){
        //send ack back first
        WorldCommandSender worldCommandSender = applicationContext.getBean(WorldCommandSender.class);
        worldCommandSender.setAck(uDeliveryMade.getSeqnum());
        Thread worldMsgSenderThread = new Thread(worldCommandSender);
        worldMsgSenderThread.start();
        //act only on the first time receiving this msg
        if(worldHandler.getSeqNumsFromWorld().add(uDeliveryMade.getSeqnum())){
            //order
            Order order = orderService.getOrderByShipId(uDeliveryMade.getPackageid());
            order.setTruckId(null);//detach order info from the truck
            order.setShipmentStatus("delivered");
            order.setDeliveredTime(LocalDateTime.now());
            orderService.updateOrder(order);
            //truck, TODO: lost update problem?
            Truck truck = truckService.getTruckById(uDeliveryMade.getTruckid());
            truck.setX(order.getX());
            truck.setY(order.getY());
            truckService.updateTruck(truck);
            //send msg to Amazon
            Long msgSeqNum=amazonHandler.getAndAddSeqNumToAmazon();
            amazonHandler.getUnAckedNums().add(msgSeqNum);
            AmazonMessageSender amazonMessageSender = applicationContext.getBean(AmazonMessageSender.class);
            AmazonUPSProto.UAUpdatePackageStatus uaUpdatePackageStatus = AmazonUPSProto.UAUpdatePackageStatus.newBuilder()
                    .setShipid(uDeliveryMade.getPackageid())
                    .setStatus("delivered")
                    .setSeqnum(msgSeqNum)
                    .build();
            amazonMessageSender.setUaUpdatePackageStatus(uaUpdatePackageStatus);
            amazonMessageSender.setSeqNum(msgSeqNum);//!!!!
            Thread amazonMsgSenderThread = new Thread(amazonMessageSender);
            amazonMsgSenderThread.start();
        }
    }

    void handleUTruck(WorldUPSProto.UTruck uTruck){
        //send ack back first
        WorldCommandSender worldCommandSender = applicationContext.getBean(WorldCommandSender.class);
        worldCommandSender.setAck(uTruck.getSeqnum());
        Thread worldMsgSenderThread = new Thread(worldCommandSender);
        worldMsgSenderThread.start();

        System.out.println("Truck with id "+uTruck.getTruckid()+" is on ("+uTruck.getX()+","+uTruck.getY()+") with the status of "+uTruck.getStatus());
    }

    @Override
    public void run() {
        //pay attention to the difference between receiving the first time and later
        for(Long ack: response.getAcksList()){
            handleAck(ack);
        }
        for(WorldUPSProto.UErr uerr: response.getErrorList()){
            handleUErr(uerr);
        }
        //not sure
        if(response.hasFinished()){
            handleFinished(response.getFinished());
        }
        for(WorldUPSProto.UFinished uFinished: response.getCompletionsList()){
            handleUFinished(uFinished);
        }
        for(WorldUPSProto.UDeliveryMade uDeliveryMade: response.getDeliveredList()){
            handleDelivered(uDeliveryMade);
        }
        for(WorldUPSProto.UTruck uTruck: response.getTruckstatusList()){
            handleUTruck(uTruck);
        }
    }
}
