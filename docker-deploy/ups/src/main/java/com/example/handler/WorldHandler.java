package com.example.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.Socket;
import java.util.HashSet;

@Component
public class WorldHandler implements Runnable{
    private final ApplicationContext applicationContext;
    private Socket clientSocketToWorld;
    private Long seqNumToWorld = 0L;//for sender, next seqNum sent out
    private HashSet<Long> unAckedNums = new HashSet<>();//for sender, ??why does IDE think it should be marked as final?
    private HashSet<Long> seqNumsFromWorld = new HashSet<>();//to implement at most once semantics, for responseHandler
    private Long worldId;

    @Autowired
    public WorldHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Socket getClientSocketToWorld() {
        return clientSocketToWorld;
    }

    public HashSet<Long> getUnAckedNums() {
        return new HashSet<>(unAckedNums);//avoid concurrency problem???
    }

    public HashSet<Long> getSeqNumsFromWorld(){
        return new HashSet<>(seqNumsFromWorld);
    }

    public Long getWorldId() {
        return worldId;
    }

    public void connectToWorld(){

    }

    public void setClientSocketToWorld(Socket clientSocketToWorld) {
        this.clientSocketToWorld = clientSocketToWorld;
    }

    public synchronized Long getAndAddSeqNumToWorld() {//avoid concurrency problem
        seqNumToWorld += 1;
        return seqNumToWorld-1;
    }

    public synchronized void addUnAckedNum(Long unAckedNum) {
        unAckedNums.add(unAckedNum);
    }

    public synchronized void addSeqNumFromWorld(Long seqNumFromWorld){
        seqNumsFromWorld.add(seqNumFromWorld);
    }
    public void setWorldId(Long worldId) {
        this.worldId = worldId;
    }

    @Override
    public void run() {

    }
}
