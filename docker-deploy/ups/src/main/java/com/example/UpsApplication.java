package com.example;

import com.example.handler.AmazonHandler;
import com.example.handler.WorldHandler;
import com.example.mapper.DatabaseInitializer;
import com.example.mapper.TruckMapper;
import com.example.model.Truck;
import com.example.utils.TestComponent;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.System.exit;

@SpringBootApplication
public class UpsApplication {

	public static void main(String[] args) throws Exception {
//		if(args.length!=1){//world server host, Amazon host, Amazon Port
//			throw new Exception("Too many or too few arguments!");
//		}
		String worldServerIP = "localhost";

		//traditional mybatis approach to initialize DB
		String resource = "mybatis-config.xml";
		InputStream inputStream;
		SqlSessionFactory sqlSessionFactory;

		try {
			inputStream = Resources.getResourceAsStream(resource);
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
		} catch (IOException e) {
			throw new RuntimeException("Error initializing SqlSessionFactory", e);
		}
		try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
			DatabaseInitializer databaseInitializer = sqlSession.getMapper(DatabaseInitializer.class);
			databaseInitializer.dropOrdersTable();//has foreign key relationship, drop first
			databaseInitializer.dropUsersTable();
			databaseInitializer.dropTrucksTable();
			databaseInitializer.createUsersTable();
			databaseInitializer.createTrucksTable();
			databaseInitializer.createOrdersTable();//has foreign key relationship, create last
			sqlSession.commit();
			//sqlSession.close(); //sqlSession will be closed automatically after leaving try block
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Can't connect to the database");
			exit(1);
		}
//		initialize trucks in the DB
		try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
			TruckMapper truckMapper = sqlSession.getMapper(TruckMapper.class);
			for(int i=0;i<10;++i){
				Truck truck = new Truck(i, "IDLE", 0, 0);
				truckMapper.insertTruck(truck);
			}
			sqlSession.commit();
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Can't create trucks");
			exit(1);
		}

		//doesn't create a new thread, just operates asynchronously
		ApplicationContext applicationContext = SpringApplication.run(UpsApplication.class, args);

//		// Get the TestComponent instance from the application context
//		TestComponent testComponent = applicationContext.getBean(TestComponent.class);
//
//		// Now you can use the TestComponent instance and call its methods
//		testComponent.test();

		AmazonHandler amazonHandler = applicationContext.getBean(AmazonHandler.class);
		WorldHandler worldHandler = applicationContext.getBean(WorldHandler.class);
		try (
				Socket clientSocketToWorld = new Socket(worldServerIP, 12345);
				ServerSocket serverSocket = new ServerSocket(34567);
				Socket clientSocketToAmazon = serverSocket.accept();
		) {
			serverSocket.close();
			amazonHandler.setClientSocketToAmazon(clientSocketToAmazon);
			worldHandler.setWorldId(amazonHandler.connectToAmazon());//connect to Amazon here!
			worldHandler.setClientSocketToWorld(clientSocketToWorld);
			worldHandler.connectToWorld();
			// Create and start two threads
			Thread amazonHandlerThread = new Thread(amazonHandler);
			amazonHandlerThread.start();
			Thread worldHandlerThread = new Thread(worldHandler);
			worldHandlerThread.start();
			worldHandlerThread.join();
			amazonHandlerThread.join();
		} catch (Exception e) {
			e.printStackTrace();
			exit(1);
		}
	}

}
