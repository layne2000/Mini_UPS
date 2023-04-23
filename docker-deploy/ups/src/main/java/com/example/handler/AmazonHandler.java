package com.example.handler;

import com.example.proto.amazon_ups.AmazonUPSProto;
import com.example.proto.world_ups.WorldUPSProto;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class AmazonHandler implements Runnable{
    private final ApplicationContext applicationContext;
    private Socket clientSocketToAmazon;
    private volatile Long seqNumToAmazon = 0L;//next seqNum sent out
    private Set<Long> unAckedNums = Collections.newSetFromMap(new ConcurrentHashMap<>());//??why does IDE think it should be marked as final?
    private Set<Long> seqNumsFromAmazon = Collections.newSetFromMap(new ConcurrentHashMap<>());//to implement at most once semantics, for responseHandler
    private final Object writingLock = new Object();//used to lock the writing to socket

    @Autowired
    public AmazonHandler (ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Socket getClientSocketToAmazon() {
        return clientSocketToAmazon;
    }

    public Set<Long> getUnAckedNums() {
        return unAckedNums;
    }

    public Set<Long> getSeqNumsFromAmazon() {
        return seqNumsFromAmazon;
    }

    public Object getWritingLock() {
        return writingLock;
    }

    public void setClientSocketToAmazon(Socket clientSocketToAmazon) {
        this.clientSocketToAmazon = clientSocketToAmazon;
    }

    public synchronized Long getAndAddSeqNumToAmazon() {//avoid concurrency problem
        seqNumToAmazon += 1;
        return seqNumToAmazon-1;
    }

    public Long connectToAmazon() throws IOException {
        InputStream inputStream = clientSocketToAmazon.getInputStream();
        CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
        int size = codedInputStream.readRawVarint32();
        AmazonUPSProto.AUConnect response = AmazonUPSProto.AUConnect.parseFrom(codedInputStream.readRawBytes(size));
        //test
        System.out.println("Received response: " + response.toString());
        long worldId=response.getWorldid();

        AmazonUPSProto.UAConnected message = AmazonUPSProto.UAConnected.newBuilder().setWorldid(worldId).setResult("connected!").build();
        OutputStream outputStream = clientSocketToAmazon.getOutputStream();
        CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
        codedOutputStream.writeUInt32NoTag(message.getSerializedSize());
        message.writeTo(codedOutputStream);
        codedOutputStream.flush();
        return worldId;
    }

    @Override
    public void run() {
        //or specify a fixed number
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        while(true){
            try {
                InputStream inputStream = clientSocketToAmazon.getInputStream();
                CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
                int size = codedInputStream.readRawVarint32();
                AmazonUPSProto.AUMessages auMessages = AmazonUPSProto.AUMessages.parseFrom(codedInputStream.readRawBytes(size));
                //can get multiple instances, not sure??
                AmazonResponseHandler amazonResponseHandler = applicationContext.getBean(AmazonResponseHandler.class);
                amazonResponseHandler.setAuMessages(auMessages);
                executor.submit(amazonResponseHandler);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
