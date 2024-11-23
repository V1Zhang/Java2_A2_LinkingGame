package org.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.List;

public class BoardSizeController {


    private Application application;
    private GameClient gameClient;
    @FXML
    private Label titleLabel;
    @FXML
    private TextField sizeField;
    @FXML
    private Button confirmButton;
    @FXML
    private Button viewPlayersButton;
    @FXML
    private Label waitingLabel;
    @FXML
    private ListView<String> waitingListView;
    public void setApplication(Application application, GameClient gameClient) {
        this.application = application;
        this.gameClient = gameClient;
    }

    // 点击确认按钮时调用
    @FXML
    private void handleConfirmButton() {
        try {
            int size = Integer.parseInt(sizeField.getText());
            if (size >= 4) {
                titleLabel.setVisible(false);
                sizeField.setVisible(false);
                confirmButton.setVisible(false);
                viewPlayersButton.setVisible(false);
                waitingLabel.setVisible(true);
                application.setBoardSize(size);
            }
        } catch (NumberFormatException ignored) {
            application.showError("请输入有效的数字！");
        }
    }

    // 点击查看等待队列按钮时，向服务器请求等待队列中的玩家
    @FXML
    private void handleViewPlayersButton() {
        try {
            gameClient.sendMessage("VIEW_PLAYERS");
        }catch (Exception e) {
            application.showError(e.getMessage());
        }
    }
    // 接收并展示当前等待队列中的玩家
    public void updateWaitingPlayersList(List<String> players) {
        System.out.println(players);
        // 更新 ListView 显示玩家列表
        Platform.runLater(() -> {
            waitingListView.getItems().clear();  // 清空当前列表
            waitingListView.getItems().addAll(players);  // 添加新的玩家列表
            waitingListView.setVisible(true);  // 显示 ListView
        });
    }
    // 玩家选择对手
    @FXML
    private void handlePlayerSelect() {
        // 获取选中的玩家
        String selectedPlayer = waitingListView.getSelectionModel().getSelectedItem();
        gameClient.sendMessage("SELECT_OPPONENT "+selectedPlayer);
    }



}
