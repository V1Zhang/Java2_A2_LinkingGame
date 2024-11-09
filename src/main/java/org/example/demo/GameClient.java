package org.example.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Consumer;

public class GameClient {
    private Socket socket;
    private PrintWriter output;
    private Scanner input;
    private Consumer<String> messageHandler;

    public GameClient(String serverAddress, int serverPort, Consumer<String> messageHandler) throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new Scanner(socket.getInputStream());
        this.messageHandler = messageHandler;

        // 启动监听服务器消息的线程
        new Thread(this::listenForServerMessages).start();
    }

    private void listenForServerMessages() {
        while (input.hasNextLine()) {
            String message = input.nextLine();
            messageHandler.accept(message);
        }
    }

    public void sendMessage(String message) {
        output.println(message);
    }

    public void closeConnection() {
        try {
            input.close();
            output.close();
            socket.close();
            System.out.println("连接已关闭。");
        } catch (IOException e) {
            System.out.println("关闭连接时出错。");
        }
    }

}
