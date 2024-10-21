package com.aric.middleware;

import com.aric.middleware.mybatis.Configuration;
import com.aric.middleware.mybatis.Resource;
import com.aric.middleware.mybatis.SqlSessionFactoryBuilder;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.Reader;

public class SqlSessionFactoryBuilderDemo {
    public void testSqlSessionFactoryBuilder() throws Exception {
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();

        Reader reader = Resource.getResourceAsReader("mybatis-config-datasource.xml");

        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(reader);
        Element rootElement = document.getRootElement();

        Configuration configuration = sqlSessionFactoryBuilder.parseConfiguration(rootElement);
        System.out.println(configuration);
    }

    public static void main(String[] args) throws Exception {
        SqlSessionFactoryBuilderDemo sqlSessionFactoryBuilderDemo = new SqlSessionFactoryBuilderDemo();
        sqlSessionFactoryBuilderDemo.testSqlSessionFactoryBuilder();
    }
}
