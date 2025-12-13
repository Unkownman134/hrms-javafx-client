package com.gd.hrmsjavafxclient.controller.employee;

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
import org.controlsfx.control.Notifications;
import javafx.util.Duration;

import java.io.IOException;
// 🌟 保持不使用缓存的修正
// import java.util.HashMap;
// import java.util.Map;

/**
 * 默认角色/普通员工主界面控制器
 */
public class EmployeeMainController implements MainController {

    // --- FXML 控件 ---
    @FXML private Label userInfoLabel;
    @FXML private StackPane contentPane; // 动态内容加载容器
    @FXML private VBox sideBar;

    // --- 缓存与状态 ---
    // private final Map<String, Parent> viewCache = new HashMap<>();
    private Button activeNavButton = null;

    // 缓存用户信息和 Token
    private CurrentUserInfo currentUser;
    private String authToken;

    /**
     * 定义一个内部接口，供所有子控制器实现，以便统一操作
     */
    public interface EmployeeSubController {
        void setUserInfo(CurrentUserInfo userInfo, String authToken);
        void initializeController(); // 用于在加载或切换时进行初始化/数据刷新
    }

    // --- MainController 接口实现 ---
    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;

        Platform.runLater(() -> {
            if (userInfo != null) {
                // 显示用户信息
                String info = String.format("%s (%s | %s)",
                        userInfo.getEmployeeName(),
                        userInfo.getPositionName(),
                        userInfo.getRoleName());
                userInfoLabel.setText(info);
            }
            // 初始化时默认加载仪表盘
            // 🌟 关键修正 1: 仪表盘按钮是 sideBar 的第一个子元素，索引为 0
            Button dashboardButton = (Button) sideBar.getChildren().get(0);
            setActiveButton(dashboardButton);
            loadView("EmployeeDashboardView");
        });
    }

    // --- 核心视图加载逻辑 (保持上一次修正，不使用缓存) ---
    private void loadView(String fxmlFileName) {
        String resourcePath = "fxml/employee/" + fxmlFileName + ".fxml";

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(resourcePath));
            Parent view = loader.load();

            // 1. 获取控制器
            Object controller = loader.getController();

            // 2. 检查并初始化子控制器 (修复 fx:controller 缺失导致的控制器为空)
            if (controller instanceof EmployeeSubController subController) {
                subController.setUserInfo(this.currentUser, this.authToken);
                subController.initializeController();

                contentPane.getChildren().setAll(view);

            } else {
                // 如果控制器为空或类型不匹配（这通常意味着 FXML 文件中缺少 fx:controller 属性）
                System.err.println("错误：加载的控制器不是 EmployeeSubController 类型或为空: " + fxmlFileName);
                contentPane.getChildren().setAll(new Label("加载视图失败，控制器错误：" + fxmlFileName));
            }

        } catch (IOException e) {
            System.err.println("无法加载视图: " + fxmlFileName);
            e.printStackTrace();
            contentPane.getChildren().setAll(new Label("加载视图失败: " + fxmlFileName + ".fxml"));
        }
    }

    // --- 导航按钮方法 ---
    @FXML
    public void showDashboardView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("EmployeeDashboardView");
    }

    @FXML
    public void showApplicationView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("EmployeeApplicationView");
    }

    @FXML
    public void showAttendanceRecordView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("AttendanceRecordView");
    }

    @FXML
    public void showSalaryRecordView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("SalaryRecordView");
    }

    // --- 辅助方法 ---
    private void setActiveButton(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-button-active");
        }
        activeNavButton = button;
        activeNavButton.getStyleClass().add("nav-button-active");
    }
}