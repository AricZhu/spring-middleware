package com.aric.middleware.mybatis;

import java.sql.Connection;
import java.util.Map;

public class Configuration {
    private Connection connection;
    private Map<String, String> dataSource;
    private Map<String, XNode> mapperElement;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Map<String, String> getDataSource() {
        return dataSource;
    }

    public void setDataSource(Map<String, String> dataSource) {
        this.dataSource = dataSource;
    }

    public Map<String, XNode> getMapperElement() {
        return mapperElement;
    }

    public void setMapperElement(Map<String, XNode> mapperElement) {
        this.mapperElement = mapperElement;
    }
}
