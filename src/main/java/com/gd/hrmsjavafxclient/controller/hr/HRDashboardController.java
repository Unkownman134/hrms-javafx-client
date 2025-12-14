package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
// ⚠️ 移除对外部 SubController 的引用

/**
 * 人事管理员 (RoleID=2) 仪表盘子视图控制器
 * 🌟 修正：实现 HRMainController.HRSubController 接口
 */
public class HRDashboardController implements HRMainController.HRSubController { // 👈 关键修改

    @FXML private Label dashboardTitle;
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label empIdLabel;
    @FXML private Label dateTimeLabel;

    @FXML
    public void initialize() {
        // 任何不需要 CurrentUserInfo/Token 的初始化工作放这里
    }

    /**
     * 实现 HRSubController 接口，用于接收父控制器的用户信息和 Token。
     * 🌟 修正：修改方法签名和名称
     */
    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) { // 👈 关键修改
        // 设置仪表盘信息
        dashboardTitle.setText("人事管理员工作台");

        welcomeLabel.setText("欢迎回来，人事管理员 " + userInfo.getEmployeeName() + "！");
        roleLabel.setText("当前角色: " + userInfo.getRoleName() + " (ID: " + userInfo.getRoleId() + ")");

        // 可以在这里显示一些 HR 相关的核心指标，如：
        // 1. 本月待审批申请数 (需要调用 API)
        // 2. 待入职员工数 (需要调用 API)
        // 3. 员工总数

        // 显示北京时间
        updateDateTime();
    }

    // 根据用户的偏好设置，显示北京时间 (UTC+8)
    private void updateDateTime() {
        // 当前时间是：2025年12月14日 23:22:35 (JST, UTC+9)
        // 转换为北京时间 (UTC+8)
        LocalDateTime nowJST = LocalDateTime.now();
        LocalDateTime nowBJT = nowJST.minusHours(1); // JST 比 BJT 快一小时
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

        Platform.runLater(() -> {
            dateTimeLabel.setText("北京时间: " + nowBJT.format(formatter));
        });
    }
}