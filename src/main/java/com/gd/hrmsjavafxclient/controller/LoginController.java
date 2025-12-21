package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.model.*;
import com.gd.hrmsjavafxclient.service.AuthService;
import com.gd.hrmsjavafxclient.service.DataFetchService;
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
import javafx.scene.control.Label;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private VBox loginCard;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();
    private final DataFetchService dataFetchService = new DataFetchService();

    @FXML
    public void initialize() {
        loginCard.setOpacity(0);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(800), loginCard);
        ft.setToValue(1.0);
        ft.play();
    }

    @FXML
    public void handleLoginAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("用户名或密码不能为空喵！");
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
                if (token == null) throw new RuntimeException("认证失败");
                this.authToken = token;
                user = dataFetchService.getUserByToken(authToken);
                if (user != null && user.getEmpId() != null) {
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
                CurrentUserInfo userInfo = new CurrentUserInfo(
                        user.getUserId(), user.getUsername(), user.getRoleId(),
                        user.getEmpId(), employee != null ? employee.getEmpName() : null,
                        position != null ? position.getPosName() : null,
                        employee != null ? employee.getDeptId() : null,
                        department != null ? department.getDeptName() : "N/A"
                );
                switchToMainView(userInfo, authToken);
            }

            @Override
            protected void failed() {
                loginCard.setDisable(false);
                showError("登录失败，请检查账号密码！");
            }
        };
        new Thread(loginTask).start();
    }

    private void switchToMainView(CurrentUserInfo userInfo, String authToken) {
        String fxmlFile;
        switch (userInfo.getRoleId()) {
            case 1: fxmlFile = "fxml/admin/AdminMainView.fxml"; break;
            case 2: fxmlFile = "fxml/hr/HRMainView.fxml"; break;
            case 3: fxmlFile = "fxml/finance/FinanceMainView.fxml"; break;
            case 4: fxmlFile = "fxml/manager/ManagerMainView.fxml"; break;
            default: fxmlFile = "fxml/employee/EmployeeMainView.fxml"; break;
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            MainController controller = loader.getController();
            controller.setUserInfo(userInfo, authToken);

            Stage currentStage = (Stage) usernameField.getScene().getWindow();

            currentStage.setScene(scene);
            currentStage.setMaximized(false);
            currentStage.setMaximized(true);
            currentStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}