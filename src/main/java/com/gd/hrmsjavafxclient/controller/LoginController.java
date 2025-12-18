package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.model.*;
import com.gd.hrmsjavafxclient.service.AuthService;
import com.gd.hrmsjavafxclient.service.DataFetchService;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private VBox loginCard;

    private final AuthService authService = new AuthService();
    private final DataFetchService dataFetchService = new DataFetchService();

    @FXML
    public void initialize() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(500), loginCard);
        transition.setFromY(-5.0);
        transition.setToY(0.0);
        transition.setCycleCount(4);
        transition.setAutoReverse(true);
        transition.setInterpolator(Interpolator.EASE_BOTH);
        transition.play();
    }

    @FXML
    public void handleLoginAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("登录失败", "用户名或密码不能为空哦！");
            return;
        }

        loginCard.setDisable(true);

        Task<User> loginTask = new Task<>() {
            private String authToken = null;
            private User user = null;
            private Employee employee = null;
            private Position position = null;
            private Department department = null;

            @Override
            protected User call() throws Exception {
                String token = authService.login(username, password);
                if (token == null) {
                    throw new RuntimeException("登录失败，请检查用户名和密码。");
                }
                this.authToken = token;

                user = dataFetchService.getUserByToken(authToken);
                if (user == null) {
                    throw new RuntimeException("认证失败，无法获取用户信息。");
                }

                if (user.getEmpId() != null) {
                    employee = dataFetchService.getEmployeeById(user.getEmpId(), authToken);
                    if (employee != null && employee.getPosId() != null) {
                        position = dataFetchService.getPositionById(employee.getPosId(), authToken);
                    }
                    if (employee != null && employee.getDeptId() != null) {
                        department = dataFetchService.getDepartmentById(employee.getDeptId(), authToken);
                    }
                }

                return user;
            }

            @Override
            protected void succeeded() {
                Integer deptId = employee != null ? employee.getDeptId() : null;
                String departmentName = department != null ? department.getDeptName() : "N/A";

                CurrentUserInfo userInfo = new CurrentUserInfo(
                        user.getUserId(),
                        user.getUsername(),
                        user.getRoleId(),
                        user.getEmpId(),
                        employee != null ? employee.getEmpName() : null,
                        position != null ? position.getPosName() : null,
                        deptId,
                        departmentName
                );

                switchToMainView(userInfo, authToken);
            }

            @Override
            protected void failed() {
                loginCard.setDisable(false);
                Throwable e = getException();
                String message = e.getMessage() != null ? e.getMessage() : "未知登录错误。";
                showAlert("登录失败 ❌", message);
                e.printStackTrace();
            }
        };

        new Thread(loginTask).start();
    }

    private void switchToMainView(CurrentUserInfo userInfo, String authToken) {
        String fxmlFile;
        String title = "HRMS | ";

        switch (userInfo.getRoleId()) {
            case 1:
                fxmlFile = "fxml/admin/AdminMainView.fxml";
                title += "超级管理员";
                break;
            case 2:
                fxmlFile = "fxml/hr/HRMainView.fxml";
                title += "人事管理员";
                break;
            case 3: // 🌟 这里的逻辑是新增的哦！指向财务模块
                fxmlFile = "fxml/finance/FinanceMainView.fxml";
                title += "财务管理员";
                break;
            case 4:
                fxmlFile = "fxml/manager/ManagerMainView.fxml";
                title += "部门经理";
                break;
            default:
                fxmlFile = "fxml/employee/EmployeeMainView.fxml";
                title += "普通员工";
                break;
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load());

            MainController controller = loader.getController();
            controller.setUserInfo(userInfo, authToken);

            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            currentStage.show();

        } catch (IOException e) {
            showAlert("界面加载错误 ❌", "无法加载主界面文件：" + fxmlFile);
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("系统错误 🐞", "主界面启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}