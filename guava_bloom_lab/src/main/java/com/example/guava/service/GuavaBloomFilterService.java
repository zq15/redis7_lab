package com.example.guava.service;


import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GuavaBloomFilterService {

    // 1.定义一个常量
    public static final int _1W = 10000;
    // 2.定义我们guava布隆过滤器，初始容量
    public static final int SIZE = 100 * _1W;
    // 3.误判率，它越小误判的个数也越少（思考：是否可以无限小？ 没有误判岂不是更好）
    public static double fpp = 0.0000000000003;  // 这个数越小所用的hash函数越多，bitmap占用的位越多  默认的就是0.03，5个hash函数   0.01，7个函数
    // 4.创建guava布隆过滤器
    private static BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), SIZE, fpp);

    public void guavaBloomFilter() {
        // 1. 往 bloomFilter 中添加数据
        for (int i = 0; i < SIZE; i++) {
            bloomFilter.put(i);
        }
        // 2. 故意取10w个不在范围内的数据进行测试，来进行误判率演示
        List<Integer> list = new ArrayList<>(10 * _1W);

        // 3. 验证
        for (int i = SIZE; i < SIZE + (10 * _1W); i++) {
            if (bloomFilter.mightContain(i)) {
//                log.info("被误判了:{}", i);
                list.add(i);
            }
        }
        log.info("误判总数量:{}", list.size());
        log.info("误判率:{}", list.size() / (10 * _1W));
    }
}
