package org.example.demo;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;

public class MainController {
    private final AccountController accountController = new AccountController();
    private Application application;
    @FXML
    private Button Button;
    @FXML
    private Button viewHistoryButton;
    public void setApplication(Application application) {
        this.application = application;
    }

    @FXML
    private void handleButton() throws IOException {
        application.showRLPage();
    }
    @FXML
    private void handleViewHistoryAndOnline() {
        // 获取所有用户的积分排名
        List<String> rankings = accountController.getUserRankings();
        // 构造排名信息
        StringBuilder info = new StringBuilder();
        info.append("用户积分排名:\n");
        for (String ranking : rankings) {
            info.append(ranking).append("\n");
        }

        // 构建用户的历史记录部分
        Accordion accordion = new Accordion();
        for (String ranking : rankings) {
            String[] parts = ranking.split(": ");
            String username = parts[0];  // 获取用户名

            User user = AccountManager.getUserByUsername(username);
            TitledPane titledPane = new TitledPane();
            titledPane.setText(username + " - 历史最高得分: " + user.getHighestScoreScore() + "分    " + (user.isLoggedIn()?"在线":"离线"));

            // 显示最近的游戏历史
            VBox historyBox = new VBox(5); // 设置间距为5
            List<String> gameHistory = user.getGameHistory();
            for (String history : gameHistory) {
                historyBox.getChildren().add(new Text(history));
            }

            titledPane.setContent(historyBox);
            accordion.getPanes().add(titledPane);
        }

        // 弹出一个弹窗显示排名和历史记录
        showAlert("积分排名与游戏历史", info.toString(), accordion);
    }

    // 用于显示弹窗（包括排名和游戏历史的 Accordion）
    private void showAlert(String title, String content, Accordion accordion) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // 将 Accordion 添加到弹窗内容
        alert.getDialogPane().setContent(accordion);

        // 设置弹窗的大小
        alert.getDialogPane().setPrefSize(600, 400);  // 设置弹窗的宽度和高度，确保足够容纳内容

        alert.showAndWait();
    }

}
