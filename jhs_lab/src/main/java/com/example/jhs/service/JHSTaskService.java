package com.example.jhs.service;


import com.example.jhs.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JHSTaskService {

    private static final String JHS_KEY = "jhs";
    private static final String JHS_KEY_A = "jhs:a";
    private static final String JHS_KEY_B = "jhs:b";

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 模拟从数据库读取20件特价商品
     * @return 商品列表
     */
    private List<Product> getProductsFromMysql() {
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Random random = new Random();
            int id = random.nextInt(1000);
            Product product = new Product((long) id, "product" + i, i, "detail");
            list.add(product);
        }
        log.info("模拟从数据库读取20件特价商品完成{}", list);
        return list;
    }

    @PostConstruct
    public void initJHSAB() {
        log.info("启动AB的定时器 天猫聚划算模拟开始===========");
        new Thread(() -> {
            while (true) {
                // 2.模拟从mysql查到数据，加到 redis 并返回给页面
                List<Product> list = getProductsFromMysql();

//                redisTemplate.delete(JHS_KEY);
//                redisTemplate.opsForList().leftPushAll(JHS_KEY, list);
//                redisTemplate.expire(JHS_KEY, 86410L, TimeUnit.SECONDS);

                // 3.先更新B缓存并且让B缓存过期时间超过A时间，如果A突然失效了还有B兜底，防止击穿
                redisTemplate.delete(JHS_KEY_B);
                redisTemplate.opsForList().leftPushAll(JHS_KEY_B, list);
                redisTemplate.expire(JHS_KEY_B, 86410L, TimeUnit.SECONDS);

                // 4.再更新A缓存
                redisTemplate.delete(JHS_KEY_A);
                redisTemplate.opsForList().leftPushAll(JHS_KEY_A, list);
                redisTemplate.expire(JHS_KEY_A, 86400L, TimeUnit.SECONDS);

                // 5.暂停一分钟，间隔1分钟执行一次，模拟聚划算一天执行的参加活动的品牌
                try {
                    Thread.sleep(1000* 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "t1").start();
    }

}
