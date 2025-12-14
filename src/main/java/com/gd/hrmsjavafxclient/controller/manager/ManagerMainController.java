package com.gd.hrmsjavafxclient.controller.manager;

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

import java.io.IOException;

/**
 * 部门经理主界面的 Controller。
 */
public class ManagerMainController implements MainController {

    // --- 内部接口定义（新）：统一传递所有上下文 ---

    /**
     * 子视图控制器接口：所有加载到内容区的子视图控制器都需要实现此接口，
     * 以接收认证 Token 和用户信息。
     */
    public interface ManagerSubController {
        void setManagerContext(CurrentUserInfo userInfo, String authToken);
    }

    // --- FXML 控件字段 ---

    @FXML private Label userInfoLabel;
    @FXML private StackPane contentPane; // 动态内容加载容器
    @FXML private Button activeNavButton = null;

    // --- 缓存与状态 ---
    private String authToken;
    private CurrentUserInfo currentUserInfo;

    // ------------------------------------------------------------------
    // MainController 接口实现
    // ------------------------------------------------------------------

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        this.currentUserInfo = userInfo;

        // 更新顶部的用户信息标签
        String userDisplay = String.format("欢迎，%s (部门: %s / 职位: %s)",
                userInfo.getEmployeeName(),
                userInfo.getDepartmentName(),
                userInfo.getPositionName());
        userInfoLabel.setText(userDisplay);

        // 🌟 修正：启动后加载默认视图，使用无参方法
        Platform.runLater(this::loadDefaultView);
    }

    // ------------------------------------------------------------------
    // FXML 导航事件处理器
    // ------------------------------------------------------------------

    /** * 修正：方法名改为 logout，以匹配 ManagerMainView.fxml 中的 onAction="#logout"
     */
    @FXML
    public void logout(ActionEvent event) {
        App.logout();
    }

    // --- 导航视图切换方法 (供 FXML 按钮调用) ---

    @FXML
    public void showDashboardView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("ManagerDashboardView"); // 视图名称修正为 ManagerDashboardView
    }

    @FXML
    public void showDeptEmployeeView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("DeptEmployeeView");
    }

    @FXML
    public void showDeptScheduleView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("DeptScheduleView");
    }

    @FXML
    public void showDeptAttendanceView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("DeptAttendanceView");
    }

    @FXML
    public void showShiftRuleView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("ShiftRuleView");
    }

    @FXML
    public void showApprovalView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("ApprovalView");
    }

    // ------------------------------------------------------------------
    // 核心功能方法
    // ------------------------------------------------------------------

    /**
     * 启动时加载默认视图，不带 ActionEvent 参数
     */
    private void loadDefaultView() {
        loadView("ManagerDashboardView");
    }

    /**
     * 动态加载指定的 FXML 视图并替换主界面的内容区域。
     * @param fxmlFileName FXML文件名（例如 "ManagerDashboardView"）
     */
    private void loadView(String fxmlFileName) {
        try {
            // 路径修正：使用绝对路径
            String resourcePath = "/com/gd/hrmsjavafxclient/fxml/manager/" + fxmlFileName + ".fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));

            if (loader.getLocation() == null) {
                throw new IOException("无法找到 FXML 资源 (Location is not set)：请检查文件路径是否为 " + resourcePath);
            }

            Parent view = loader.load();

            // 视图控制器初始化 (传递上下文)
            Object controller = loader.getController();

            // 🚨 关键修正：调用新的 setManagerContext 方法
            if (controller instanceof ManagerSubController subController) {
                subController.setManagerContext(this.currentUserInfo, this.authToken);
            }

            // 替换内容区域
            contentPane.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("加载视图文件失败，文件：" + fxmlFileName + ".fxml");
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------
    // 辅助方法 (导航栏按钮高亮逻辑)
    // ------------------------------------------------------------------

    private void setActiveButton(Button button) {
        if (button == null) return;

        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-button-active");
        }

        activeNavButton = button;
        if (!activeNavButton.getStyleClass().contains("nav-button-active")) {
            activeNavButton.getStyleClass().add("nav-button-active");
        }
    }
}