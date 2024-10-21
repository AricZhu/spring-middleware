package com.aric.middleware.mybatis;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlSessionFactoryBuilder {
    public SqlSessionFactory build(Reader reader) throws Exception {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(reader);
        Element rootElement = document.getRootElement();
        Configuration configuration = parseConfiguration(rootElement);

        return new DefaultSqlSessionFactory(configuration);
    }

    public Configuration parseConfiguration(Element rootElement) throws Exception {
        Configuration configuration = new Configuration();

        Map<String, String> dataSource = getDataSource(rootElement.selectNodes("//dataSource"));
        configuration.setDataSource(dataSource);

        Connection connection = DriverManager.getConnection(dataSource.get("url"), dataSource.get("username"), dataSource.get("password"));
        configuration.setConnection(connection);

        configuration.setMapperElement(getMapperElement(rootElement.selectNodes("mappers")));

        return configuration;
    }

    public Map<String, String> getDataSource(List<Element> elementList) {
        Map<String, String> map = new HashMap<>();
        Element dataSourceElement = elementList.get(0);
        List content = dataSourceElement.content();

        for (Object o : content) {
            Element element = (Element) o;
            String name = element.attributeValue("name");
            String value = element.attributeValue("value");

            map.put(name, value);
        }

        return map;
    }

    public Map<String, XNode> getMapperElement(List<Element> elementList) throws Exception {
        Element mappersElement = elementList.get(0);
        List mapperList = mappersElement.content();
        Map<String, XNode> mapperElement = new HashMap<>();

        for (Object o : mapperList) {
            Element mapper = (Element) o;
            String resource = mapper.attributeValue("resource");

            Reader reader = Resource.getResourceAsReader(resource);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(reader);

            Element rootElement = document.getRootElement();
            String namespace = rootElement.attributeValue("namespace");

            List selectNodeList = rootElement.selectNodes("select");
            for (Object object : selectNodeList) {
                Element selectNode = (Element) object;

                String id = selectNode.attributeValue("id");
                String parameterType = selectNode.attributeValue("parameterType");
                String resultType = selectNode.attributeValue("resultType");
                String sql = selectNode.getText();

                Map<Integer, String> parameter = new HashMap<>();
                Pattern pattern = Pattern.compile("(#\\{(.*?)\\})");
                Matcher matcher = pattern.matcher(sql);
                for (int i = 1; matcher.find(); i++) {
                    String g1 = matcher.group(1);
                    String g2 = matcher.group(2);
                    parameter.put(i, g2);
                    sql = sql.replace(g1, "?");
                }

                XNode xNode = new XNode();
                xNode.setNamespace(namespace);
                xNode.setId(id);
                xNode.setParameterType(parameterType);
                xNode.setResultType(resultType);
                xNode.setSql(sql);
                xNode.setParameter(parameter);

                mapperElement.put(namespace + "." + id, xNode);
            }
        }

        return mapperElement;
    }
}
