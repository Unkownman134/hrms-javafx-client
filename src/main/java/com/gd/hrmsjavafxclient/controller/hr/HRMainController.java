package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.controller.MainController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

/**
 * 角色ID=2：人事管理员主界面控制器 (MainController)
 * 移除了薪酬和审批功能。
 */
public class HRMainController implements MainController {

    // --- 内部接口定义：统一传递所有上下文 ---

    /**
     * 子视图控制器接口：所有加载到内容区的子视图控制器都需要实现此接口。
     */
    public interface HRSubController {
        void setHRContext(CurrentUserInfo userInfo, String authToken);
    }

    // --- FXML 控件字段 ---

    @FXML private Label userInfoLabel;
    @FXML private StackPane contentPane;
    @FXML private VBox sideBar;

    // 导航按钮
    @FXML private Button dashboardButton;
    @FXML private Button employeeButton;
    @FXML private Button departmentButton;
    @FXML private Button positionButton;
    @FXML private Button recruitmentButton;
    @FXML private Button settingsButton;

    // 缓存当前激活的按钮
    private Button activeNavButton = null;

    // --- 缓存与状态 ---
    private String authToken;
    private CurrentUserInfo userInfo;

    // FXML 路径常量
    private static final String HR_DASHBOARD_VIEW = "fxml/hr/HRDashboardView.fxml";
    private static final String HR_EMPLOYEE_VIEW = "fxml/hr/EmployeeView.fxml";
    private static final String HR_DEPARTMENT_VIEW = "fxml/hr/DepartmentView.fxml";
    private static final String HR_POSITION_VIEW = "fxml/hr/PositionView.fxml";
    private static final String HR_RECRUITMENT_VIEW = "fxml/hr/RecruitmentView.fxml";

    // --- MainController 接口实现 ---

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        this.userInfo = userInfo;

        String labelText = String.format(
                "%s (%s) | %s | %s",
                userInfo.getEmployeeName(),
                userInfo.getUsername(),
                userInfo.getPositionName(),
                userInfo.getDepartmentName()
        );
        userInfoLabel.setText(labelText);

        if (dashboardButton != null) {
            setActiveButton(dashboardButton);
            loadView(HR_DASHBOARD_VIEW);
        } else {
            loadView(HR_DASHBOARD_VIEW);
        }
    }

    // --- 视图加载核心逻辑 ---

    private void loadView(String fxmlPath) {
        try {
            URL fxmlUrl = App.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                String errorMessage = "无法找到 FXML 视图文件！\n期望路径: " + fxmlPath;
                System.err.println("加载视图文件失败: " + errorMessage);
                showAlert("视图文件丢失 🚫", errorMessage);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof HRSubController subController) {
                subController.setHRContext(userInfo, authToken);
            }

            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);

        } catch (IOException e) {
            System.err.println("加载视图文件失败: " + fxmlPath);
            e.printStackTrace();
            showAlert("视图加载错误 ❌", "加载视图文件时发生IO错误: " + fxmlPath + "\n错误信息: " + e.getMessage());
        }
    }

    // --- 导航按钮动作 ---

    @FXML
    private void showDashboardView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_DASHBOARD_VIEW);
    }

    @FXML
    private void showEmployeeView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_EMPLOYEE_VIEW);
    }

    @FXML
    private void showDepartmentView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_DEPARTMENT_VIEW);
    }

    @FXML
    private void showPositionView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_POSITION_VIEW);
    }

    // 招聘管理
    @FXML
    private void showRecruitmentView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_RECRUITMENT_VIEW);
    }

    @FXML
    private void showSystemSettingsView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        showAlert("提示 💡", "系统设置功能待实现哦！");
    }

    // --- 辅助方法 ---

    private void showAlert(String title, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

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