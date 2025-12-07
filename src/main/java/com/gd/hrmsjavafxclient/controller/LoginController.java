package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.model.User;
import com.gd.hrmsjavafxclient.service.AuthService;
import com.gd.hrmsjavafxclient.service.DataFetchService;
import javafx.animation.TranslateTransition; // 导入平移动画
import javafx.animation.Interpolator;      // 导入插值器
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox; // 导入 VBox，用于绑定登录卡片
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private VBox loginCard; // 🌟 新增：绑定 LoginView.fxml 中的 VBox

    private final AuthService authService = new AuthService();
    private final DataFetchService dataFetchService = new DataFetchService();

    // 🌟 新增：实现摇晃动画
    private void shakeLoginCard() {
        if (loginCard == null) return; // 安全检查

        // 经典的摇晃动画：左右平移 4 次
        TranslateTransition tt = new TranslateTransition(javafx.util.Duration.millis(50), loginCard);
        tt.setFromX(0f);
        tt.setByX(10f); // 左右晃动幅度
        tt.setCycleCount(4); // 晃动次数
        tt.setAutoReverse(true); // 自动反向
        tt.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1)); // 平滑过渡

        // 播放动画并结束后恢复到原位
        tt.setOnFinished(e -> loginCard.setTranslateX(0));
        tt.play();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showAlert("警告 ⚠️", "用户名和密码不能为空！");
            shakeLoginCard(); // 🌟 字段为空也摇晃一下
            return;
        }

        // Task 返回聚合后的 CurrentUserInfo
        Task<CurrentUserInfo> loginTask = new Task<>() {
            @Override
            protected CurrentUserInfo call() throws Exception {
                // 1. 认证登录
                User loggedInUser = authService.login(username, password);

                if (loggedInUser == null) {
                    throw new Exception("用户名或密码错误，请重试。");
                }

                String positionName = "未分配职位";
                String employeeName = loggedInUser.getUsername();
                Integer roleId = loggedInUser.getRoleId();

                // 🌟 核心修正：如果角色ID是1 (超级管理员)，则跳过员工档案查询，直接设置职位为 N/A。
                if (roleId != null && roleId == 1) {
                    positionName = "N/A"; // 满足用户需求：设置为 N/A
                    // 管理员名称直接使用登录用户名
                    employeeName = loggedInUser.getUsername();
                } else {
                    // 非管理员用户：执行员工档案查询逻辑
                    Integer empId = loggedInUser.getEmpId();
                    Employee employee = null;

                    if (empId != null && empId > 0) {
                        // 2. 查询员工档案
                        employee = dataFetchService.getEmployeeById(empId);
                    }

                    if (employee != null) {
                        employeeName = employee.getEmpName();
                        Integer posId = employee.getPosId();

                        // 3. 查询职位名称
                        if (posId != null && posId > 0) {
                            Position position = dataFetchService.getPositionById(posId);
                            if (position != null) {
                                positionName = position.getPosName();
                            }
                        } else {
                            positionName = "未分配职位";
                        }
                    } else if (empId != null) {
                        // 有 EmpID 但查不到档案 (如已离职或数据错误)
                        positionName = "员工档案缺失";
                    }
                }

                // 4. 构造 CurrentUserInfo
                return new CurrentUserInfo(
                        loggedInUser.getUserId(),
                        loggedInUser.getUsername(),
                        roleId,
                        employeeName,
                        positionName
                );
            }
        };

        loginTask.setOnSucceeded(e -> {
            try {
                CurrentUserInfo userInfo = loginTask.getValue();
                // 登录成功，切换主界面
                switchToMainView(userInfo);
            } catch (Exception ex) {
                showAlert("登录失败 ❌", "无法获取用户信息或跳转主界面: " + ex.getMessage());
                shakeLoginCard(); // 🌟 登录成功后跳转失败也摇晃一下
                ex.printStackTrace();
            }
        });

        loginTask.setOnFailed(e -> {
            Throwable ex = loginTask.getException();
            showAlert("登录失败 ❌", "身份验证失败: " + (ex != null ? ex.getMessage() : "未知错误"));
            shakeLoginCard(); // 🌟 身份验证失败时，调用摇晃动画
            if (ex != null) {
                ex.printStackTrace();
            }
        });

        // 在新的线程中执行登录操作
        new Thread(loginTask).start();
    }

    private void switchToMainView(CurrentUserInfo userInfo) {
        String fxmlFile;
        String title = "HRMS | ";

        switch (userInfo.getRoleId()) {
            case 1:
                fxmlFile = "fxml/admin/AdminMainView.fxml"; // 超级管理员
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
            controller.setUserInfo(userInfo); // 传递聚合数据！

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
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}