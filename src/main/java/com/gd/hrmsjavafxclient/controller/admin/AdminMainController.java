package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.controller.MainController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 角色ID=1：超级管理员的主界面控制器
 */
public class AdminMainController implements MainController {

    // --- FXML 控件 ---
    @FXML private Label userInfoLabel;
    @FXML private StackPane contentPane; // 动态内容加载容器
    @FXML private VBox sideBar; // 侧边栏 VBox

    // --- 缓存与状态 ---
    private final Map<String, Parent> viewCache = new HashMap<>();
    private Button activeNavButton = null;

    // 缓存用户信息和 Token
    private CurrentUserInfo currentUser;
    private String authToken; // 🌟 关键修正：新增字段来存储 Token

    @FXML
    public void initialize() {
        // 默认显示用户管理界面
        Platform.runLater(() -> {
            // 找到侧边栏中的第一个按钮（假设是用户管理）
            if (!sideBar.getChildren().isEmpty() && sideBar.getChildren().get(0) instanceof Button firstButton) {
                // 模拟点击第一个按钮，加载默认视图
                showUserView(new ActionEvent(firstButton, firstButton));
            }
        });
    }

    /**
     * 实现 MainController 接口的方法
     */
    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken; // 存储 Token
        userInfoLabel.setText(userInfo.getRoleName() + ": " + userInfo.getUsername() + " (" + userInfo.getEmployeeName() + ")");
    }

    // --- 视图加载方法 ---

    @FXML
    public void showUserView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("fxml/admin/UserManagementView");
    }

    @FXML
    public void showSalaryView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        // 加载薪酬标准管理视图
        loadView("fxml/admin/SalaryStandardManagementView");
    }

    @FXML
    public void showEmployeeView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("fxml/admin/EmployeeManagementView");
    }

    @FXML
    public void showPositionView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("fxml/admin/PositionManagementView");
    }

    @FXML
    public void showDepartmentView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("fxml/admin/DepartmentManagementView");
    }

    @FXML
    public void showDashboardView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("fxml/admin/AdminDashboardView");
    }

    // --- 辅助方法 ---

    /**
     * 核心方法：加载 FXML 视图到 contentPane
     * @param fxmlName FXML 文件的路径（不带 .fxml 后缀）
     */
    private void loadView(String fxmlName) {
        try {
            Parent view;
            String fullFxmlPath = fxmlName + ".fxml";

            // 1. 尝试从缓存加载
            if (viewCache.containsKey(fullFxmlPath)) {
                view = viewCache.get(fullFxmlPath);
            } else {
                // 2. 缓存中没有，通过 FXMLLoader 加载
                FXMLLoader loader = new FXMLLoader(App.class.getResource(fullFxmlPath));
                view = loader.load();
                viewCache.put(fullFxmlPath, view);

                // 3. 将 Token 传递给子 Controller
                Object controller = loader.getController();
                // 确保子 Controller 实现了 ChildController 接口，才能传递 Token
                if (controller instanceof ChildController childController) {
                    childController.setAuthToken(this.authToken);
                }
            }

            // 4. 显示视图
            contentPane.getChildren().setAll(view);

        } catch (IOException e) {
            showNotification("加载界面失败 ❌", "无法加载 " + fxmlName + " 视图文件! 请检查路径。");
            e.printStackTrace();
        } catch (Exception e) {
            showNotification("视图初始化失败 🐞", "初始化 " + fxmlName + " 视图控制器时出错!");
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-button-active");
        }
        activeNavButton = button;
        activeNavButton.getStyleClass().add("nav-button-active");
    }

    private void showNotification(String title, String text) {
        Platform.runLater(() -> {
            Notifications.create()
                    .title(title)
                    .text(text)
                    .hideAfter(Duration.seconds(4))
                    .position(javafx.geometry.Pos.TOP_RIGHT)
                    .show();
        });
    }

    /**
     * 子控制器接口：所有子视图的控制器必须实现此接口
     */
    public interface ChildController {
        void setAuthToken(String authToken);
    }
}