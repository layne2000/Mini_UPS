package com.example;

import com.example.mapper.DatabaseInitializer;
import com.example.mapper.TruckMapper;
import com.example.mapper.UserMapper;
import com.example.model.Order;
import com.example.model.Truck;
import com.example.model.User;
import com.example.utils.TestTestComponent;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import static java.lang.System.exit;

@SpringBootApplication
public class UpsApplication {

	public static void main(String[] args) throws SQLException {
		//traditional mybatis approach
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
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("Can't connect to the database");
			exit(1);
		}

		ApplicationContext applicationContext = SpringApplication.run(UpsApplication.class, args);

		// Get the TestTestComponent instance from the application context
		TestTestComponent testTestComponent = applicationContext.getBean(TestTestComponent.class);

		// Now you can use the TestTestComponent instance and call its methods
		testTestComponent.test();

	}

}
