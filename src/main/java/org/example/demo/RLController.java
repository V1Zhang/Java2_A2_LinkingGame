package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class RLController {
    private Application application;
    private AccountManager Manager;
    private GameClient gameClient;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    public void setApplication(Application application, GameClient gameClient) {
        this.application = application;
        this.gameClient = gameClient;
    }
    public void setManager(AccountManager manager) {
        this.Manager = manager;
    }
    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // 校验用户名和密码是否为空
        if (username.isEmpty() || password.isEmpty()) {
            application.showError("用户名或密码不能为空！");
            return;
        }
        // 密码强度检查（这里可以根据实际需求改进）
        if (password.length() < 6) {
            application.showError("密码长度至少为6位！");
            return;
        }
        // 注册新用户
        if (Manager.registerUser(username, password)) {
            gameClient.sendMessage("REGISTER " + username + " " + password);
            application.showInfo("注册成功！");
        } else {
            // 注册失败，显示错误信息
            application.showError("注册失败，请稍后重试！");
        }
    }
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        // 校验用户名和密码是否为空
        if (username.isEmpty() || password.isEmpty()) {
            application.showError("用户名或密码不能为空！");
            return;
        }
        if (username.contains(" ") || password.contains(" ")) {
            application.showError("用户名或密码不能包含空格！");
            return;
        }

        try {
            // 向服务器发送登录请求
            application.username = username;
            gameClient.sendMessage("LOGIN " + username + " " + password);
        } catch (Exception e) {
            application.showError("无法连接到服务器：" + e.getMessage());
        }
    }
}
