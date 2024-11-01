package com.aric.middleware.rpc.network;

import com.aric.middleware.rpc.config.ProviderConfig;

public class Request {
    private String uuid;
    private ProviderConfig providerConfig;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ProviderConfig getProviderConfig() {
        return providerConfig;
    }

    public void setProviderConfig(ProviderConfig providerConfig) {
        this.providerConfig = providerConfig;
    }
}
