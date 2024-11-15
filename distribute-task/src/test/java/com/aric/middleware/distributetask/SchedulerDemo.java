package com.aric.middleware.distributetask;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;

public class SchedulerDemo {
    @Scheduled(fixedRate = 1000)
    public void fixedRateTask() {
        System.out.println("使用 @Scheduled 固定频率触发的任务调度");
    }

    public static TaskScheduler getScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);  // 设置线程池大小
        scheduler.setThreadNamePrefix("MyScheduledTask-");
        scheduler.initialize();
        return scheduler;
    }

    public static void main(String[] args) throws InterruptedException {
        TaskScheduler scheduler = SchedulerDemo.getScheduler();

        CronTask cronTask = new CronTask(() -> {
            System.out.println("run task.");
        }, "0/1 * * * * ?");

        ScheduledFuture<?> future = scheduler.schedule(cronTask.getRunnable(), cronTask.getTrigger());

        Thread.sleep(3000);

        future.cancel(true);
    }
}
