package com.example.handler;

import com.example.proto.world_ups.WorldUPSProto;
import com.google.protobuf.CodedOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

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
    private Boolean sendingErr;
    private Long seqNum;//seqNum for this particular msg, ack msg has no such field

    @Autowired
    WorldCommandSender(WorldHandler worldHandler){
        this.worldHandler=worldHandler;
    }

    public Boolean getSendingErr() {
        return sendingErr;
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

    public void setSeqNum(Long seqNum) {
        this.seqNum = seqNum;
    }

    @Override
    public void run() {
        WorldUPSProto.UCommands.Builder uCommandBuilder = WorldUPSProto.UCommands.newBuilder();
        if(uGoPickup!=null)
            uCommandBuilder.addPickups(uGoPickup);
        else if(uGoDeliver!=null)
            uCommandBuilder.addDeliveries(uGoDeliver);
        else if(uQuery != null)
            uCommandBuilder.addQueries(uQuery);
        else if(simSpeed!=null)
            uCommandBuilder.setSimspeed(simSpeed);
        else if(disconnect!=null)
            uCommandBuilder.setDisconnect(disconnect);
        else if(ack!=null)
            uCommandBuilder.addAcks(ack);
        WorldUPSProto.UCommands uCommand = uCommandBuilder.build();
        boolean sendOnce= (ack != null) || (simSpeed!=null) || (disconnect!=null); // these msg have no seqNum
        while(sendOnce||worldHandler.getUnAckedNums().contains(seqNum)) {//send every 3s
            try {
            //not sure
            synchronized (worldHandler.getWritingLock()) {
                    OutputStream outputStream = worldHandler.getClientSocketToWorld().getOutputStream();
                    CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
                    codedOutputStream.writeUInt32NoTag(uCommand.getSerializedSize());
                    uCommand.writeTo(codedOutputStream);
                    codedOutputStream.flush();
            }
            if(sendOnce) {//ack, simSpeed or disconnect msg only sends once
                break;
            }
            Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
                sendingErr = true;
            }
        }
    }
}
