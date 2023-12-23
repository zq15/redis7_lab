package com.example.redis.bio.accept;

import java.io.IOException;
import java.net.Socket;

public class RedisClient1 {
    public static void main(String[] args) throws IOException {
        System.out.println("----RedisClient01 start----");

        Socket socket = new Socket("localhost", 30154);

        System.out.println("----RedisClient01 end----");
    }
}
