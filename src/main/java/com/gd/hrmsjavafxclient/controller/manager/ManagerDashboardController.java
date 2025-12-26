package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * 部门经理仪表板视图控制器
 */
public class ManagerDashboardController implements ManagerSubController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;

    private CurrentUserInfo currentUser;

    @FXML
    public void initialize() {
    }

    /**
     * 🌟 实现统一接口：接收并设置上下文
     */
    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;

        loadContextAndData();
    }

    /** 业务逻辑初始化 */
    private void loadContextAndData() {
        if (currentUser != null) {
            Platform.runLater(() -> {
                String deptName = currentUser.getDepartmentName() != null ? currentUser.getDepartmentName() : "未知部门";
                welcomeLabel.setText(String.format("欢迎回来，%s！", currentUser.getEmployeeName()));
                roleLabel.setText(String.format("%s (%s)", currentUser.getRoleName(), deptName));
            });
        }
    }
}