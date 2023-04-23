package com.example.jedis;

import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用来学习jedis的用法，其实也可以用来复习
 */
public class JedisDemo {
    public static void main(String[] args) {
        // 1. connection 连接 指定 ip 和 端口
        Jedis jedis = new Jedis("127.0.0.1", 6379);

        // 2. 指定访问服务器密码
//        jedis.auth("123456");

        // 3. 指定了 Jedis 客户端，可以像访问 jdbc 一样访问 redis
        System.out.println(jedis.ping());

        // 4. string
        jedis.set("k3", "hello-jedis");
        System.out.println(jedis.get("k3"));
        System.out.println(jedis.ttl("k3"));

        // 5. list 双端链表
        jedis.lpush("list", "1", "2", "3");
        List<String> list = jedis.lrange("list", 0, -1);
        for (String s : list) {
            System.out.println(s);
        }
        System.out.println(jedis.rpop("list"));
        System.out.println(jedis.lpop("list"));

        // 6. hash
        jedis.hset("hash1", "k1", "v1");
        Map<String, String> hash = new HashMap<>();
        hash.put("k1", "v1");
        hash.put("k2", "v2");
        hash.put("k3", "v3");
        hash.put("k4", "v4");
        jedis.hmset("hash2", hash);
        System.out.println(jedis.hmget("hash2", "k1", "k2", "k3", "k4"));
        System.out.println(jedis.hget("hash1", "k1"));
        System.out.println(jedis.hexists("hash2", "k1"));
        System.out.println(jedis.hkeys("hash2"));

        // 7. set
        jedis.sadd("set1", "1", "2", "3");
        jedis.sadd("set2", "1");
        System.out.println(jedis.smembers("set1")); // 获取 set1 的成员
        System.out.println(jedis.scard("set1")); // 获取 set1 的成员数量
        System.out.println(jedis.spop("set1")); // 从 set1 中删除一个成员

        // 把 set1 中的 1 从 set1 移动到 set2
        jedis.smove("set1", "set2", "3");
        System.out.println(jedis.smembers("set1"));
        System.out.println(jedis.smembers("set2"));

        System.out.println(jedis.sinter("set1", "set2")); // 交集
        System.out.println(jedis.sunion("set1", "set2")); // 并集

        // 8. zset
        jedis.zadd("zset1", 1.0, "1");
        jedis.zadd("zset1", 2.0, "2");
        jedis.zadd("zset1", 3.0, "3");
        jedis.zadd("zset1", 4.0, "4");
        jedis.zadd("zset1", 5.0, "5");
        jedis.zadd("zset1", 6.0, "6");
        jedis.zadd("zset1", 7.0, "7");
        jedis.zadd("zset1", 8.0, "8");
        jedis.zadd("zset1", 9.0, "9");
        jedis.zadd("zset1", 10.0, "10");

        List<String> zset1 = jedis.zrange("zset1", 0, -1);
        for (String s : zset1) {
            System.out.println(s);
        }

        List<String> zset11 = jedis.zrevrange("zset1", 0, -1);
        zset11.forEach(System.out::println);

    }
}
