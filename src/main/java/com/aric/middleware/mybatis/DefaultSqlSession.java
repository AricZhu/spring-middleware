package com.aric.middleware.mybatis;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Date;

public class DefaultSqlSession implements SqlSession {
    private final Connection connection;
    private final Map<String, XNode> mapperElement;

    public DefaultSqlSession(Connection connection, Map<String, XNode> mapperElement) {
        this.connection = connection;
        this.mapperElement = mapperElement;
    }

    @Override
    public <T> T selectOne(String statement) {
        XNode xNode = mapperElement.get(statement);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> results = resultSet2Obj(resultSet, Class.forName(xNode.getResultType()));

            return results.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {
        XNode xNode = mapperElement.get(statement);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            buildSqlParameter(preparedStatement, parameter, xNode.getParameter());
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> results = resultSet2Obj(resultSet, Class.forName(xNode.getResultType()));
            return results.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public <T> List<T> selectList(String statement) {
        XNode xNode = mapperElement.get(statement);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet2Obj(resultSet, Class.forName(xNode.getResultType()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of();
    }

    @Override
    public <T> List<T> selectList(String statement, Object parameter) {
        XNode xNode = mapperElement.get(statement);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(xNode.getSql());
            buildSqlParameter(preparedStatement, parameter, xNode.getParameter());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet2Obj(resultSet, Class.forName(xNode.getResultType()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of();
    }

    @Override
    public void close() {
        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setParameter2Sql(PreparedStatement preparedStatement, Integer i, Object parameter) throws Exception {
        if (parameter instanceof Short) {
            preparedStatement.setShort(i, Short.parseShort(parameter.toString()));
        } else if (parameter instanceof Integer) {
            preparedStatement.setInt(i, Integer.parseInt(parameter.toString()));
        } else if (parameter instanceof Long) {
            preparedStatement.setLong(i, Long.parseLong(parameter.toString()));
        } else if (parameter instanceof String) {
            preparedStatement.setString(i, parameter.toString());
        } else if (parameter instanceof Date) {
            preparedStatement.setDate(i, (java.sql.Date) parameter);
        }
    }

    public void buildSqlParameter(PreparedStatement preparedStatement, Object parameter, Map<Integer, String> parameterMap) throws Exception {
        int size = parameterMap.size();
        // 简单对象，直接替换
        if (parameter instanceof Short || parameter instanceof Integer || parameter instanceof Long || parameter instanceof String || parameter instanceof Date) {
            for (int i = 1; i <= size; i++) {
                setParameter2Sql(preparedStatement, i, parameter);
            }
            return;
        }

        // 非简单对象，先获取参数内部的值
        HashMap<String, Object> parameterValueMap = new HashMap<>();
        Field[] fields = parameter.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(parameter);
            parameterValueMap.put(field.getName(), value);
            field.setAccessible(false);
        }
        // 参数替换
        for (int i = 1; i <= size; i++) {
            String key = parameterMap.get(i);
            Object value = parameterValueMap.get(key);
            setParameter2Sql(preparedStatement, i, value);
        }
    }

    public <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) throws Exception {
        ArrayList<T> list = new ArrayList<>();

        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while(resultSet.next()) {
            T target = (T) clazz.getDeclaredConstructor().newInstance();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = resultSet.getObject(i);
                String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);

                Method method;
                if (columnValue instanceof LocalDateTime) {
                    method = clazz.getMethod(setMethod, LocalDateTime.class);
                } else {
                    method = clazz.getMethod(setMethod, columnValue.getClass());
                }
                method.invoke(target, columnValue);
            }

            list.add(target);
        }

        return list;
    }
}
