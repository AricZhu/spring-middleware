package com.aric.middleware.rpc.network;

import java.util.HashMap;
import java.util.Map;

public class WriteFutureMap {
    private static final Map<String, WriteFuture> syncMap = new HashMap<>();

    public static WriteFuture getWriteFuture(String uid) {
        if (syncMap.containsKey(uid)) {
            return syncMap.get(uid);
        }
        WriteFuture writeFuture = new WriteFuture();
        syncMap.put(uid, writeFuture);
        return writeFuture;
    }

    public static void removeWriteFuture(String uid) {
        syncMap.remove(uid);
    }
}
