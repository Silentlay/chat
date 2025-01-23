package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class Program {
    public static void main(String[] args) {
//        System.out.println("Start Server ...");
        try{
            ServerSocket serverSocket = new ServerSocket(1400);

            Server server = new Server(serverSocket);
            server.runServer();
        }
        catch (UnknownHostException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
