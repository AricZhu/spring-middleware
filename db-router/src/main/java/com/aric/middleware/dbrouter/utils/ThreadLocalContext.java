package com.aric.middleware.dbrouter.utils;

public class ThreadLocalContext {
    public static final ThreadLocal<String> dbKey = new ThreadLocal<>();
    public static final ThreadLocal<String> tbKey = new ThreadLocal<>();

    public static void setDbKey(String key) {
        dbKey.set(key);
    }

    public static String getDbKey() {
        return dbKey.get();
    }

    public static void setTbKey(String key) {
        tbKey.set(key);
    }

    public static String getTbKey() {
        return tbKey.get();
    }

    public static void clearDbkey() {
        dbKey.remove();
    }

    public static void clearTbKey() {
        tbKey.remove();
    }
}
