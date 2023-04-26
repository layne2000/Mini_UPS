package com.example.handler;

import com.example.proto.amazon_ups.AmazonUPSProto;
import com.google.protobuf.CodedOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

@Component
@Scope("prototype")
public class AmazonMessageSender implements Runnable {
    private AmazonHandler amazonHandler;
    private AmazonUPSProto.UATruckArrived uaTruckArrived;
    private AmazonUPSProto.UAUpdatePackageStatus uaUpdatePackageStatus;
    private AmazonUPSProto.UAUpdatePackageAddress uaUpdatePackageAddress;
    private AmazonUPSProto.Err err;
    private Long ack;
    private Boolean sendingErr;
    private Long seqNum;

    @Autowired
    AmazonMessageSender(AmazonHandler amazonHandler) {
        this.amazonHandler = amazonHandler;
    }

    public Boolean getSendingErr() {
        return sendingErr;
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

    public void setSeqNum(Long seqNum) {
        this.seqNum = seqNum;
    }

    @Override
    public void run() {
        AmazonUPSProto.UAMessages.Builder uaMessageBuilder = AmazonUPSProto.UAMessages.newBuilder();
        if (uaTruckArrived != null)
            uaMessageBuilder.addTruckArrived(uaTruckArrived);
        else if (uaUpdatePackageStatus != null)
            uaMessageBuilder.addUpdatePackageStatus(uaUpdatePackageStatus);
        else if (uaUpdatePackageAddress != null)
            uaMessageBuilder.addUpdatePackageAddress(uaUpdatePackageAddress);
        else if (err != null)
            uaMessageBuilder.addError(err);
        else if (ack != null)
            uaMessageBuilder.addAcks(ack);
        AmazonUPSProto.UAMessages uaMessage = uaMessageBuilder.build();
        boolean sendOnce = (ack != null);
        if (amazonHandler.getClientSocketToAmazon() == null) {
            System.out.println("Haven't connected to Amazon yet, can't send msg!");
            return;
        }
        while (sendOnce || amazonHandler.getUnAckedNums().contains(seqNum)) {
            try {
                synchronized (amazonHandler.getWritingLock()) {
                    OutputStream outputStream = amazonHandler.getClientSocketToAmazon().getOutputStream();
                    CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
                    codedOutputStream.writeUInt32NoTag(uaMessage.getSerializedSize());
                    uaMessage.writeTo(codedOutputStream);
                    codedOutputStream.flush();
                }
                if (sendOnce) {//send only once if it's ack msg
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
