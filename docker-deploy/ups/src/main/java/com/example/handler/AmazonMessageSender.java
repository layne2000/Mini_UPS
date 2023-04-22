package com.example.handler;

import com.example.proto.amazon_ups.AmazonUPSProto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class AmazonMessageSender implements Runnable{
    private WorldHandler worldHandler;
    private AmazonUPSProto.UATruckArrived uaTruckArrived;
    private AmazonUPSProto.UAUpdatePackageStatus uaUpdatePackageStatus;
    private AmazonUPSProto.UAUpdatePackageAddress uaUpdatePackageAddress;
    private AmazonUPSProto.Err err;
    private Long ack;

    @Autowired
    AmazonMessageSender(WorldHandler worldHandler){
        this.worldHandler=worldHandler;
    }

    public void setUaTruckArrived(AmazonUPSProto.UATruckArrived uaTruckArrived) {
        this.uaTruckArrived = uaTruckArrived;
    }

    public void setUaUpdatePackageStatus(AmazonUPSProto.UAUpdatePackageStatus uaUpdatePackageStatus) {
        this.uaUpdatePackageStatus = uaUpdatePackageStatus;
    }

    public void setUaUpdatePackageAddress(AmazonUPSProto.UAUpdatePackageAddress uaUpdatePackageAddress) {
        this.uaUpdatePackageAddress = uaUpdatePackageAddress;
    }

    public void setErr(AmazonUPSProto.Err err) {
        this.err = err;
    }

    public void setAck(Long ack) {
        this.ack = ack;
    }

    @Override
    public void run() {

    }
}
