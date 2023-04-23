package com.example;

import com.example.handler.AmazonHandler;
import com.example.handler.WorldHandler;
import com.example.mapper.DatabaseInitializer;
import com.example.mapper.TruckMapper;
import com.example.model.Truck;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import static java.lang.System.exit;

@SpringBootApplication
public class UpsApplication {

	public static void main(String[] args) throws Exception {
		if(args.length!=3){//world server host, Amazon host, Amazon Port
			throw new Exception("Too many or too few arguments!");
		}

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

		try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
			TruckMapper truckMapper = sqlSession.getMapper(TruckMapper.class);
			for(int i=0;i<100;++i){
				Truck truck = new Truck(i, "idle", 0, 0);
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

//		// Get the TestTestComponent instance from the application context
//		TestTestComponent testTestComponent = applicationContext.getBean(TestTestComponent.class);
//
//		// Now you can use the TestTestComponent instance and call its methods
//		testTestComponent.test();

		AmazonHandler amazonHandler = applicationContext.getBean(AmazonHandler.class);
		WorldHandler worldHandler = applicationContext.getBean(WorldHandler.class);
		try (
				Socket clientSocketToWorld = new Socket(args[0], 12345);
				Socket clientSocketToAmazon = new Socket(args[1], Integer.parseInt(args[2]));
		) {
			amazonHandler.setClientSocketToAmazon(clientSocketToAmazon);
			worldHandler.setClientSocketToWorld(clientSocketToWorld);
			worldHandler.setWorldId(amazonHandler.connectToAmazon());
			worldHandler.connectToWorld();
			// Create and start two threads
			Thread amazonHandlerThread = new Thread(amazonHandler);
			Thread worldHandlerThread = new Thread(worldHandler);
			amazonHandlerThread.start();
			worldHandlerThread.start();

		} catch (Exception e) {
			e.printStackTrace();
			exit(1);
		}

	}

}
