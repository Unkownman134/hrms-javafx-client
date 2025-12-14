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
import javafx.scene.layout.VBox; // 假设导航栏是 VBox

import java.io.IOException;
import java.net.URL;

/**
 * 角色ID=2：人事管理员主界面控制器 (MainController)
 * 实现了视图切换逻辑，并将子控制器接口定义在内部。
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

    @FXML private Label userInfoLabel; // 顶部显示用户信息
    @FXML private StackPane contentPane; // 中间内容区域
    @FXML private VBox sideBar; // 假设左侧导航栏是 VBox

    // 导航按钮 (用于高亮显示)
    @FXML private Button dashboardButton;
    @FXML private Button employeeButton;
    @FXML private Button departmentButton;
    @FXML private Button positionButton;
    @FXML private Button recruitmentButton; // 🌟 新增：招聘管理按钮
    @FXML private Button salaryButton;      // 🌟 新增：薪酬福利按钮
    @FXML private Button applicationButton; // 🌟 新增：审批管理按钮
    @FXML private Button settingsButton;    // 🌟 新增：系统设置按钮

    // 缓存当前激活的按钮
    private Button activeNavButton = null;

    // --- 缓存与状态 ---
    private String authToken; // 缓存认证 Token
    private CurrentUserInfo userInfo; // 缓存当前用户信息

    // FXML 路径常量 (假设所有 FXML 都在 resources/fxml/hr 目录下)
    private static final String HR_DASHBOARD_VIEW = "fxml/hr/HRDashboardView.fxml";
    // 🌟 关键修正：路径常量应指向您上传的 EmployeeView.fxml
    private static final String HR_EMPLOYEE_VIEW = "fxml/hr/EmployeeView.fxml";
    private static final String HR_DEPARTMENT_VIEW = "fxml/hr/DepartmentView.fxml";
    private static final String HR_POSITION_VIEW = "fxml/hr/PositionView.fxml";

    // --- MainController 接口实现 ---

    /**
     * 实现 MainController 接口，用于登录成功后初始化主界面。
     */
    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        this.userInfo = userInfo;

        // 显示聚合的用户信息 (与您的 LoginController 逻辑保持一致)
        String labelText = String.format(
                "%s (%s) | %s | %s",
                userInfo.getEmployeeName(),
                userInfo.getUsername(),
                userInfo.getPositionName(),
                userInfo.getDepartmentName()
        );
        userInfoLabel.setText(labelText);

        // 默认加载仪表盘
        // 确保在初始化后再调用，因为需要 userInfo/authToken
        if (dashboardButton != null) {
            setActiveButton(dashboardButton);
            loadView(HR_DASHBOARD_VIEW);
        } else {
            // 如果 FXML 中没有 dashboardButton，则直接加载
            loadView(HR_DASHBOARD_VIEW);
        }
    }

    // --- 视图加载核心逻辑 ---

    /**
     * 加载并显示指定路径的子视图。
     * @param fxmlPath FXML 文件的路径 (相对于 classpath)
     */
    private void loadView(String fxmlPath) {
        try {
            // 🌟 关键修正：首先检查资源是否存在
            URL fxmlUrl = App.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                String errorMessage = "无法找到 FXML 视图文件！\n" +
                        "期望路径: " + fxmlPath +
                        "\n请检查 resources 目录下文件名称和路径是否正确。";
                System.err.println("加载视图文件失败: " + errorMessage);
                showAlert("视图文件丢失 🚫", errorMessage);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            // 尝试获取子视图的控制器
            Object controller = loader.getController();
            // 🚨 注意：这里使用内部定义的 HRSubController 接口
            if (controller instanceof HRSubController subController) {
                // 确保子控制器可以访问到必要的信息
                subController.setHRContext(userInfo, authToken);
            }

            // 清除旧内容并加载新内容
            contentPane.getChildren().clear();
            contentPane.getChildren().add(view);

        } catch (IOException e) {
            System.err.println("加载视图文件失败: " + fxmlPath);
            e.printStackTrace();
            showAlert("视图加载错误 ❌", "加载视图文件时发生IO错误: " + fxmlPath + "\n错误信息: " + e.getMessage());
        }
    }

    // --- 导航按钮动作 ---

    // 假设 HRMainView.fxml 中按钮的 onAction 绑定了以下方法

    @FXML
    private void showDashboardView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_DASHBOARD_VIEW);
    }

    @FXML
    private void showEmployeeView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        // 🌟 使用修正后的常量
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

    // 🌟 补齐缺失的方法 1: 招聘管理
    @FXML
    private void showRecruitmentView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        showAlert("提示 💡", "招聘管理功能待实现哦！");
        // loadView("fxml/hr/RecruitmentView.fxml"); // 待创建 FXML
    }

    // 🌟 补齐缺失的方法 2: 薪酬福利
    @FXML
    private void showSalaryBenefitView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        showAlert("提示 💡", "薪酬福利功能待实现哦！");
        // loadView("fxml/hr/SalaryBenefitView.fxml"); // 待创建 FXML
    }

    // 🌟 补齐缺失的方法 3: 审批管理
    @FXML
    private void showApplicationView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        showAlert("提示 💡", "审批管理功能待实现哦！");
        // loadView("fxml/hr/ApplicationView.fxml"); // 待创建 FXML
    }

    // 🌟 补齐缺失的方法 4: 系统设置 (虽然在 FXML 中被禁用，但方法定义不能少)
    @FXML
    private void showSystemSettingsView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        showAlert("提示 💡", "系统设置功能待实现哦！");
        // loadView("fxml/hr/SystemSettingsView.fxml"); // 待创建 FXML
    }

    // --- 辅助方法 ---

    private void showAlert(String title, String content) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void setActiveButton(Button button) {
        if (button == null) return;

        // 移除旧按钮的激活样式
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-button-active");
        }

        // 设置新按钮的激活样式
        activeNavButton = button;
        if (!activeNavButton.getStyleClass().contains("nav-button-active")) {
            activeNavButton.getStyleClass().add("nav-button-active");
        }
    }
}