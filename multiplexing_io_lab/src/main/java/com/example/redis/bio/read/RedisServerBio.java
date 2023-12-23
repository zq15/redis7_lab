package com.example.redis.bio.read;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class RedisServerBio {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(30015);

        while (true) {
            System.out.println("模拟 redisServer启动 ---- 111 等待连接");
            Socket socket = server.accept(); // 阻塞1 等待客户端连接
            System.out.println("----222 连接成功  " + UUID.randomUUID());

            InputStream inputStream = socket.getInputStream();
            int length = 1;
            byte[] bytes = new byte[1024];
            System.out.println("----333 等待数据读取");
            while ((length = inputStream.read(bytes)) != -1) { // 阻塞2 等待客户端发送数据
                System.out.println("----444 成功读取 " + new String(bytes, 0, length));
                System.out.println("=====================" + UUID.randomUUID());
                System.out.println();
            }
            inputStream.close();
            socket.close();
        }
    }
}
