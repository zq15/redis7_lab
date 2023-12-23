package com.example.redis.bio.read;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class RedisClient2 {
    public static void main(String[] args) throws IOException {
        System.out.println("----RedisClient02 start----");

        Socket socket = new Socket("localhost", 30015);

        OutputStream outputStream = socket.getOutputStream();

        while (true) {
            Scanner scanner = new Scanner(System.in);
            String str = scanner.next();
            if (str.equalsIgnoreCase("quit")) {
                break;
            }
            socket.getOutputStream().write(str.getBytes());
            System.out.println("------RedisClient02 write---");
        }
        outputStream.close();
        socket.close();
    }
}
