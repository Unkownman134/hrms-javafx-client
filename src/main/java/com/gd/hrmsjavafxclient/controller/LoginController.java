package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.service.AuthService;
import com.gd.hrmsjavafxclient.service.DataFetchService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final AuthService authService = new AuthService();
    private final DataFetchService dataFetchService = new DataFetchService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showAlert("警告 ⚠️", "用户名和密码不能为空！");
            return;
        }

        // Task 返回聚合后的 CurrentUserInfo
        Task<CurrentUserInfo> loginTask = new Task<>() {
            @Override
            protected CurrentUserInfo call() throws Exception {
                // 1. 调用登录 API
                User loggedInUser = authService.login(username, password);

                if (loggedInUser == null) {
                    return null; // 登录失败
                }

                // --- 2. 查员工信息和职位信息 ---
                String employeeName = null;
                String positionName = null;
                Integer empId = loggedInUser.getEmpId();

                if (empId != null) {
                    // a. 查询员工信息 (获取员工姓名和职位ID)
                    Employee employee = dataFetchService.getEmployeeById(empId);
                    if (employee != null) {
                        employeeName = employee.getEmpName();
                        Integer posId = employee.getPosId();

                        if (posId != null) {
                            // b. 查询职位信息 (获取职位名称)
                            Position position = dataFetchService.getPositionById(posId);
                            if (position != null) {
                                positionName = position.getPosName();
                            }
                        }
                    }
                }

                // 3. 聚合所有信息并返回
                return new CurrentUserInfo(
                        loggedInUser.getUserId(),
                        loggedInUser.getUsername(),
                        loggedInUser.getRoleId(), // 🌟 核心：RoleID 用于权限判断
                        employeeName,
                        positionName
                );
            }

            @Override
            protected void succeeded() {
                CurrentUserInfo userInfo = getValue();
                if (userInfo != null) {
                    showAlert("成功 ✅", "登录成功！欢迎你，" + userInfo.getUsername()
                            + "！\n身份: " + userInfo.getRoleName() + " (" + userInfo.getRoleId() + ")");

                    // 🌟 核心：根据 RoleID 加载主界面
                    openMainWindow(userInfo);
                } else {
                    showAlert("登录失败 😭", "用户名或密码不正确！");
                }
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                // 检查是否是网络连接错误
                if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                    showAlert("连接错误 🛑", "无法连接到后端服务器，请确认Spring Boot已启动！");
                } else {
                    showAlert("操作失败 🚨", e.getMessage());
                }
                e.printStackTrace();
            }
        };

        new Thread(loginTask).start();
    }

    /**
     * 根据角色信息加载不同的主界面 (P8)
     */
    private void openMainWindow(CurrentUserInfo userInfo) {
        String fxmlFile = null;
        String title = "人事管理系统 - ";

        switch (userInfo.getRoleId()) {
            case 1:
                fxmlFile = "AdminMainView.fxml"; // 超级管理员
                title += "超级管理员";
                break;
            case 2:
                fxmlFile = "HRMainView.fxml"; // 人事管理员
                title += "人事管理员";
                break;
            case 3:
                fxmlFile = "FinanceMainView.fxml"; // 财务管理员
                title += "财务管理员";
                break;
            case 4:
                fxmlFile = "ManagerMainView.fxml"; // 部门经理
                title += "部门经理";
                break;
            default:
                fxmlFile = "EmployeeMainView.fxml"; // 普通员工
                title += "普通员工";
                break;
        }

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlFile));
            Scene scene = new Scene(loader.load());

            // 传递数据到主界面的 Controller
            MainController controller = loader.getController();
            controller.setUserInfo(userInfo); // 🌟 传递聚合数据！

            // 获取当前 Stage 并替换 Scene
            Stage currentStage = (Stage) usernameField.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setTitle(title);

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
            Alert alert = new Alert(Alert.AlertType.INFORMATION); // 使用完整路径
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}