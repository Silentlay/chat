package org.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientManager implements Runnable {

    private final Socket socket;

    private BufferedReader bufferedReader;

    private BufferedWriter bufferedWriter;

    private String name;

    public final static ArrayList<ClientManager> clients = new ArrayList<>();

    public ClientManager(Socket socket){
        this.socket = socket;

        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            name = bufferedReader.readLine();
            clients.add(this);
            System.out.println(name + " подключился к чату.");
            broadcastMessage("Server: " + name + " подключился к чату.");
        }
        catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();

                if (messageFromClient == null) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }

                if (messageFromClient.startsWith("/@")) {
                    // Личное сообщение
                    String[] parts = messageFromClient.split(" ", 2);
                    if (parts.length < 2 || !parts[0].startsWith("/@")) {
                        bufferedWriter.write("Server: Неправильный формат команды. Используйте /@<username> <message>.");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    } else {
                        String recipientName = parts[0].substring(2); // Получить имя после /@
                        String privateMessage = parts[1];
                        sendPrivateMessage(recipientName, privateMessage);
                    }
                } else {
                    // Обычное сообщение
                    broadcastMessage(messageFromClient);
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void broadcastMessage(String message) {
        for (ClientManager client : clients) {
            try {
                if (!client.name.equals(name)) {
                    client.bufferedWriter.write(message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    private void sendPrivateMessage(String recipientName, String message) {
        boolean foundRecipient = false;
        for (ClientManager client : clients) {
            if (client.name.equals(recipientName)) {
                try {
                    client.bufferedWriter.write("[Личное сообщение от " + name + "]: " + message);
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                    foundRecipient = true;
                    break;
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }
        if (!foundRecipient) {
            try {
                bufferedWriter.write("Server: Пользователь " + recipientName + " не найден.");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){

        // Удаление клиента из коллекции
        removeClient();
        try {
            // Завершаем работу на чтение данных
            if (bufferedReader != null){
                bufferedReader.close();
            }
            // Завершаем работу буфера для записи данных
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            // Закрытие соединения с клиентским сокетом
            if (socket != null) {
                socket.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void removeClient(){
        clients.remove(this);
        System.out.println(name + " покинул чат.");
        broadcastMessage("Server: " + name + " покинул чат.");
    }
}
