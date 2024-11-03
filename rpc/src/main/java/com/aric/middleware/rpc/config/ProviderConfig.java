package com.aric.middleware.rpc.config;

public class ProviderConfig {
    private String nozzle; // 接口全名
    private String ref; // 接口映射，需要根据这个去 Spring 容器中获取到实际的 Bean
    private String host;
    private int port;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getNozzle() {
        return nozzle;
    }

    public void setNozzle(String nozzle) {
        this.nozzle = nozzle;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
