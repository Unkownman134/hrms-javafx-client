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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

/**
 * HR 主界面控制器
 * 负责切换不同的子功能视图
 */
public class HRMainController implements MainController {

    /**
     * 定义子控制器接口，用于传递登录上下文
     */
    public interface HRSubController {
        void setHRContext(CurrentUserInfo userInfo, String authToken);
    }

    @FXML private Label userInfoLabel;
    @FXML private StackPane contentPane;
    @FXML private VBox sideBar;

    @FXML private Button dashboardButton;
    @FXML private Button employeeButton;
    @FXML private Button departmentButton;
    @FXML private Button positionButton;
    @FXML private Button recruitmentButton;
    @FXML private Button shiftButton;

    private CurrentUserInfo currentUser;
    private String authToken;

    // 路径常量，请务必确认这些文件位于 resources/com/gd/hrmsjavafxclient/fxml/hr/ 目录下
    private static final String HR_DASHBOARD_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/HRDashboardView.fxml";
    private static final String HR_EMPLOYEE_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/EmployeeView.fxml";
    private static final String HR_DEPARTMENT_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/DepartmentView.fxml";
    private static final String HR_POSITION_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/PositionView.fxml";
    private static final String HR_RECRUITMENT_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/RecruitmentView.fxml";
    private static final String HR_SHIFT_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/ShiftView.fxml";

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
        // 设置顶栏用户信息
        userInfoLabel.setText("👤 HR: " + userInfo.getEmployeeName() + " | " + userInfo.getDepartmentName());
        // 默认加载仪表盘
        loadView(HR_DASHBOARD_VIEW);
        setActiveButton(dashboardButton);
    }

    /**
     * 核心加载方法：动态切换中间 contentPane 的内容
     */
    private void loadView(String fxmlPath) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IOException("找不到 FXML 资源文件: " + fxmlPath + "\n请检查 resources 目录下的路径是否正确。");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            // 如果子控制器需要用户信息，则进行传递
            Object controller = loader.getController();
            if (controller instanceof HRSubController) {
                ((HRSubController) controller).setHRContext(currentUser, authToken);
            }

            // 将新视图放入 StackPane
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("界面加载失败");
            alert.setHeaderText("无法切换视图");
            alert.setContentText("错误详情: " + e.getMessage());
            alert.show();
        }
    }

    // --- 按钮点击事件处理 ---

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

    @FXML
    private void showRecruitmentView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_RECRUITMENT_VIEW);
    }

    @FXML
    private void showShiftView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_SHIFT_VIEW);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要注销并退出系统吗？", ButtonType.YES, ButtonType.NO);
        alert.setTitle("确认退出");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                App.logout();
            }
        });
    }

    /**
     * 切换侧边栏按钮的激活状态样式
     */
    private void setActiveButton(Button button) {
        sideBar.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("active");
            }
        });
        button.getStyleClass().add("active");
    }
}