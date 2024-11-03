package com.aric.middleware;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JdbcDemo {
    private static final String URL = "jdbc:mysql://localhost:3306/demo";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public void insertData(String name) {
        String sql = "insert into users(name) values(?)";
        try(
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
            System.out.println("Inserted: " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetchData() {
        String sql = "select * from users;";

        try (
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ) {
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("fetch data: id  name");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println(id + ", " + name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateData(int id, String name) {
        String sql = "update users set name = ? where id = ?";

        try (
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);

            preparedStatement.executeUpdate();
            System.out.println("update for id = " + id + ", name = " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteData(int id) {
        String sql = "delete from users where id = ?";
        try (
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
        ) {
            preparedStatement.setInt(1, id);
            int i = preparedStatement.executeUpdate();
            System.out.println("delete id: " + id + ", result: " + i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JdbcDemo jdbcDemo = new JdbcDemo();
        jdbcDemo.insertData("aric");
        jdbcDemo.insertData("xiaoming");
        jdbcDemo.fetchData();
        jdbcDemo.updateData(3, "xiaoli");
        jdbcDemo.deleteData(4);
    }


}
