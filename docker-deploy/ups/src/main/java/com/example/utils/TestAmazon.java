package com.example.utils;

import com.example.proto.amazon_ups.AmazonUPSProto;
import com.example.proto.world_amazon.WorldAmazonProto;
import com.example.proto.world_ups.WorldUPSProto;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TestAmazon {
    private final ServerSocket serverSocket;
    private final int port;//open port as the server
    private final Socket clientSocket;
    private final String worldHost;
    private long worldid;

    public TestAmazon(int port, String worldHost) throws IOException {
        this.port = port;
        this.worldHost = worldHost;
        clientSocket = new Socket(worldHost, 23456);
        serverSocket = new ServerSocket(port);
    }

    public TestAmazon() throws IOException {
        this.port = 23333;
        this.worldHost = "127.0.0.1";
        clientSocket = new Socket(worldHost, 23456);
        serverSocket = new ServerSocket(port);
    }

    public void closeClient() throws IOException {
        clientSocket.close();
    }

    public void closeServer() throws IOException {
        serverSocket.close();
    }

    public void connectToWorld() throws Exception {
        WorldAmazonProto.AConnect.Builder messageBuilder = WorldAmazonProto.AConnect.newBuilder()
                .setIsAmazon(true);

        for (int i = 0; i < 10; ++i) {
            WorldAmazonProto.AInitWarehouse initwh = WorldAmazonProto.AInitWarehouse.newBuilder()
                    .setId(i)
                    .setX(i)
                    .setY(i + 5)
                    .build();
            messageBuilder.addInitwh(initwh);
        }

        WorldAmazonProto.AConnect message = messageBuilder.build();
        OutputStream outputStream = clientSocket.getOutputStream();
        CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
        codedOutputStream.writeUInt32NoTag(message.getSerializedSize());//changed from writeRawVarint32(int)
        message.writeTo(codedOutputStream);
        codedOutputStream.flush();

        InputStream inputStream = clientSocket.getInputStream();
        CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
        int size = codedInputStream.readRawVarint32();
        WorldAmazonProto.AConnected response = WorldAmazonProto.AConnected.parseFrom(codedInputStream.readRawBytes(size));
//        System.out.println("Received response: " + response.toString());

        // Extract the fields from the response
        String result = response.getResult();
        if (!result.equals("connected")) {
            throw new Exception("Failed to connect to the world");
        }
        this.worldid = response.getWorldid();
    }

    public void connectToUPS() throws Exception {
        Socket upsSocket = serverSocket.accept();

        AmazonUPSProto.AUConnect message = AmazonUPSProto.AUConnect.newBuilder().setWorldid(worldid).build();
        OutputStream outputStream = upsSocket.getOutputStream();
        CodedOutputStream codedOutputStream = CodedOutputStream.newInstance(outputStream);
        codedOutputStream.writeUInt32NoTag(message.getSerializedSize());//changed from writeRawVarint32(int)
        message.writeTo(codedOutputStream);
        codedOutputStream.flush();

        InputStream inputStream = upsSocket.getInputStream();
        CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
        int size = codedInputStream.readRawVarint32();
        AmazonUPSProto.UAConnected response = AmazonUPSProto.UAConnected.parseFrom(codedInputStream.readRawBytes(size));
        System.out.println("Received response: " + response.toString());
        String result = response.getResult();
        if(!result.equals("connected!")){
            throw new Exception("Failed to connect to UPS");
        }
    }

    public static void main(String[] args) throws IOException {

    }
}

