package com.aric.middleware.distributetask.service;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.distributetask.annotation.SchedulerTaskDesc;
import com.aric.middleware.distributetask.domain.ExecTask;
import com.aric.middleware.distributetask.domain.Instruct;
import com.aric.middleware.distributetask.scheduler.TaskRunnable;
import com.aric.middleware.distributetask.scheduler.TaskScheduleCtrl;
import com.aric.middleware.distributetask.utils.Constants;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.nodes.PersistentEphemeralNode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ZkCuratorServer {
    private final static Logger logger = LoggerFactory.getLogger(ZkCuratorServer.class);

    // 创建客户端
    public static CuratorFramework getClient(String address) {
        if (null != Constants.Global.client) {
            return Constants.Global.client;
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(address, retryPolicy);
        //添加重连监听
        client.getConnectionStateListenable().addListener((curatorFramework, connectionState) -> {
            switch (connectionState) {
                //Sent for the first successful connection to the server
                case CONNECTED:
                    logger.info("middleware schedule init server connected {}", address);
                    break;
                //A suspended, lost, or read-only connection has been re-established
                case RECONNECTED:

                    break;
                default:
                    break;
            }
        });

        client.start();
        Constants.Global.client = client;
        return client;
    }

    // 根据路径递归创建节点，比如对于路径： /aric/zhu 会创建 "aric" 和 "zhu" 两个节点
    public static void createNode(CuratorFramework client, String path) throws Exception {
        List<String> pathChild = new ArrayList<>();
        pathChild.add(path);
        while (path.lastIndexOf(Constants.Global.LINE) > 0) {
            path = path.substring(0, path.lastIndexOf(Constants.Global.LINE));
            pathChild.add(path);
        }
        for (int i = pathChild.size() - 1; i >= 0; i--) {
            Stat stat = client.checkExists().forPath(pathChild.get(i));
            if (null == stat) {
                client.create().creatingParentsIfNeeded().forPath(pathChild.get(i));
            }
        }
    }

    // 创建简单节点，其实这里也是根据路径递归创建节点，效果同上面的 createNode，只不过本方法创建出来的中间节点不会有默认数据
    public static void createNodeSimple(CuratorFramework client, String path) throws Exception {
        if (null == client.checkExists().forPath(path)) {
            client.create().creatingParentsIfNeeded().forPath(path);
        }
    }
    //删除节点
    public static void deleteNodeSimple(CuratorFramework client, String path) throws Exception {
        if (null != client.checkExists().forPath(path)) {
            client.delete().deletingChildrenIfNeeded().forPath(path);
        }
    }

    //设置节点数据
    public static void setData(CuratorFramework client, String path, String data) throws Exception {
        if(null == client.checkExists().forPath(path)) return;
        client.setData().forPath(path, data.getBytes(Constants.Global.CHARSET_NAME));
    }

    //获取节点的数据
    public static byte[] getData(CuratorFramework client, String path) throws Exception {
        return client.getData().forPath(path);
    }

    //删除数据保留节点
    public static void deleteDataRetainNode(CuratorFramework client, String path) throws Exception {
        if (null != client.checkExists().forPath(path)) {
            client.setData().forPath(path, new byte[0]);
        }
    }

    //添加临时节点数据
    public static void appendPersistentData(CuratorFramework client, String path, String data) throws Exception {
        PersistentEphemeralNode node = new PersistentEphemeralNode(client, PersistentEphemeralNode.Mode.EPHEMERAL, path, data.getBytes(Constants.Global.CHARSET_NAME));
        node.start();
        node.waitForInitialCreate(3, TimeUnit.SECONDS);
    }

    public static void deletingChildrenIfNeeded(CuratorFramework client, String path) throws Exception {
        if (null == client.checkExists().forPath(path)) return;
        // 递归删除节点
        client.delete().deletingChildrenIfNeeded().forPath(path);
    }

    // 添加节点监听
    public static void addTreeCacheListener(final ApplicationContext applicationContext, final CuratorFramework client, String path) throws Exception {
        TreeCache treeCache = new TreeCache(client, path);
        treeCache.start();
        treeCache.getListenable().addListener((curatorFramework, event) -> {
            if (null == event.getData()) return;
            byte[] eventData = event.getData().getData();
            if (null == eventData || eventData.length < 1) return;
            String json = new String(eventData, Constants.Global.CHARSET_NAME);
            if ("".equals(json) || json.indexOf("{") != 0 || json.lastIndexOf("}") + 1 != json.length()) return;
            Instruct instruct = JSON.parseObject(json, Instruct.class);
            logger.info("receive instruct: {}", json);

            if (!Constants.Global.ip.equals(instruct.getIp())) {
                return;
            }

            String beanName = instruct.getBeanName();
            String methodName = instruct.getMethodName();
            if (!Constants.execTaskListMap.containsKey(beanName)) {
                logger.error("无效的 beanName: {}", json);
                return;
            }
            List<ExecTask> execTasks = Constants.execTaskListMap.get(beanName);
            ExecTask execTask = null;
            for (ExecTask task : execTasks) {
                if (task.getMethodName().equals(instruct.getMethodName())) {
                    execTask = task;
                    break;
                }
            }

            if (null == execTask) {
                logger.error("无效的 methodName: {}", json);
                return;
            }

            TaskScheduleCtrl taskScheduleCtrl = applicationContext.getBean("taskScheduleCtrl", TaskScheduleCtrl.class);

            // 开启任务
            if (instruct.isStatus()) {
                logger.info("启动定时任务: {}", execTask.getBeanName() + "." + execTask.getMethodName());
                taskScheduleCtrl.addTaskSchedule(new TaskRunnable(execTask.getBean(), beanName, methodName), execTask.getCron());
            } else { // 暂停任务
                logger.info("关闭定时任务: {}", execTask.getBeanName() + "." + execTask.getMethodName());
                taskScheduleCtrl.removeTaskSchedule(beanName + "_" + methodName);
            }
        });
    }


    public static void main(String[] args) throws Exception {
        CuratorFramework client = ZkCuratorServer.getClient("127.0.0.1:2181");

        ZkCuratorServer.addTreeCacheListener(null, client, "/aric/zhu");

        ZkCuratorServer.setData(client, "/aric/zhu", "hello world!");
//        ZkCuratorServer.createNodeSimple(client, "/hello/world");
//
//        ZkCuratorServer.createNode(client, "/aric/zhu");
//
//        ZkCuratorServer.setData(client, "/aric/zhu", "hello world!");
//
//        byte[] data = ZkCuratorServer.getData(client, "/aric/zhu");
//        System.out.println("get data: " + new String(data));
//
//        ZkCuratorServer.deleteDataRetainNode(client, "/aric/zhu");
//
//        data = ZkCuratorServer.getData(client, "/aric/zhu");
//        System.out.println("after delete, get data: " + new String(data));

//        ZkCuratorServer.deleteNodeSimple(client, "/aric/zhu");
//
//        for (int i = 0; i < 10; i++) {
//            Thread.sleep(1000);
//        }
//        System.out.println("end.");
    }
}
