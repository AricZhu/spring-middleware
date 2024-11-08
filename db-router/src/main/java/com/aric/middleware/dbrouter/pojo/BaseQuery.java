package com.aric.middleware.dbrouter.pojo;

import com.aric.middleware.dbrouter.utils.ThreadLocalContext;

public class BaseQuery {
    private String tbIdx;

    public String getTbIdx() {
        return ThreadLocalContext.getTbKey();
    }
}
