package com.aric.middleware.rpc.register;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.aric.middleware.rpc.config.ProviderConfig;
import redis.clients.jedis.Jedis;

public class RedisRegistryCenter {
    private static Jedis jedis;

    public static void init(String host, int port) {
        jedis = new Jedis(host, port);
    }

    public static Long addProvider(ProviderConfig providerConfig) {
        return jedis.sadd(providerConfig.getNozzle(), JSON.toJSONString(providerConfig));
    }

    public static ProviderConfig getProvider(String key) {
        String srandmember = jedis.srandmember(key);
        if (StrUtil.isEmpty(srandmember)) {
            return null;
        }
        return JSON.parseObject(srandmember, ProviderConfig.class);
    }

    public static void main(String[] args) {

        RedisRegistryCenter.init("127.0.0.1", 6379);
        ProviderConfig providerConfig = new ProviderConfig();
        providerConfig.setHost("127.0.0.1");
        providerConfig.setNozzle("com.aric");
        providerConfig.setPort(6379);
        RedisRegistryCenter.addProvider(providerConfig);

        ProviderConfig providerConfig1 = RedisRegistryCenter.getProvider(providerConfig.getNozzle());

        System.out.println(JSON.toJSONString(providerConfig1));
    }
}
