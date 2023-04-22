package com.example.handler;

import com.example.proto.world_ups.WorldUPSProto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class WorldCommandSender implements Runnable{//send only one message at a time
    private WorldHandler worldHandler;
    private WorldUPSProto.UGoPickup uGoPickup;
    private WorldUPSProto.UGoDeliver uGoDeliver;
    private Integer simSpeed;
    private Boolean disconnect;
    private WorldUPSProto.UQuery uQuery;
    private Long ack;

    @Autowired
    WorldCommandSender(WorldHandler worldHandler){
        this.worldHandler=worldHandler;
    }

    public void setUGoPickup(WorldUPSProto.UGoPickup uGoPickup) {
        this.uGoPickup = uGoPickup;
    }

    public void setUGoDeliver(WorldUPSProto.UGoDeliver uGoDeliver) {
        this.uGoDeliver = uGoDeliver;
    }

    public void setSimSpeed(Integer simSpeed) {
        this.simSpeed = simSpeed;
    }

    public void setDisconnect(Boolean disconnect) {
        this.disconnect = disconnect;
    }

    public void setUQuery(WorldUPSProto.UQuery uQuery) {
        this.uQuery = uQuery;
    }

    public void setAck(Long ack) {
        this.ack = ack;
    }

    @Override
    public void run() {

    }
}
