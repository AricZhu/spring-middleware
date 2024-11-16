package com.aric.middleware.distributetask.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.aric.middleware.distributetask.annotation.SchedulerTaskDesc;
import com.aric.middleware.distributetask.domain.ExecTask;
import com.aric.middleware.distributetask.scheduler.TaskRunnable;
import com.aric.middleware.distributetask.scheduler.TaskScheduleCtrl;
import com.aric.middleware.distributetask.service.ZkCuratorServer;
import com.aric.middleware.distributetask.utils.Constants;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SchedulerAutoConfig implements ApplicationContextAware, BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {
    private final Logger logger = LoggerFactory.getLogger(SchedulerAutoConfig.class);
    private ApplicationContext applicationContext;

    @Autowired
    private TaskScheduleCtrl taskScheduleCtrl;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!Constants.execTaskListMap.containsKey(beanName)) {
            ArrayList<ExecTask> execTasks = new ArrayList<>();

            // 扫描并添加所有有任务调度注解的方法
            Class<?> aClass = bean.getClass();
            for (Method method : aClass.getDeclaredMethods()) {

                if (!method.isAnnotationPresent(SchedulerTaskDesc.class)) {
                    continue;
                }

                SchedulerTaskDesc annotation = method.getAnnotation(SchedulerTaskDesc.class);
                ExecTask execTask = new ExecTask();
                execTask.setBean(bean);
                execTask.setBeanName(beanName);
                execTask.setMethodName(method.getName());
                execTask.setCron(annotation.cron());
                execTask.setAutoStart(annotation.autoStart());
                execTask.setDesc(annotation.desc());

                execTasks.add(execTask);
            }

            if (execTasks.size() > 0) {
                Constants.execTaskListMap.put(beanName, execTasks);
            }
        }

        return bean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 初始化配置
        init_config();
        // 初始化 zookeeper 客户端
        init_server();
        // 初始化调度任务
        init_task_schedule();
        // 挂载节点
        init_task_node();
    }

    public void init_config() {
        try {
            Constants.Global.ip = InetAddress.getLocalHost().getHostAddress();

            logger.info("ip is: {}", Constants.Global.ip);
        } catch (UnknownHostException e) {
            logger.error("init config error.");
            throw new RuntimeException(e);
        }
    }

    public void init_server() {
        try {
            CuratorFramework client = ZkCuratorServer.getClient("127.0.0.1:2181");
            ZkCuratorServer.deleteNodeSimple(client, Constants.Global.path_task_exec);
            ZkCuratorServer.createNodeSimple(client, Constants.Global.path_task_exec);
            ZkCuratorServer.addTreeCacheListener(this.applicationContext, client, Constants.Global.path_task_exec);
        } catch (Exception e) {
            logger.error("init server error");
            throw new RuntimeException(e);
        }
    }

    public void init_task_schedule() {
        for (String beanName : Constants.execTaskListMap.keySet()) {
            List<ExecTask> execTasks = Constants.execTaskListMap.get(beanName);
            for (ExecTask execTask : execTasks) {
                if (!execTask.isAutoStart()) {
                    continue;
                }

                TaskRunnable taskRunnable = new TaskRunnable(execTask.getBean(), execTask.getBeanName(), execTask.getMethodName());

                taskScheduleCtrl.addTaskSchedule(taskRunnable, execTask.getCron());
            }
        }
    }

    public void init_task_node() {
        CuratorFramework client = ZkCuratorServer.getClient("127.0.0.1:2181");
        try {
            ZkCuratorServer.deletingChildrenIfNeeded(client, Constants.Global.path_task_schedule);
            for (String beanName : Constants.execTaskListMap.keySet()) {
                List<ExecTask> execTasks = Constants.execTaskListMap.get(beanName);
                for (ExecTask execTask : execTasks) {
                    String nodePath = Constants.Global.path_task_schedule + Constants.Global.LINE + execTask.getBeanName() + Constants.Global.LINE + execTask.getMethodName();
                    ZkCuratorServer.createNode(client, nodePath);
                    ZkCuratorServer.setData(client, nodePath, JSON.toJSONString(execTask));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
