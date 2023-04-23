package com.example.jhs;

import com.example.jhs.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

@SpringBootTest
@Slf4j
public class JhsTest {

    private static final String JHS_KEY = "jhs";
    private static final String JHS_KEY_A = "jhs:a";
    private static final String JHS_KEY_B = "jhs:b";

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void findAB() {
        int page = 1;
        int size = 10;

        List<Product> list = null;

        long start = (page - 1) * size;
        long end = start + size - 1;

        try {
            list = redisTemplate.opsForList().range(JHS_KEY_A, start, end);

            if (CollectionUtils.isEmpty(list)) {
                log.info("---------A缓存已经过期或活动结束了，记得人工修补，B缓存继续顶着");

                // A 没有来找 B
                list = redisTemplate.opsForList().range(JHS_KEY_B, start, end);

                if (CollectionUtils.isEmpty(list)) {
                    // TODO 走 mysql 查询
                }
            }

            log.info("参加活动的商家={}", list);
        } catch (Exception e) {
            // 出异常了，一般 redis 宕机了或者redis网络抖动导致timeout
            log.error("jhs exception{}", e);
            e.printStackTrace();
            //  ..... 重试机制 再次查询 mysql
        }

        log.info(list.toString());

    }

    @Test
    public void find() {
        int page = 1;
        int size = 10;

        List<Product> list = null;

        long start = (page - 1) * size;
        long end = start + size - 1;

        try {
            list = redisTemplate.opsForList().range(JHS_KEY, start, end);

            if (CollectionUtils.isEmpty(list)) {
                // TODO 走 mysql 查询
            }

            log.info("参加活动的商家={}", list);
        } catch (Exception e) {
            // 出异常了，一般 redis 宕机了或者redis网络抖动导致timeout
            log.error("jhs exception{}", e);
            e.printStackTrace();
            //  ..... 重试机制 再次查询 mysql
        }

        log.info(list.toString());

    }

}
