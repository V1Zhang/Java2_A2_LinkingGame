package org.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.example.demo.Game.SetupBoard;

public class Application extends javafx.application.Application {
    private Stage primaryStage;
    private CompletableFuture<int[]> boardSizeFuture;
    private GameClient gameClient;
    private Controller controller;
    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        connectToServer(); // 连接到服务器

    }

    // 连接到服务器
    private void connectToServer() {
        try {
            gameClient = new GameClient("localhost", 12345, this::listenForServerMessages); // 创建 GameClient 实例

            System.out.println("成功连接到服务器");
            showMainPage();    // 显示主页面
        } catch (IOException e) {
            showError("无法连接到服务器: " + e.getMessage());
        }
    }

    private void showMainPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        VBox root = loader.load();
        MainController mainController = loader.getController();
        mainController.setApplication(this);

        Scene scene = new Scene(root);
        primaryStage.setTitle("登录注册");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void showLoginPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        VBox root = loader.load();
        LoginController loginController = loader.getController();
        loginController.setApplication(this);

        Scene scene = new Scene(root);
        primaryStage.setTitle("登录");
        primaryStage.setScene(scene);
    }

    public void showBoardSizePage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("boardSize.fxml"));
        VBox root = loader.load();
        BoardSizeController boardSizeController = loader.getController();
        boardSizeController.setApplication(this);

        Scene scene = new Scene(root);
        primaryStage.setTitle("设置棋盘大小");
        primaryStage.setScene(scene);
    }

    public void setBoardSize(int size) {
        boardSizeFuture = new CompletableFuture<>(); // 用于异步等待
        gameClient.sendMessage("BOARD_SIZE " + size); // 使用 GameClient 发送消息到服务器
        System.out.println("棋盘大小已发送到服务器，等待匹配...");
    }

    // 处理服务器发送的消息
    private void listenForServerMessages(String message) {
        System.out.println(message);
        if (message.startsWith("MATCHED")) {
            System.out.println(message);
            String[] parts = message.split(" ");
            String opponent = parts[1];
            int size = Integer.parseInt(parts[2]);
        } else if (message.startsWith("BOARD")) {
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
            Game game = new Game(board, controller); // 先传 null, 稍后会在 Controller 中设置
            loadGameBoard(game);
            // 初始化并渲染游戏板
        } else if (message.startsWith("YOUR_TURN")) {
            Platform.runLater(() -> controller.setTurn(true));
        } else if (message.startsWith("NOT_YOUR_TURN")) {
            Platform.runLater(() -> controller.setTurn(false));
        } else if (message.startsWith("GAME_WON")) {
            System.out.println("You won!");
        }
    }

    // 加载游戏棋盘
    private void loadGameBoard(Game game) {
        Platform.runLater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("board.fxml"));
                VBox root = fxmlLoader.load();
                controller = fxmlLoader.getController();

                // 设置 Game 对象和 GameClient
                controller.setGameClient(gameClient);
                controller.setGame(game);
                game.setController(controller);

                // 初始化游戏板
                controller.createGameBoard();

                Scene scene = new Scene(root);
                primaryStage.setTitle("游戏棋盘");
                primaryStage.setScene(scene);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    // 用户选择棋盘大小
    private int[] getBoardSizeFromUser() {
        try {
            showBoardSizePage();
            return boardSizeFuture.get(); // 等待用户输入完成
        } catch (Exception e) {
            e.printStackTrace();
            return new int[]{4, 4};  // 默认值
        }
    }
    public void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }


}
