package com.aric.middleware.dbrouter.config;

import com.aric.middleware.dbrouter.datasource.DynamicDatasource;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class AutoConfigure implements EnvironmentAware {
    private Map<String, ConnectionConfig> dataSourceMap = new HashMap<>();
    private int dbCount;
    private int tbCount;

    @Bean
    public DBRouterConfig dbRouterConfig() {
        return new DBRouterConfig(this.dbCount, this.tbCount);
    }

    @Bean
    public DataSource dataSource() {
        Map<Object, Object> targetDatasource = new HashMap<>();
        for (String dbKey : dataSourceMap.keySet()) {
            ConnectionConfig connectionConfig = dataSourceMap.get(dbKey);
            targetDatasource.put(dbKey, new DriverManagerDataSource(connectionConfig.getUrl(), connectionConfig.getUsername(), connectionConfig.getPassword()));
        }

        DynamicDatasource dynamicDatasource = new DynamicDatasource();
        dynamicDatasource.setTargetDataSources(targetDatasource);

        return dynamicDatasource;
    }

    @Override
    public void setEnvironment(Environment environment) {
        // 库表数量的配置读取
        String prefix = "dbrouter.";
        dbCount = environment.getProperty(prefix + "dbCount", Integer.class);
        tbCount = environment.getProperty(prefix + "tbCount", Integer.class);

        // 设置数据源
        String list = environment.getProperty(prefix + "list");
        String[] dblist = list.split(",");
        for (String db : dblist) {
            String url = environment.getProperty(prefix + db + ".url");
            String username = environment.getProperty(prefix + db + ".username");
            String password = environment.getProperty(prefix + db + ".password");
            ConnectionConfig connectionConfig = new ConnectionConfig(url, username, password);
            dataSourceMap.put(db, connectionConfig);
        }
    }
}
