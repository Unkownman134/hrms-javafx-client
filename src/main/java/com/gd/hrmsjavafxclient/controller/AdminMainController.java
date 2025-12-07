package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox; // 导入VBox
import javafx.util.Duration; // 导入 Duration
import org.controlsfx.control.Notifications; // 🌟 导入 ControlsFX Notifications

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 角色ID=1：超级管理员的主界面控制器
 */
public class AdminMainController implements MainController {

    // 已修改：使用 Label 代替 Text (与 FXML 匹配)
    @FXML private Label userInfoLabel;
    @FXML private StackPane contentPane; // 动态内容加载容器
    @FXML private VBox sideBar; // 侧边栏 VBox

    // 缓存已加载的视图，避免重复加载 FXML
    private final Map<String, Parent> viewCache = new HashMap<>();

    // 跟踪当前选中的按钮
    private Button activeNavButton = null;

    /**
     * FXML加载完成后自动执行，用于初始化视图和导航按钮
     */
    @FXML
    public void initialize() {
        // 默认选中第一个按钮 (仪表盘) 并加载内容
        for (javafx.scene.Node node : sideBar.getChildren()) {
            if (node instanceof Button) {
                Button initialButton = (Button) node;
                setActiveButton(initialButton);
                // 确保内容区域加载了初始视图
                loadView("AdminDashboardView");
                break;
            }
        }
    }


    /**
     * 实现 MainController 接口，接收并显示数据
     */
    @Override
    public void setUserInfo(CurrentUserInfo userInfo) {
        String info = String.format(
                "当前登录人: %s | 身份: %s (RoleID: %d) | 职位: %s",
                userInfo.getEmployeeName(),
                userInfo.getRoleName(),
                userInfo.getRoleId(),
                userInfo.getPositionName()
        );
        userInfoLabel.setText(info);
    }

    /**
     * 导航按钮激活状态控制
     */
    private void setActiveButton(Button newButton) {
        if (activeNavButton != null) {
            // 移除旧按钮的 active 样式
            activeNavButton.getStyleClass().remove("active");
        }
        // 添加新按钮的 active 样式
        newButton.getStyleClass().add("active");
        activeNavButton = newButton;
    }

    /**
     * 根据 FXML 文件名加载并显示视图
     */
    private void loadView(String fxmlFileName) {
        try {
            // 1. 检查缓存
            Parent view = viewCache.get(fxmlFileName);
            if (view == null) {
                // 2. 加载新的 FXML
                FXMLLoader loader = new FXMLLoader(App.class.getResource(fxmlFileName + ".fxml"));
                view = loader.load();
                // 3. 缓存视图
                viewCache.put(fxmlFileName, view);
            }

            // 4. 显示视图
            contentPane.getChildren().setAll(view);

            // 🌟 核心修正：加载成功后，弹出 ControlsFX 通知
            Notifications.create()
                    .title("导航成功 ✅")
                    .text("已成功加载视图：" + fxmlFileName)
                    .darkStyle() // 使用深色样式，配合 hrms-styles.css
                    .hideAfter(Duration.seconds(2)) // 2 秒后自动消失
                    .position(javafx.geometry.Pos.TOP_RIGHT) // 放在右上角
                    .show();

        } catch (IOException e) {
            System.err.println("无法加载视图: " + fxmlFileName);
            e.printStackTrace();
            // 错误反馈
            contentPane.getChildren().setAll(new Label("加载视图失败: " + fxmlFileName + ".fxml"));
        }
    }

    // --- 菜单点击事件：更新视图和激活按钮 ---\

    @FXML
    public void showDashboardView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("AdminDashboardView");
    }

    @FXML
    public void showUserView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("UserManagementView");
    }

    @FXML
    public void showEmployeeView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("EmployeeManagementView");
    }

    @FXML
    public void showPositionView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("PositionManagementView");
    }

    @FXML
    public void showDepartmentView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("DepartmentManagementView");
    }

    @FXML
    public void showSalaryView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("SalaryStandardManagementView");
    }
}