package com.example.redis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class OrderService {

    @Resource
    private RedisTemplate redisTemplate;

//    @Resource
//    private StringRedisTemplate stringRedisTemplate;

    private static final String ORDER_KEY = "ord:";

    public void saveOrder() {
        int keyId = ThreadLocalRandom.current().nextInt(1000)+1;
        String serialNo = UUID.randomUUID().toString();

        String key = ORDER_KEY + keyId;
        String value = "京东订单" + serialNo;

        log.info("=====key:{}", key);
        log.info("=====value:{}", value);

        redisTemplate.opsForValue().set(key, value);
    }

    public String getOrderId(Integer keyId) {
//        return (String) redisTemplate.opsForValue().get(ORDER_KEY + keyId);
        return (String) redisTemplate.opsForValue().get(ORDER_KEY + keyId);
    }

}
