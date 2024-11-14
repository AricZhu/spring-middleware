package com.aric.middleware.distributetask.utils;

import org.apache.curator.framework.CuratorFramework;

public class Constants {
    public static class Global {
        public static CuratorFramework client;
        public static final String LINE = "/";
        public static String CHARSET_NAME = "utf-8";
    }
}
