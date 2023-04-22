package com.example.service;

import com.example.mapper.UserMapper;
import com.example.model.Order;
import com.example.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

//for authentication
@Service
public class UserService {
    private final UserMapper userMapper;

//Starting with Spring 4.3, if a class has only one constructor, the @Autowired annotation is no longer required
    @Autowired
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void addUser(User user) throws SQLException {
        userMapper.insertUser(user);
    }

    public User getUserById(String userId){
        return userMapper.getUserById(userId);
    }

    public void updateUser(User user)throws SQLException {
        userMapper.updateUser(user);
    }

    public void deleteUserById(String userId)throws SQLException {
        userMapper.deleteUserById(userId);
    }

    public List<Order> getOrdersByUserId(String userId){//return empty Arraylist?
        return userMapper.getOrdersByUserId(userId);
    }

}
