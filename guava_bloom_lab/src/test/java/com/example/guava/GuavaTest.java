package com.example.guava;

import com.example.guava.service.GuavaBloomFilterService;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@SpringBootTest
public class GuavaTest {

    @Resource
    GuavaBloomFilterService guavaBloomFilterService;

    /**
     * guava版本布隆过滤器，helloworld 入门级演示
     */
    @Test
    public void testGuavaWithBloomFilter() {
        System.out.println("testGuavaWithBloomFilter");
        // 1. 创建 guava版布隆过滤器
        BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), 100);

        //2. 判断指定的元素是否存在
        System.out.println(bloomFilter.mightContain(1));
        System.out.println(bloomFilter.mightContain(2));

        // 2. 添加数据
        bloomFilter.put(1);
        bloomFilter.put(2);

        System.out.println(bloomFilter.mightContain(1));
        System.out.println(bloomFilter.mightContain(2));
    }

    @Test
    public void testGuavaWithBloomFilter2() {
        guavaBloomFilterService.guavaBloomFilter();
    }

}
