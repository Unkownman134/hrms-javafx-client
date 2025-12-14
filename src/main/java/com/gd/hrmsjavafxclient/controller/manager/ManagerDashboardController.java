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
    @FXML private Label lateCountLabel;
    @FXML private Label pendingApprovalLabel;
    @FXML private Label deptEmpCountLabel; // 部门员工总数

    private CurrentUserInfo currentUser;
    private String authToken;

    @FXML
    public void initialize() {
        // FXML 初始化逻辑（例如 TableView 列的设置），这里没有所以留空
    }

    /**
     * 🌟 实现统一接口：接收并设置上下文
     */
    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;

        // 接收到上下文后，启动初始化和数据加载
        loadContextAndData();
    }

    /** 业务逻辑初始化 */
    private void loadContextAndData() {
        if (currentUser != null) {
            Platform.runLater(() -> {
                String deptName = currentUser.getDepartmentName() != null ? currentUser.getDepartmentName() : "未知部门";
                welcomeLabel.setText(String.format("欢迎回来，%s！", currentUser.getEmployeeName()));
                roleLabel.setText(String.format("%s (%s)", currentUser.getRoleName(), deptName));

                // 🌟 调用 Service 获取仪表盘数据
                fetchDashboardData();
            });
        }
    }

    /**
     * 模拟从后端获取部门关键数据的过程
     */
    private void fetchDashboardData() {
        // 这里的数值是硬编码的，实际应通过 API 获取
        int lateCount = 1;
        int pending = 3;
        int totalEmployees = 15;

        Platform.runLater(() -> {
            lateCountLabel.setText(String.valueOf(lateCount));
            pendingApprovalLabel.setText(String.valueOf(pending));
            deptEmpCountLabel.setText(String.valueOf(totalEmployees));
        });
    }
}