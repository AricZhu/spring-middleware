package com.aric.middleware;

import com.alibaba.fastjson.JSON;
import com.aric.middleware.rpc.utils.SerializeUtil;

import java.util.Arrays;

public class SerializeUtilDemo {
    public static void main(String[] args) throws Exception {
        CustomBean customBean = new CustomBean();
        customBean.setName("小明");
        customBean.setAge(22);

        // 序列化
        byte[] data = SerializeUtil.seriazlize(customBean);
        System.out.println(Arrays.toString(data));

        // 反序列化
        CustomBean customBean1 = SerializeUtil.deserialize(data, CustomBean.class);
        System.out.println(JSON.toJSONString(customBean1));
    }
}
