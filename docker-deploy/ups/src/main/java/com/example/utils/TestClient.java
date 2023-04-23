package com.example.utils;

import com.example.proto.world_ups.WorldUPSProto;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TestClient {
    private final String host;
    private final int port;
    private final Socket socket;
    private long worldid;
    public TestClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        socket = new Socket(host, port);
    }


    public void connectToWorld() throws Exception {
        WorldUPSProto.UConnect.Builder messageBuilder = WorldUPSProto.UConnect.newBuilder()
                .setIsAmazon(false);//TODO: add worldid

        for (int i = 0; i < 50; ++i) {
            WorldUPSProto.UInitTruck truck = WorldUPSProto.UInitTruck.newBuilder()
                    .setId(i)
                    .setX(0)
                    .setY(0)
                    .build();
            messageBuilder.addTrucks(truck);
        }

        WorldUPSProto.UConnect message = messageBuilder.build();
        OutputStream outputStream = socket.getOutputStream();
        CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
        codedOutputStream.writeUInt32NoTag(message.getSerializedSize());//changed from writeRawVarint32(int)
        message.writeTo(codedOutputStream);
        codedOutputStream.flush();

        InputStream inputStream = socket.getInputStream();
        CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
        int size = codedInputStream.readRawVarint32();
        WorldUPSProto.UConnected response = WorldUPSProto.UConnected.parseFrom(codedInputStream.readRawBytes(size));
        System.out.println("Received response: " + response.toString());

        // Extract the fields from the response
        String result = response.getResult();
        if(!result.equals("connected!")){
            throw new Exception("Failed to connect to the world");
        }
//        long worldid = response.getWorldid();
//        System.out.println("WorldID: " + worldid);
//        System.out.println("Result: " + result);

    }

    //call close() if this client will no longer be used
    public void close() throws IOException {
        socket.close();
    }

    //test
    public static void main(String[] args){
        try {
            TestClient clientToWorld = new TestClient("vcm-32315.vm.duke.edu",12345);
            clientToWorld.connectToWorld();
            clientToWorld.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
