package com.example;

import com.example.handler.AmazonHandler;
import com.example.handler.WorldHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorldAmazonBackend implements Runnable{
    private WorldHandler worldHandler;
    private AmazonHandler amazonHandler;

    @Autowired
    WorldAmazonBackend(WorldHandler worldHandler, AmazonHandler amazonHandler){
        this.worldHandler=worldHandler;
        this.amazonHandler=amazonHandler;
    }

    @Override
    public void run() {

    }
}
