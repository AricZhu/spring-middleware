package com.aric.middleware.distributetask.scheduler;

import java.lang.reflect.Method;

public class TaskRunnable implements Runnable {
    private Object bean;
    private String beanName;
    private String methodName;

    public TaskRunnable(Object bean, String beanName, String methodName) {
        this.bean = bean;
        this.beanName = beanName;
        this.methodName = methodName;
    }

    @Override
    public void run() {
        Class<?> aClass = bean.getClass();
        try {
            Method method = aClass.getDeclaredMethod(methodName);
            method.invoke(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String getTaskId() {
        return beanName + "_" + methodName;
    }
}
