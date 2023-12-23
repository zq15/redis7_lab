package com.example.redis.bio.accept;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;

public class RedisServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(30154);

        while (true) {
            System.out.println("模拟 redisServer启动 ---- 111 等待连接");
            server.accept(); // 这里阻塞了
            System.out.println("----222 连接成功  " + UUID.randomUUID());
            System.out.println();
        }
    }
}
