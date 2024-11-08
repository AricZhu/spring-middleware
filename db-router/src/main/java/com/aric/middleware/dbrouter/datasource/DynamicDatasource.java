package com.aric.middleware.dbrouter.datasource;

import com.aric.middleware.dbrouter.utils.ThreadLocalContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDatasource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return "db" + ThreadLocalContext.getDbKey();
    }
}
