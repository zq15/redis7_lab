package com.example.redis.nio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.UUID;

public class RedisServerNio {

    static ArrayList<SocketChannel> socketChannels = new ArrayList<>();
    static ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    public static void main(String[] args) throws IOException {
        System.out.println("------RedisServerNIO 启动等待中 ...");
        ServerSocketChannel serverSocket = ServerSocketChannel.open();

        serverSocket.bind(new java.net.InetSocketAddress("127.0.0.1", 30015));
        serverSocket.configureBlocking(false); // 设置成非阻塞模式

        while (true) {
            for (SocketChannel element : socketChannels) {
                int read = element.read(byteBuffer);
                if (read > 0) {
                    System.out.println("-----读取数据: " + read);
                    byteBuffer.flip();
                    byte[] bytes = new byte[read];
                    byteBuffer.get(bytes);
                    System.out.println(new String(bytes));
                    byteBuffer.clear();
                }
            }
            SocketChannel socketChannel = serverSocket.accept();
            if (socketChannel!= null) {
                System.out.println("成功连接: ");
                socketChannel.configureBlocking(false); // 设置成非阻塞模式
                socketChannels.add(socketChannel);
                System.out.println("-----socketList size: " + socketChannels.size());
            }

        }
    }
}
