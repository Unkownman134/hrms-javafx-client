package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 人事管理员 (RoleID=2) 仪表盘子视图控制器
 * 修正：实现 HRMainController.HRSubController 接口
 */
public class HRDashboardController implements HRMainController.HRSubController {

    @FXML private Label dashboardTitle;
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label empIdLabel;
    @FXML private Label dateTimeLabel;

    @FXML
    public void initialize() {
    }

    /**
     * 实现 HRSubController 接口，用于接收父控制器的用户信息和 Token。
     * 修正：修改方法签名和名称
     */
    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        dashboardTitle.setText("人事管理员工作台");

        welcomeLabel.setText("欢迎回来，人事管理员 " + userInfo.getEmployeeName() + "！");
        roleLabel.setText("当前角色: " + userInfo.getRoleName() + " (ID: " + userInfo.getRoleId() + ")");

        updateDateTime();
    }

    private void updateDateTime() {
        LocalDateTime nowJST = LocalDateTime.now();
        LocalDateTime nowBJT = nowJST.minusHours(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

        Platform.runLater(() -> {
            dateTimeLabel.setText("北京时间: " + nowBJT.format(formatter));
        });
    }
}