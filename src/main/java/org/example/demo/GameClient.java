package org.example.demo;

import javafx.application.Platform;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class GameClient {
    private Socket socket;
    private PrintWriter output;
    private Scanner input;
    private Consumer<String> messageHandler;
    private Application application;
    private boolean isYourTurn;
    private boolean isClientClosing = false;
    long turnStartTime;
    String opponent = "";
    int boardSize = 0;
    int score = 0;
    public GameClient(Application application, String serverAddress, int serverPort) throws IOException {
        this.application = application;
        this.socket = new Socket(serverAddress, serverPort);
        this.output = new PrintWriter(socket.getOutputStream(), true);
        this.input = new Scanner(socket.getInputStream());
        this.messageHandler = this::listenForServerMessages;

        // 启动监听服务器消息的线程
        new Thread(this::listenForServerMessages).start();
    }
    public void setMessageHandler(Consumer<String> messageHandler) {
        this.messageHandler = messageHandler;
    }


    private void listenForServerMessages() {
        CompletableFuture.runAsync(() -> {
            try {
                while (socket != null && !socket.isClosed() && input.hasNextLine()) {
                    String message = input.nextLine();
                    messageHandler.accept(message);
                }
            } catch (Exception e) {
                System.err.println("服务器连接已断开。");
                e.printStackTrace();
            } finally {
                if(!isClientClosing) {
                    handleServerDisconnection(); // 确保断开逻辑总会被执行
                }
            }
        });
    }
    private void handleServerDisconnection() {
        Platform.runLater(() -> {
            System.err.println("服务器异常关闭，客户端即将退出。");
            application.showError("服务器已断开，程序即将关闭！");
            closeConnection(); // 关闭客户端连接
            Platform.exit(); // 停止 JavaFX 应用程序
            System.exit(0); // 确保进程终止
        });
    }
    public void sendMessage(String message) {
        output.println(message);
    }

    public void closeConnection() {
        isClientClosing = true;
        try {
            if (output != null) {
                output.flush(); // 确保输出流被刷新
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        } catch (IOException e) {
            System.out.println("关闭连接时出错。");
        }
    }
    private void listenForServerMessages(String message) {
        System.out.println(message);
        if (message.startsWith("SUCCESS_LOGIN ")){
            String[] parts = message.split(" ");
            String username = parts[parts.length - 1];
            application.setCurrentUser(username);
            System.out.println(this.score);
            sendMessage("RECONNECT " + username);
        } else if (message.startsWith("OLD_GAME ")) {
            String[] parts = message.split(" ");
            this.score = Integer.parseInt(parts[parts.length - 1]);
//            application.getController().setScore(this.score);
//            Platform.runLater(() -> {
//                try {

//                    application.showGamePage();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
        } else if (message.equals("NEW_GAME")) {
            Platform.runLater(() -> {
                try {
                    application.showBoardSizePage();
                } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        } else if (message.startsWith("FAIL_LOGIN ")) {
            Platform.runLater(() -> application.showError("登录失败，用户名或密码错误！"));
        } else if (message.startsWith("WAITING_QUEUE ")) {
            String[] playersData = message.substring("WAITING_QUEUE ".length()).split(";");
            System.out.println(Arrays.toString(playersData));
            List<String> playerList = new ArrayList<>();
            for (String playerInfo : playersData) {
                System.out.println(playerInfo);
                if (!playerInfo.trim().isEmpty()) {
                    playerList.add(playerInfo.trim());
                }
            }
            application.getBoardSizeController().updateWaitingPlayersList(playerList);
        } else if (message.startsWith("MATCHED ")) {
            String[] parts = message.split(" ");
            opponent = parts[1];
            boardSize = Integer.parseInt(parts[2]);
        } else if (message.startsWith("BOARD ")) {
            // 解析服务器发来的棋盘数据
            String boardData = message.substring(6).trim();
            boardData = boardData.replaceAll("\\[|\\]", "").trim(); // 去掉所有的方括号
            String[] values = boardData.split(",\\s*"); // 根据逗号并忽略空格分割数据

            int size = (int) Math.sqrt(values.length);
            int[][] board = new int[size][size];
            for (int i = 0; i < values.length; i++) {
                int row = i / size;
                int col = i % size;
                board[row][col] = Integer.parseInt(values[i].trim());
            }
            Game game = new Game(board);
            // 初始化并渲染游戏板
            application.loadGameBoard(game);


        } else if (message.equals("YOUR_TURN")) {
            isYourTurn = true;
            System.out.println("It's your turn!");
            turnStartTime = System.currentTimeMillis();
        } else if (message.equals("NOT_YOUR_TURN")) {
            isYourTurn = false;
            System.out.println("Waiting for opponent's turn...");
        } else if (message.startsWith("GAME_WIN ")) {
            System.out.println("You won!");
            String[] parts = message.split(": ");
            String opponent = parts[1];
            int newScore = application.getController().getScore();
            User currentUser = application.getCurrentUser();
            sendMessage("RECORD "+opponent + " win " + newScore);
            System.out.println("Saving new score for user: " + currentUser.getUsername() + " --" +newScore);
            Platform.runLater(() -> {
                try {
                    application.showWinPage();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (message.startsWith("GAME_LOSE ")) {
            System.out.println("You lose, try next time!");
            String[] parts = message.split(": ");
            String opponent = parts[1];
            int newScore = application.getController().getScore();
            sendMessage("RECORD "+opponent + " lose " + newScore);
            Platform.runLater(() -> {
                try {
                    application.showLosePage();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (message.startsWith("OPPONENT_DISCONNECTED ")) {
            String[] parts = message.split(": ");
            User currentUser = application.getCurrentUser();
            currentUser.addGameHistory("对手: " + parts[parts.length-1] + ", 结果: " + "opponent disconnected");
            Platform.runLater(() -> {
                application.showInfo("对手已退出！");
                try {
                    application.showDisconnectPage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    public boolean getIsYourTurn(){
        return isYourTurn;
    }
}
