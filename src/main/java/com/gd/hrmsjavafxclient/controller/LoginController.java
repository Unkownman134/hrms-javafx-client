package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.App;
// 🌟 修正 1: 导入 Department 模型
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
import java.util.Optional;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private VBox loginCard;

    private final AuthService authService = new AuthService();
    private final DataFetchService dataFetchService = new DataFetchService();

    @FXML
    public void initialize() {
        // 初始时给登录框加一个轻微的抖动动画，表示等待输入
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

        // 禁用输入，显示加载中...
        loginCard.setDisable(true);

        Task<User> loginTask = new Task<>() {
            private String authToken = null;
            private User user = null;
            private Employee employee = null;
            private Position position = null;
            private Department department = null; // 🌟 新增：用于部门信息

            @Override
            protected User call() throws Exception {
                // 1. 调用登录 API
                String token = authService.login(username, password);
                if (token == null) {
                    throw new RuntimeException("登录失败，请检查用户名和密码。");
                }
                this.authToken = token;

                // 2. 验证并获取 User 基础信息
                user = dataFetchService.getUserByToken(authToken); // 👈 🌟 修正：改用 dataFetchService.getUserByToken
                if (user == null) {
                    throw new RuntimeException("认证失败，无法获取用户信息。");
                }

                // 3. 根据 empId 获取员工和职位信息
                if (user.getEmpId() != null) {
                    // 获取员工档案
                    employee = dataFetchService.getEmployeeById(user.getEmpId(), authToken);
                    if (employee != null && employee.getPosId() != null) {
                        // 获取职位信息
                        position = dataFetchService.getPositionById(employee.getPosId(), authToken);
                    }

                    // 4. 🌟 新增：获取部门信息（为 CurrentUserInfo 构造器准备参数）
                    if (employee != null && employee.getDeptId() != null) {
                        // 假设 DataFetchService 中新增了根据部门ID获取部门信息的方法
                        // 🌟 注意：你需要在项目中创建 com.gd.hrmsjavafxclient.model.Department 模型
                        // 🌟 并且在 DataFetchService 中实现 getDepartmentById(Integer deptId, String authToken)
                        department = dataFetchService.getDepartmentById(employee.getDeptId(), authToken);
                    }
                }

                return user;
            }

            @Override
            protected void succeeded() {
                // 登录成功，线程切换回 JavaFX 线程进行 UI 操作

                // 5. 聚合用户信息 (🌟 关键修正：传递 8 个参数)
                Integer deptId = employee != null ? employee.getDeptId() : null;
                String departmentName = department != null ? department.getDeptName() : "N/A"; // 如果获取失败，给个默认值

                CurrentUserInfo userInfo = new CurrentUserInfo(
                        user.getUserId(),
                        user.getUsername(),
                        user.getRoleId(),
                        user.getEmpId(),
                        employee != null ? employee.getEmpName() : null,
                        position != null ? position.getPosName() : null,
                        deptId, // 🌟 新增参数 7: 部门ID
                        departmentName // 🌟 新增参数 8: 部门名称
                );

                // 6. 切换主界面
                switchToMainView(userInfo, authToken);
            }

            @Override
            protected void failed() {
                // 登录失败，线程切换回 JavaFX 线程进行 UI 操作
                loginCard.setDisable(false);
                Throwable e = getException();
                String message = e.getMessage() != null ? e.getMessage() : "未知登录错误。";
                showAlert("登录失败 ❌", message);
                e.printStackTrace();
            }
        };

        new Thread(loginTask).start();
    }

    /**
     * 根据角色ID切换到对应的主界面
     */
    private void switchToMainView(CurrentUserInfo userInfo, String authToken) {
        String fxmlFile;
        String title = "HRMS | ";

        switch (userInfo.getRoleId()) {
            case 1:
                fxmlFile = "fxml/admin/AdminMainView.fxml"; // 超级管理员
                title += "超级管理员";
                break;
            case 2: // 👈 🌟 新增：人事管理员的跳转逻辑！
                fxmlFile = "fxml/hr/HRMainView.fxml"; // 人事管理员
                title += "人事管理员";
                break;
            case 4:
                fxmlFile = "fxml/manager/ManagerMainView.fxml"; // 部门经理
                title += "部门经理";
                break;
            default:
                fxmlFile = "fxml/employee/EmployeeMainView.fxml"; // 普通员工
                title += "普通员工";
                break;
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load());

            // 传递数据到主界面的 Controller
            MainController controller = loader.getController();
            // 🌟 关键修正：调用新的 setUserInfo(userInfo, authToken) 方法
            controller.setUserInfo(userInfo, authToken);

            // 获取当前 Stage 并替换 Scene
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle(title);
            currentStage.show();

        } catch (IOException e) {
            showAlert("界面加载错误 ❌", "无法加载主界面文件：" + fxmlFile + " (请检查 FXML 文件是否存在于 resources 文件夹中)");
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