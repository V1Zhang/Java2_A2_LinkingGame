package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.IOException;

public class MainController {

    private Application application;

    @FXML
    private Button loginButton;

    public void setApplication(Application application) {
        this.application = application;
    }
    @FXML
    private void handleRegisterButton() {
        // 注册逻辑或跳转到注册页面
        System.out.println("注册按钮被点击");
    }
    @FXML
    private void handleLoginButton() throws IOException {
        application.showLoginPage();
    }
}
