package com.example.canal;


import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import com.example.canal.biz.RedisCanalClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.UUID;

@SpringBootTest
public class CanalClientTest {

    public static final Integer _60SECONDS = 60;

    @Resource
    RedisCanalClient redisCanalClient;

    /**
     * 测试 canal client 监听 server 实现 mysql -> redis
     */
    @Test
    public void startClient() {
        System.out.println("--------------initCanal main()方法------------");

        // ======================================================================================
        CanalConnector connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress("127.0.0.1", 11111), // canal server 地址
                "example",
                "",
                "");

        int batchSize = 1000;
        int emptyCount = 0;
        System.out.println("---------------------canal init OK，开始监听mysql变化------");
        try {
            connector.connect();
//            connector.subscribe(".*\\..*");
            // 设置监听的表

            connector.subscribe("canal.t_user");

            connector.rollback();
            int totalEmptyCount = 10 * _60SECONDS;
            while (emptyCount < totalEmptyCount) {
                System.out.println("我是 canal, 每秒一次正在监听: " + UUID.randomUUID().toString());
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    emptyCount++;
                    System.out.println("empty count : " + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    // 计数器置0
                    emptyCount = 0;
                    // System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);
                    redisCanalClient.printEntry(message.getEntries());
                }

                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }

            System.out.println("已经监听了"+totalEmptyCount+"秒，无任何消息，请重启重试");
        } finally {
            connector.disconnect();
        }
    }
}
