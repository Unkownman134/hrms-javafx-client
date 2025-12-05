package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.concurrent.Task; // 🌟 必须用这个来做网络请求！

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final AuthService authService = new AuthService();

    @FXML // 对应 FXML 文件中的 onAction="#handleLogin"
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("错误 ❌", "用户名和密码不能为空哦！");
            return;
        }

        // 创建一个后台任务 (Task) 来处理网络请求
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                // 在后台线程中调用网络服务
                return authService.login(username, password);
            }

            @Override
            protected void succeeded() {
                // 成功后，回到 UI 线程处理结果
                User loggedInUser = getValue();
                if (loggedInUser != null) {
                    showAlert("成功 ✅", "登录成功！欢迎你，" + loggedInUser.getUsername()
                            + "！\n你的角色ID是: " + loggedInUser.getRoleId());
                    // TODO: P8 - 根据 RoleID 加载对应的用户主界面
                } else {
                    showAlert("登录失败 😭", "用户名或密码不正确！");
                }
            }

            @Override
            protected void failed() {
                // 失败后，回到 UI 线程处理异常（如网络不通）
                Throwable e = getException();
                showAlert("连接错误 🛑", "无法连接到后端服务器，请确认Spring Boot已启动！\n错误: " + e.getMessage());
                e.printStackTrace();
            }
        };

        new Thread(loginTask).start(); // 启动后台线程
    }

    // 辅助方法：显示对话框
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}