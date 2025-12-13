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
import javafx.event.ActionEvent; // 🌟 导入 ActionEvent
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration; // 导入 Duration

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private VBox loginCard; // 绑定 LoginView.fxml 中的 VBox

    private final AuthService authService = new AuthService();
    private final DataFetchService dataFetchService = new DataFetchService();

    // 🌟 修正：实现摇晃动画
    private void shakeLoginCard() {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), loginCard);
        tt.setFromX(0f);
        tt.setByX(10f);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.setInterpolator(Interpolator.LINEAR);
        tt.playFromStart();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            shakeLoginCard();
            showAlert("温馨提示", "用户名和密码不能为空哦！");
            return;
        }

        // 运行在后台线程，避免阻塞 UI
        Task<Void> loginTask = new Task<>() {
            private String authToken = null; // 存储认证 Token
            private User user = null; // 存储用户基础信息 (来自 /auth/user-details)
            private Employee employee = null;
            private Position position = null;

            @Override
            protected Void call() throws Exception {
                // 1. 登录认证，获取 Token
                // 🌟 修正 1：authService.login() 现在返回 String authToken
                authToken = authService.login(username, password);

                if (authToken == null || authToken.isEmpty()) {
                    throw new Exception("用户名或密码错误。");
                }

                // 🌟 修正 2：authToken 已获取，不再需要 user.getAuthToken()

                // 2. 使用 Token 获取用户基础信息 (UserID, RoleID, EmpID)
                // getUserByToken 默认会带上 Token 请求后端的用户详情接口
                user = dataFetchService.getUserByToken(authToken);

                if (user == null) {
                    throw new Exception("无法获取用户基本信息。");
                }

                // 3. 获取员工和职位信息（现在所有查询都需要 Token）
                Integer empId = user.getEmpId();

                if (empId != null) {
                    // 🌟 修正 3.1：getEmployeeById 必须传入 authToken
                    employee = dataFetchService.getEmployeeById(empId, authToken);

                    if (employee != null && employee.getPosId() != null) {
                        // 🌟 修正 3.2：getPositionById 必须传入 authToken
                        position = dataFetchService.getPositionById(employee.getPosId(), authToken);
                    }
                }

                return null;
            }

            @Override
            protected void succeeded() {
                try {
                    // 4. 聚合信息
                    CurrentUserInfo userInfo = new CurrentUserInfo(
                            user.getUserId(),
                            user.getUsername(),
                            user.getRoleId(),
                            user.getEmpId(), // 使用修正后的 EmpId
                            employee != null ? employee.getEmpName() : null,
                            position != null ? position.getPosName() : null
                    );

                    // 5. 启动主界面，传入聚合信息和 Token
                    launchMainView(userInfo, authToken);

                } catch (Exception e) {
                    showAlert("系统错误 🐞", "聚合用户信息失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            protected void failed() {
                // ... (错误处理逻辑) ...
                Throwable e = getException();
                // 动画必须在 JavaFX UI 线程运行
                javafx.application.Platform.runLater(() -> shakeLoginCard());
                showAlert("登录失败 ❌", e.getMessage());
                e.printStackTrace();
            }
        };

        new Thread(loginTask).start();
    }

    /**
     * 根据用户角色启动对应的主界面
     * 🌟 修正：现在需要传入 authToken
     */
    private void launchMainView(CurrentUserInfo userInfo, String authToken) {
        String fxmlFile;
        String title = "HRMS 人力资源管理系统 - ";

        switch (userInfo.getRoleId()) {
            case 1:
                fxmlFile = "fxml/admin/AdminMainView.fxml"; // 超级管理员
                title += "超级管理员";
                break;
            case 2:
                fxmlFile = "fxml/hr/HRMainView.fxml"; // 人事管理员
                title += "人事管理员";
                break;
            case 3:
                fxmlFile = "fxml/finance/FinanceMainView.fxml"; // 财务管理员
                title += "财务管理员";
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
            controller.setUserInfo(userInfo, authToken); // 👈 传递聚合数据和 Token！

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