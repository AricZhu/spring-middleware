package com.aric.middleware.distributetask.task;

import com.aric.middleware.distributetask.annotation.SchedulerTaskDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScheduleTask {
    private final Logger logger = LoggerFactory.getLogger(ScheduleTask.class);

    @SchedulerTaskDesc(cron = "0/1 * * * * ?", autoStart = true, desc = "每隔 1s 定时任务")
    public void taskPerOneSeconds() {
        logger.info("{}: 每隔 1s 定时任务，自动启动", System.currentTimeMillis() / 1000);
    }

    @SchedulerTaskDesc(cron = "0/3 * * * * ?", autoStart = false, desc = "每隔 3s 定时任务")
    public void taskPerThreeSeconds() {
        logger.info("{}: 每隔 3s 定时任务，手动启动", System.currentTimeMillis() / 1000);
    }
}
