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

public class Application extends javafx.application.Application {
    private Stage primaryStage;
    private GameClient gameClient;
    private Controller controller;
    private BoardSizeController boardSizeController;
    String username;
    private User currentUser;
    private volatile boolean controllerInitialized = false;
    public Controller getController() {
        return controller;  // 返回 Controller 实例
    }
    public void setCurrentUser(String username) {
        this.currentUser = AccountManager.getUserByUsername(username);
    }
    public User getCurrentUser() {
        return currentUser;
    }
    public BoardSizeController getBoardSizeController() {
        return boardSizeController;
    }
    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        connectToServer(); // 连接到服务器
        primaryStage.setOnCloseRequest(event -> {
            // 通知服务器客户端断开连接
            gameClient.sendMessage("DISCONNECT "+username);
            gameClient.closeConnection(); // 关闭客户端连接
            System.out.println("客户端关闭。");
        });
    }

    // 连接到服务器
    private void connectToServer() {
        try {
            gameClient = new GameClient(this, "localhost", 12345); // 创建 GameClient 实例

            System.out.println("成功连接到服务器");
            showMainPage();    // 显示主页面
        } catch (IOException e) {
            showError("无法连接到服务器: " + e.getMessage());
        }
    }

    void showMainPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        VBox root = loader.load();
        MainController mainController = loader.getController();
        mainController.setApplication(this);

        Scene scene = new Scene(root);
        primaryStage.setTitle("登录注册");
        primaryStage.setScene(scene);
        primaryStage.show();
        isGameBoardLoaded = false;
    }


    public void showRLPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterOrLogin.fxml"));
        VBox root = loader.load();
        RLController rlController = loader.getController();
        rlController.setApplication(this, gameClient);
        rlController.setManager(new AccountManager());

        Scene scene = new Scene(root);
        primaryStage.setTitle("注册登录");
        primaryStage.setScene(scene);

    }

    public void showBoardSizePage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("boardSize.fxml"));
        VBox root = loader.load();
        boardSizeController = loader.getController();
        boardSizeController.setApplication(this, gameClient);

        Scene scene = new Scene(root);
        primaryStage.setTitle("设置棋盘大小");
        primaryStage.setScene(scene);
    }

    public void setBoardSize(int size) {
        gameClient.sendMessage("BOARD_SIZE " + size); // 使用 GameClient 发送消息到服务器
        System.out.println("棋盘大小"+size+"已发送到服务器，等待匹配...");
    }

    // 处理服务器发送的消息

    private boolean isGameBoardLoaded = false;
    // 加载游戏棋盘
    void loadGameBoard(Game game) {
        Platform.runLater(() -> {
            try {
                if(!isGameBoardLoaded) {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("board.fxml"));
                    VBox root = fxmlLoader.load();
                    controller = fxmlLoader.getController();

                    // 设置 Game 对象和 GameClient
                    controller.setGameClient(gameClient);
                    controller.setGame(game);

                    // 初始化游戏板
                    controller.createGameBoard();

                    controllerInitialized = true;
                    Scene scene = new Scene(root);
                    primaryStage.setTitle("游戏棋盘");
                    primaryStage.setScene(scene);
                    isGameBoardLoaded = true;
                    controller.setScore(gameClient.score);
                    controller.setCurrentUser(username);
                    controller.setOpponent(gameClient.opponent);
                    controller.setBoardSize(String.valueOf(gameClient.boardSize));
                }else {
                    // 更新现有棋盘内容
                    controller.updateGameBoard(game);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void showWinPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("win.fxml"));
        VBox root = loader.load();

        GameOverController gameOverController = loader.getController();
        gameOverController.setApplication(this, gameClient);
        gameOverController.setManager(new AccountManager());

        // 创建场景并设置到主舞台
        Scene scene = new Scene(root);
        primaryStage.setTitle("游戏结束");
        primaryStage.setScene(scene);
    }
    public void showLosePage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("lose.fxml"));
        VBox root = loader.load();

        GameOverController gameOverController = loader.getController();
        gameOverController.setApplication(this, gameClient);

        // 创建场景并设置到主舞台
        Scene scene = new Scene(root);
        primaryStage.setTitle("游戏结束");
        primaryStage.setScene(scene);
    }
    public void showDisconnectPage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("disconnect.fxml"));
        VBox root = loader.load();

        GameOverController gameOverController = loader.getController();
        gameOverController.setApplication(this, gameClient);

        // 创建场景并设置到主舞台
        Scene scene = new Scene(root);
        primaryStage.setTitle("游戏结束");
        primaryStage.setScene(scene);
    }
    public void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void showInfo(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("信息");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }


    public void exitApplication() {
        try {
            // 停止所有后台线程，释放资源（如客户端连接等）
            if (gameClient != null) {
                gameClient.closeConnection();
            }
            // 退出 JavaFX 应用程序
            Platform.exit();
            // 确保程序完全终止
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
