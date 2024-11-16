package com.aric.middleware.distributetask.scheduler;

import com.aric.middleware.distributetask.utils.Constants;
import jakarta.annotation.Resource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledFuture;

@Component
public class TaskScheduleCtrl {
    @Resource(name = "my-task-scheduler")
    private TaskScheduler taskScheduler;

    public void addTaskSchedule(TaskRunnable taskRunnable, String cron) {
        if (Constants.taskScheduledMap.containsKey(taskRunnable.getTaskId())) {
            return;
        }
        ScheduledFuture<?> future = taskScheduler.schedule(taskRunnable, new CronTrigger(cron));
        TaskScheduled taskScheduled = new TaskScheduled();
        taskScheduled.setFuture(future);
        Constants.taskScheduledMap.put(taskRunnable.getTaskId(), taskScheduled);
    }

    public void removeTaskSchedule(String taskId) {
        if (!Constants.taskScheduledMap.containsKey(taskId)) {
            return;
        }

        TaskScheduled taskScheduled = Constants.taskScheduledMap.remove(taskId);
        taskScheduled.cancel();
    }
}
