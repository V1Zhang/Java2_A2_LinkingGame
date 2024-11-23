package org.example.demo;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class Controller {

    @FXML
    private Label currentUserLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label boardSizeLabel;
    @FXML
    private Label opponentLabel;

    @FXML
    private GridPane gameBoard;

    private GameClient gameClient;
    public static Game game;
    int line;
    private int score = 0;
    int[] position = new int[3];
    // postion[0]表示是选中的一对位置的第一个/第二个
    @FXML
    public void initialize() {


    }

    public void setGameClient(GameClient gameClient) {
        this.gameClient = gameClient;
    }

    public void setGame(Game game) {
        Controller.game = game;
    }
    public void setCurrentUser(String username) {
        currentUserLabel.setText(username);
    }
    public void setOpponent(String opponent) {opponentLabel.setText(opponent);}
    public void setBoardSize(String size) {boardSizeLabel.setText(size);}
    public void setScore(int score) {scoreLabel.setText(String.valueOf(score));}

    public void createGameBoard() {

        gameBoard.getChildren().clear();

        for (int row = 0; row < game.row; row++) {
            for (int col = 0; col < game.col; col++) {
                Button button = new Button();
                button.setPrefSize(40, 40);
                ImageView imageView = addContent(game.board[row][col]);
                imageView.setFitWidth(30);
                imageView.setFitHeight(30);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
                int finalRow = row;
                int finalCol = col;
//                System.out.println("   "+isYourTurn);
                button.setOnAction( event -> handleButtonPress(finalRow, finalCol));
                gameBoard.add(button, col, row);
            }
        }
    }
    public void updateGameBoard(Game game) {
        this.game = game; // 更新控制器中的游戏对象

        // 遍历现有的按钮并根据新的游戏状态更新它们的显示
        for (Node node : gameBoard.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                Integer row = GridPane.getRowIndex(button);
                Integer col = GridPane.getColumnIndex(button);
                if (row == null) row = 0;
                if (col == null) col = 0;

                int value = game.board[row][col];
                ImageView imageView = addContent(value);
                imageView.setFitWidth(30);
                imageView.setFitHeight(30);imageView.setPreserveRatio(true);
                button.setGraphic(imageView); // 设置新的图像
            }

        }

    }
    public void updateScore(int points) {
        score += points;
        scoreLabel.setText(String.valueOf(score));
    }
    public int getScore(){
        return score;
    }
    private void handleButtonPress(int row, int col) {
        if (!gameClient.getIsYourTurn()) {
            System.out.println(gameClient+"不是你的回合！");
            return;
        }
        System.out.println("Button pressed at: " + row + ", " + col);
        if(position[0] == 0) {
            if(game.board[row][col]==0 || (row==position[1]&&col==position[2])){
                position[1] = 0;
                position[2] = 0;
            }else {
                position[1] = row;
                position[2] = col;
                position[0] = 1;
            }
        }else {
            if (game.board[row][col] == 0 || (row == position[1] && col == position[2])) {
                position[1] = 0;
                position[2] = 0;
                position[0] = 0;
            } else {
                boolean change = game.judge(position[1], position[2], row, col, game.board);
                position[0] = 0;
                if (change) {
                    // TODO: handle the grid deletion logic
                    drawLineBetweenBlocks(position[1], position[2], row, col);
                    game.deleteGrid(position[1], position[2]);
                    game.deleteGrid(row, col);
                    System.out.println("Grids at (" + position[1] + ", " + position[2] + ") and (" + row + ", " + col + ") deleted.");
                    Button button1 = (Button) gameBoard.getChildren().get(position[1] * game.col + position[2]);
                    Button button2 = (Button) gameBoard.getChildren().get(row * game.col + col);
                    button1.setGraphic(null);
                    button2.setGraphic(null);
                    long moveTime = System.currentTimeMillis() - gameClient.turnStartTime;

                    updateScore(game.increaseScore(moveTime, line));
                    gameClient.score = score;
                    gameClient.sendMessage("LINK_SUCCESS " + position[1] + " " + position[2] + " " + row + " " + col + " " + score);
                } else {
                    showTemporaryMessage("无法消除！");
                    gameClient.sendMessage("LINK_FAIL " + position[1] + " " + position[2] + " " + row + " " + col);
                }
            }
        }
    }
    private void drawLineBetweenBlocks(int row1, int col1, int row2, int col2) {
        Pane pane = (Pane) gameBoard.getParent();

        // 一折线情况
        if (game.isDirectlyConnected(row1, col1, row2, col2, game.board)) {
            drawLine(pane, col1, row1, col2, row2, 40);
            line = 1;
            return;
        }
        // 两折线情况
        if ((row1 != row2) && (col1 != col2)) {
            if (game.board[row1][col2] == 0 && game.isDirectlyConnected(row1, col1, row1, col2, game.board)
                    && game.isDirectlyConnected(row1, col2, row2, col2, game.board)) {
                drawLine(pane, col1, row1, col2, row1, 40);
                drawLine(pane, col2, row1, col2, row2, 40);
                line = 2;
                return;
            } else if (game.board[row2][col1] == 0 && game.isDirectlyConnected(row1, col1, row2, col1, game.board)
                    && game.isDirectlyConnected(row2, col1, row2, col2, game.board)) {
                drawLine(pane, col1, row1, col1, row2, 40);
                drawLine(pane, col1, row2, col2, row2, 40);
                line = 2;
                return;
            }
        }
        // 三折线情况
        if (row1 != row2) {
            for (int i = 0; i < game.board[0].length; i++) {
                if (game.board[row1][i] == 0 && game.board[row2][i] == 0
                        && game.isDirectlyConnected(row1, col1, row1, i, game.board)
                        && game.isDirectlyConnected(row1, i, row2, i, game.board)
                        && game.isDirectlyConnected(row2, col2, row2, i, game.board)) {
                    drawLine(pane, col1, row1, i, row1, 40);
                    drawLine(pane, i, row1, i, row2, 40);
                    drawLine(pane, col2, row2, i, row2, 40);
                    line = 3;
                    return;
                }
            }
        }
        if (col1 != col2) {
            for (int j = 0; j < game.board[0].length; j++) {
                if (game.board[j][col1] == 0 && game.board[j][col2] == 0
                        && game.isDirectlyConnected(row1, col1, j, col1, game.board)
                        && game.isDirectlyConnected(j, col1, j, col2, game.board)
                        && game.isDirectlyConnected(row2, col2, j, col2, game.board)) {
                    drawLine(pane, col1, row1, col1, j, 40);
                    drawLine(pane, col1, j, col2, j, 40);
                    drawLine(pane, col2, row2, col2, j, 40);
                    line = 3;
                    return;
                }
            }
        }
    }

    // 绘制一个线段
    private void drawLine(Pane pane, int col1, int row1, int col2, int row2, double buttonSize) {
        Button button1 = (Button) gameBoard.getChildren().get(row1 * game.col + col1);
        Button button2 = (Button) gameBoard.getChildren().get(row2 * game.col + col2);

        // 获取按钮的场景坐标
        double startX = button1.localToScene(button1.getBoundsInLocal()).getMinX() + button1.getWidth() / 2;
        double startY = button1.localToScene(button1.getBoundsInLocal()).getMinY() + button1.getHeight() / 2;
        double endX = button2.localToScene(button2.getBoundsInLocal()).getMinX() + button2.getWidth() / 2;
        double endY = button2.localToScene(button2.getBoundsInLocal()).getMinY() + button2.getHeight() / 2;

        // 将场景坐标转换为 Pane 坐标
        startX = pane.sceneToLocal(startX, startY).getX();
        startY = pane.sceneToLocal(startX, startY).getY();
        endX = pane.sceneToLocal(endX, endY).getX();
        endY = pane.sceneToLocal(endX, endY).getY();

//        System.out.println(startX+" "+startY+" "+endX+" "+endY);
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.BLUE);
        line.setStrokeWidth(2);

        pane.getChildren().add(line);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> pane.getChildren().remove(line)));
        timeline.setCycleCount(1);
        timeline.play();
    }

    private void showTemporaryMessage(String message) {
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-background-color: yellow; -fx-text-fill: black; -fx-padding: 5;");
        Pane pane = (Pane) gameBoard.getParent();
        pane.getChildren().add(messageLabel);

        double centerX = (pane.getWidth() - messageLabel.prefWidth(-1)) / 2;
        double centerY = (pane.getHeight() - messageLabel.prefHeight(-1)) / 2;
        messageLabel.setLayoutX(centerX);
        messageLabel.setLayoutY(centerY);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> pane.getChildren().remove(messageLabel)));
        timeline.setCycleCount(1);
        timeline.play();
    }

    @FXML
    private void handleReset() {

    }


    public ImageView addContent(int content){
        return switch (content) {
            case 0 -> new ImageView(imageCarambola);
            case 1 -> new ImageView(imageApple);
            case 2 -> new ImageView(imageMango);
            case 3 -> new ImageView(imageBlueberry);
            case 4 -> new ImageView(imageCherry);
            case 5 -> new ImageView(imageGrape);
            case 6 -> new ImageView(imageKiwi);
            case 7 -> new ImageView(imageOrange);
            case 8 -> new ImageView(imagePeach);
            case 9 -> new ImageView(imagePear);
            case 10 -> new ImageView(imagePineapple);
            case 11 -> new ImageView(imageWatermelon);
            default -> null;
        };
    }

    public static Image imageApple = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/apple.png")).toExternalForm());
    public static Image imageMango = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/mango.png")).toExternalForm());
    public static Image imageBlueberry = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/blueberry.png")).toExternalForm());
    public static Image imageCherry = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/cherry.png")).toExternalForm());
    public static Image imageGrape = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/grape.png")).toExternalForm());
    public static Image imageCarambola = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/carambola.png")).toExternalForm());
    public static Image imageKiwi = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/kiwi.png")).toExternalForm());
    public static Image imageOrange = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/orange.png")).toExternalForm());
    public static Image imagePeach = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/peach.png")).toExternalForm());
    public static Image imagePear = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/pear.png")).toExternalForm());
    public static Image imagePineapple = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/pineapple.png")).toExternalForm());
    public static Image imageWatermelon = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/watermelon.png")).toExternalForm());

}
