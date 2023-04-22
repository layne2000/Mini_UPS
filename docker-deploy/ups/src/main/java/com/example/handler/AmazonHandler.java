package com.example.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.HashSet;

@Component
public class AmazonHandler implements Runnable{
    private final ApplicationContext applicationContext;
    private Socket clientSocketToAmazon;
    private Long seqNumToAmazon = 0L;//next seqNum sent out
    private HashSet<Long> unAckedNums = new HashSet<>();//??why does IDE think it should be marked as final?
    private HashSet<Long> seqNumsFromAmazon = new HashSet<>();//to implement at most once semantics, for responseHandler

    @Autowired
    public AmazonHandler (ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Socket getClientSocketToAmazon() {
        return clientSocketToAmazon;
    }

    public HashSet<Long> getUnAckedNums() {
        return new HashSet<>(unAckedNums);//avoid concurrency problem?
    }

    public HashSet<Long> getSeqNumsFromAmazon(){
        return new HashSet<>(seqNumsFromAmazon);
    }
    public void setClientSocketToAmazon(Socket clientSocketToAmazon) {
        this.clientSocketToAmazon = clientSocketToAmazon;
    }

    public synchronized Long getAndAddSeqNumToAmazon() {//avoid concurrency problem
        seqNumToAmazon += 1;
        return seqNumToAmazon-1;
    }

    public synchronized void addUnAcked(long unAckedNum) {
        unAckedNums.add(unAckedNum);
    }

    public synchronized void addSeqNumFromAmazon(Long seqNumFromAmazon){
        seqNumsFromAmazon.add(seqNumFromAmazon);
    }

    public Long connectToWorld(){
        return 0L;//To be modified
    }

    @Override
    public void run() {

    }
}
