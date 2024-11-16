package com.aric.middleware.distributetask.scheduler;

import java.util.concurrent.ScheduledFuture;

public class TaskScheduled {
    private ScheduledFuture<?> future;

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    public void cancel() {
        this.future.cancel(true);
    }

    public boolean isCanceled() {
        return this.future.isCancelled();
    }
}
