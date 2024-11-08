package com.aric.middleware.dbrouter.test;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.dbrouter.mapper.UserMapper;
import com.aric.middleware.dbrouter.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@SpringBootTest
public class ApplicationTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    public void test_insertUser() {
        User user = new User();
        user.setUserId("100101");
        user.setUserNickName("aric");
        user.setUserHead("head");
        user.setUserPassword("******");

        userMapper.insertInto(user);
    }

    @Test
    public void test_queryUser() {
        User user = new User();
        user.setUserId("100101");

        User user1 = userMapper.queryUser(user);
        System.out.println("query user: " + JSON.toJSONString(user1));
    }

    @Test
    public void test_mysql() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:mysql://127.0.0.1:3306/bugstack_01?useUnicode=true", "root", "");
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("select * from user_00");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String userId = resultSet.getString("userId");
            System.out.println(id + ", " + userId);
        }
    }
}
