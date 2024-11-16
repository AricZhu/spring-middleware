package com.aric.middleware.distributetask.utils;

import com.aric.middleware.distributetask.domain.ExecTask;
import com.aric.middleware.distributetask.scheduler.TaskScheduled;
import org.apache.curator.framework.CuratorFramework;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constants {
    public final static Map<String, List<ExecTask>> execTaskListMap = new ConcurrentHashMap();
    public final static Map<String, TaskScheduled> taskScheduledMap = new ConcurrentHashMap();

    public static class Global {
        public static CuratorFramework client;
        public static final String LINE = "/";
        public static String CHARSET_NAME = "utf-8";
        public static String path_task_exec = "/taskschedule/exec";
        public static String path_task_schedule = "/taskschedule/task";
        public static String ip;
    }
}
