package com.example.mapper;

import com.example.model.Order;
import com.example.model.User;
import org.apache.ibatis.annotations.*;

import java.util.List;
@Mapper
public interface UserMapper {
    @Insert("INSERT INTO users (user_id, email, password) VALUES (#{userId}, #{email}, #{password})")
    void insertUser(User user);

    @Select("SELECT * FROM users WHERE user_id = #{userId}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "email", column = "email"),
            @Result(property = "password", column = "password"),
            @Result(property = "orders", column = "user_id", javaType = List.class, many = @Many(select = "getOrdersByUserId"))
    })
    User getUserById(String userId);

    @Update("UPDATE users SET email = #{email}, password = #{password} WHERE user_id = #{userId}")
    void updateUser(User user);

    @Delete("DELETE FROM users WHERE user_id = #{userId}")
    void deleteUserById(String userId);

    @Select("SELECT * FROM orders WHERE user_id = #{userId}")
    List<Order> getOrdersByUserId(String userId);
}
