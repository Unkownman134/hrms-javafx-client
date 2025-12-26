package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.controller.MainController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * 部门经理主界面的 Controller。
 */
public class ManagerMainController implements MainController {

    /**
     * 子视图控制器接口：用于统一传递上下文
     */
    public interface ManagerSubController {
        void setManagerContext(CurrentUserInfo userInfo, String authToken);
    }

    @FXML private Label userInfoLabel;
    @FXML private StackPane contentPane;
    @FXML private Button activeNavButton = null;

    private String authToken;
    private CurrentUserInfo currentUserInfo;

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUserInfo = userInfo;
        this.authToken = authToken;

        if (userInfo != null && userInfo.getEmployeeName() != null) {
            userInfoLabel.setText("欢迎回来，" + userInfo.getEmployeeName() + " (经理)");
        }
        // 默认加载仪表盘
        loadView("ManagerDashboardView", null);
    }

    @FXML
    private void showDashboardView(ActionEvent event) {
        loadView("ManagerDashboardView", (Button) event.getSource());
    }

    @FXML
    private void showDeptEmployeeView(ActionEvent event) {
        loadView("DeptEmployeeView", (Button) event.getSource());
    }

    @FXML
    private void showDeptScheduleView(ActionEvent event) {
        loadView("DeptScheduleView", (Button) event.getSource());
    }

    @FXML
    private void showDeptAttendanceView(ActionEvent event) {
        loadView("DeptAttendanceView", (Button) event.getSource());
    }

    @FXML
    private void showShiftRuleView(ActionEvent event) {
        loadView("ShiftRuleView", (Button) event.getSource());
    }

    /**
     * 跳转到“待我审批”界面
     */
    @FXML
    private void showPendingApprovalView(ActionEvent event) {
        loadView("PendingApprovalView", (Button) event.getSource());
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

    private void loadView(String fxmlFileName, Button triggerButton) {
        try {
            setActiveButton(triggerButton);
            String resourcePath = "/com/gd/hrmsjavafxclient/fxml/manager/" + fxmlFileName + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));

            if (loader.getLocation() == null) {
                throw new IOException("无法找到 FXML 资源：" + resourcePath);
            }

            Parent view = loader.load();
            Object controller = loader.getController();

            if (controller instanceof ManagerSubController subController) {
                subController.setManagerContext(this.currentUserInfo, this.authToken);
            }

            contentPane.getChildren().setAll(view);

        } catch (IOException e) {
            System.err.println("加载视图失败：" + fxmlFileName);
            e.printStackTrace();
        }
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