package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    private Application application;

    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;

    public void setApplication(Application application) {
        this.application = application;
    }

    @FXML
    private void handleLogin() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // 简单验证（实际项目中需要更完善的验证）
        if (username.isEmpty() || password.isEmpty()) {
            application.showError("用户名或密码不能为空！");
        } else {
            // 登录成功，进入棋盘大小设置页面
            application.showBoardSizePage();
        }
    }
}
