package com.aric.middleware.rpc.config;

public class LocalServer {
    private static String host;
    private static int port;

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        LocalServer.host = host;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port) {
        LocalServer.port = port;
    }
}
