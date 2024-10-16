package org.example.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

import static org.example.demo.Game.SetupBoard;

public class Application extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws IOException {

        int[] size = getBoardSizeFromUser();
        Controller.game = new Game(SetupBoard(size[0], size[1]));

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("board.fxml"));
        VBox root = fxmlLoader.load();
        Controller controller = fxmlLoader.getController();
        controller.createGameBoard();

        Scene scene = new Scene(root);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        // TODO: handle the game logic

    }

    // let user choose board size
    private int[] getBoardSizeFromUser() {

        // TODO: let user choose board size

        return new int[]{4, 4};
    }

    public static void main(String[] args) {
        launch();
    }
}