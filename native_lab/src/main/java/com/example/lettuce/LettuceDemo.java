package com.example.lettuce;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.List;

public class LettuceDemo {
    public static void main(String[] args) {
        // 1. 使用构造器链式编程来builder我们的RedisURI
        RedisURI uri = RedisURI.builder()
                .withHost("127.0.0.1")
                .withPort(6379)
//                .withAuthentication("default", "123456")
                .build();

        // 2. 连接客户端
        RedisClient redisClient = RedisClient.create(uri);
        StatefulRedisConnection<String, String> conn = redisClient.connect();

        // 3. 创建操作的command，通过 conn 创建
        RedisCommands<String, String> commands = conn.sync();

        // string
        commands.set("k1", "v1");
        System.out.println(commands.get("k1"));
        System.out.println(commands.mget("k2", "v2"));
        List<String> keys = commands.keys("*");
        for (String key : keys) {
            System.out.println(key);
        }

        // list
        commands.lpush("list01", "1", "2", "3");
        List<String> list01 = commands.lrange("list01", 0, -1);
        for (String s : list01) {
            System.out.println(s);
        }
        System.out.println(commands.rpop("list01", 2));

        // hash
        commands.hset("hash", "k1", "v1");
        commands.hset("hash", "k2", "v2");
        commands.hset("hash", "k3", "v3");
        System.out.println(commands.hgetall("hash"));
        Boolean hexists = commands.hexists("hash", "k2");
        System.out.println(hexists);

        // set
        commands.sadd("set01", "1", "2", "3");
        System.out.println(commands.smembers("set01"));
        commands.sismember("set01", "2");
        System.out.println(commands.scard("set01"));

        // zset
        commands.zadd("zset01", 1.0, "1");
        commands.zadd("zset01", 2.0, "2");
        commands.zadd("zset01", 3.0, "3");
        System.out.println(commands.zrange("zset01", 0, -1));

        // 4. 关闭连接
        conn.close();
        redisClient.shutdown();

    }
}
