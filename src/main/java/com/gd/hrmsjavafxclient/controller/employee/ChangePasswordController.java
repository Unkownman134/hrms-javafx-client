package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.service.employee.ChangePasswordService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class ChangePasswordController implements EmployeeSubController {

    @FXML private PasswordField newPasswordField;
    @FXML private TextField newTextField;
    @FXML private ToggleButton eyeButton1;

    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField confirmTextField;
    @FXML private ToggleButton eyeButton2;

    @FXML private Button submitButton;

    private CurrentUserInfo currentUser;
    private String authToken;
    private final ChangePasswordService passwordService = new ChangePasswordService();

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @Override
    public void initializeController() {
        syncFields(newPasswordField, newTextField);
        syncFields(confirmPasswordField, confirmTextField);
    }

    private void syncFields(PasswordField pf, TextField tf) {
        pf.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!tf.getText().equals(newVal)) tf.setText(newVal);
        });
        tf.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!pf.getText().equals(newVal)) pf.setText(newVal);
        });
    }

    @FXML
    private void toggleNewPassword() {
        toggleVisibility(newPasswordField, newTextField, eyeButton1);
    }

    @FXML
    private void toggleConfirmPassword() {
        toggleVisibility(confirmPasswordField, confirmTextField, eyeButton2);
    }

    private void toggleVisibility(PasswordField pf, TextField tf, ToggleButton btn) {
        if (btn.isSelected()) {
            tf.setVisible(true);
            tf.setManaged(true);
            pf.setVisible(false);
            pf.setManaged(false);
            btn.setText("🙈");
        } else {
            tf.setVisible(false);
            tf.setManaged(false);
            pf.setVisible(true);
            pf.setManaged(true);
            btn.setText("👁");
        }
    }

    @FXML
    private void handleSubmit() {
        String newPwd = newPasswordField.getText();
        String confirmPwd = confirmPasswordField.getText();

        if (newPwd.isBlank() || confirmPwd.isBlank()) {
            showAlert("提醒", "密码不能为空！", Alert.AlertType.WARNING);
            return;
        }

        if (!newPwd.equals(confirmPwd)) {
            showAlert("提醒", "两次输入的密码不一致，再检查一下", Alert.AlertType.WARNING);
            return;
        }

        User updateData = new User();
        updateData.setUserId(currentUser.getUserId());
        updateData.setRawPassword(newPwd);

        submitButton.setDisable(true);
        submitButton.setText("提交中...");

        new Thread(() -> {
            try {
                boolean success = passwordService.updatePassword(updateData, authToken);
                Platform.runLater(() -> {
                    if (success) {
                        showAlert("成功", "密码修改成功！", Alert.AlertType.INFORMATION);
                        handleReset();
                    }
                    submitButton.setDisable(false);
                    submitButton.setText("确认修改");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("失败", e.getMessage(), Alert.AlertType.ERROR);
                    submitButton.setDisable(false);
                    submitButton.setText("确认修改");
                });
            }
        }).start();
    }

    @FXML
    private void handleReset() {
        newPasswordField.clear();
        newTextField.clear();
        confirmPasswordField.clear();
        confirmTextField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}