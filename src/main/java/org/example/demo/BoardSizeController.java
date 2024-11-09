package org.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

public class BoardSizeController {

    private Application application;

    @FXML
    private TextField sizeField;
    @FXML
    private Button confirmButton;
    @FXML
    private Label waitingLabel;

    public void setApplication(Application application) {
        this.application = application;
    }

    // 点击确认按钮时调用
    @FXML
    private void handleConfirmButton(ActionEvent event) {
        try {
            int size = Integer.parseInt(sizeField.getText());
            if (size >= 4) {
                sizeField.setVisible(false);
                confirmButton.setVisible(false);
                waitingLabel.setVisible(true);
                application.setBoardSize(size);
            }
        } catch (NumberFormatException ignored) {
            application.showError("请输入有效的数字！");
        }
    }


}
