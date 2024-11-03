package com.aric.middleware.rpc.network;

import java.util.concurrent.*;

public class WriteFuture implements Future<Response> {
    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private Response response;


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        countDownLatch.countDown();
        this.response = response;
    }

    @Override
    public Response get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return this.response;
    }

    @Override
    public Response get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (countDownLatch.await(timeout, unit)) {
            return this.response;
        }

        return null;
    }
}
