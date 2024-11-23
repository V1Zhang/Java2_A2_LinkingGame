package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class GameOverController {
    private Application application;
    private GameClient gameClient;
    private AccountManager Manager;

    @FXML
    private Label resultLabel;


    public void setManager(AccountManager manager) {
        this.Manager = manager;
    }
    public void setApplication(Application application, GameClient gameClient) {
        this.application = application;
        this.gameClient = gameClient;
    }
    @FXML
    private void handleReturnToMain() throws IOException {
        try {
            gameClient.sendMessage("LOGOUT " + application.username);
        } catch (Exception e) {
            application.showError("无法连接到服务器：" + e.getMessage());
        }
        application.showMainPage(); // 跳转到主界面
    }
    @FXML
    private void handleExitGame() {
        try {
            gameClient.sendMessage("LOGOUT " + application.username);
        } catch (Exception e) {
            application.showError("无法连接到服务器：" + e.getMessage());
        }
        application.exitApplication(); // 退出游戏
    }
}
