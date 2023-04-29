package com.example.handler;

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

import static java.lang.System.exit;

@Component
public class WorldHandler implements Runnable {
    private final ApplicationContext applicationContext;
    private Socket clientSocketToWorld;
    private volatile Long seqNumToWorld = 0L;//for sender, next seqNum sent out
    //thread-safe, but still need to ensure atomicity and consistency for compound operations(e.g., checking if a key exists and then adding it if it doesn't)
    private Set<Long> unAckedNums = Collections.newSetFromMap(new ConcurrentHashMap<>());//for sender, ??why does IDE think it should be marked as final?
    private Set<Long> seqNumsFromWorld = Collections.newSetFromMap(new ConcurrentHashMap<>());//to implement at most once semantics, for responseHandler
    private Long worldId;
    private volatile Boolean isConnected;//check if currently connected to world
    private volatile Boolean hasConnected;//indicate whether connected ever, used to avoid adding duplicate trucks in the world
    private final Object writingLock = new Object();//used to lock the writing to socket

    @Autowired
    public WorldHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Socket getClientSocketToWorld() {
        return clientSocketToWorld;
    }

    public Set<Long> getUnAckedNums() {
        return unAckedNums;
    }

    public Set<Long> getSeqNumsFromWorld() {
        return seqNumsFromWorld;
    }

    public Long getWorldId() {
        return worldId;
    }

    public Object getWritingLock() {
        return writingLock;
    }

    public synchronized Long getAndAddSeqNumToWorld() {//avoid concurrency problem
        seqNumToWorld += 1;
        return seqNumToWorld - 1;
    }

    private void sendUConnect() throws IOException, InterruptedException {
        WorldUPSProto.UConnect.Builder messageBuilder = WorldUPSProto.UConnect.newBuilder()
                .setWorldid(worldId)
                .setIsAmazon(false);

        if (hasConnected==null) {
            for (int i = 0; i < 3; ++i) {
                WorldUPSProto.UInitTruck truck = WorldUPSProto.UInitTruck.newBuilder()
                        .setId(i)
                        .setX(0)
                        .setY(0)
                        .build();
                messageBuilder.addTrucks(truck);
            }
        }

        WorldUPSProto.UConnect message = messageBuilder.build();

        while (!isConnected) {//send the connecting msg every 1s until receiving the response
            OutputStream outputStream = clientSocketToWorld.getOutputStream();
            CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
            codedOutputStream.writeUInt32NoTag(message.getSerializedSize());//changed from writeRawVarint32(int)
            message.writeTo(codedOutputStream);
            codedOutputStream.flush();
            Thread.sleep(1000); // Sleep for 1 second
        }
    }

    public void connectToWorld() throws Exception {

        isConnected = false;
        // Create a Runnable instance using a lambda expression
        Runnable task = () -> {
            try {
                sendUConnect();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };

        // Create and start a new thread with the Runnable instance
        Thread thread = new Thread(task);
        thread.start();

        InputStream inputStream = clientSocketToWorld.getInputStream();
        CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
        int size = codedInputStream.readRawVarint32();//blocking
        isConnected = true;
        hasConnected = true;
        WorldUPSProto.UConnected response = WorldUPSProto.UConnected.parseFrom(codedInputStream.readRawBytes(size));
        String result = response.getResult();
        if (!result.equals("connected!")) {
            throw new Exception("Failed to connect to the world");
        } else {
            System.out.println("Connect to world " + response.getWorldid());
        }
    }

    public void setClientSocketToWorld(Socket clientSocketToWorld) {
        this.clientSocketToWorld = clientSocketToWorld;
    }

    public void setWorldId(Long worldId) {
        this.worldId = worldId;
    }

    @Override
    public void run() {
        //or specify a fixed number
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        while (true) {
            try {
                if (clientSocketToWorld.isClosed()) {
                    connectToWorld();
                }
                InputStream inputStream = clientSocketToWorld.getInputStream();
                CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
                int size = codedInputStream.readRawVarint32();
                WorldUPSProto.UResponses response = WorldUPSProto.UResponses.parseFrom(codedInputStream.readRawBytes(size));
                //test
                System.out.println("world response: "+response);
                //can get multiple instances, not sure??
                WorldResponseHandler worldResponseHandler = applicationContext.getBean(WorldResponseHandler.class);
                worldResponseHandler.setResponse(response);
                executor.submit(worldResponseHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
