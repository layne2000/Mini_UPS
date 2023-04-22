package com.example.handler;

import com.example.proto.world_ups.WorldUPSProto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
@Scope("prototype")
public class WorldResponseHandler implements Runnable{
    private final ApplicationContext applicationContext;
    private WorldHandler worldHandler;
    WorldUPSProto.UResponses response;


    @Autowired
    WorldResponseHandler(WorldHandler worldHandler, ApplicationContext applicationContext){
        this.worldHandler=worldHandler;
        this.applicationContext=applicationContext;
    }

    public void setResponse(WorldUPSProto.UResponses response) {
        this.response = response;
    }


    void handleAck(Long ack){

    }

    void handleUErr(WorldUPSProto.UErr uerr){

    }

    void handleFinished(Boolean finished){

    }

    void handleUFinished(WorldUPSProto.UFinished uFinished){

    }

    void handleDelivered(WorldUPSProto.UDeliveryMade uDeliveryMade){

    }

    void handleUTruck(WorldUPSProto.UTruck uTruck){

    }

    @Override
    public void run() {

    }
}
