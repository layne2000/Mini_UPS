package com.example.handler;

import com.example.proto.amazon_ups.AmazonUPSProto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AmazonResponseHandler implements Runnable{
    private final ApplicationContext applicationContext;
    private AmazonHandler amazonHandler;
    AmazonUPSProto.AUMessages auMessages;

    @Autowired
    AmazonResponseHandler(ApplicationContext applicationContext, AmazonHandler amazonHandler){
        this.applicationContext = applicationContext;
        this.amazonHandler = amazonHandler;
    }

    public void setAuMessages(AmazonUPSProto.AUMessages auMessages) {
        this.auMessages = auMessages;
    }

    public void handleAck(Long ack){

    }
    public void handleErr(AmazonUPSProto.Err err){

    }
    public void handleAUPlaceOrder(AmazonUPSProto.AUPlaceOrder auPlaceOrder){

    }
    public void handleAUAssociateUserId(AmazonUPSProto.AUAssociateUserId auAssociateUserId){

    }
    public void handleAUCallTruck(AmazonUPSProto.AUCallTruck auCallTruck){

    }
    public void handleAUUpdateTruckStatus(AmazonUPSProto.AUUpdateTruckStatus auUpdateTruckStatus){

    }
    public void handleAUTruckGoDeliver(AmazonUPSProto.AUTruckGoDeliver auTruckGoDeliver){

    }

    @Override
    public void run() {

    }
}
