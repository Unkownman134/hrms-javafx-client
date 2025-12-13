package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 员工仪表板视图控制器
 * 🌟 遵循 EmployeeSubController 接口。
 */
public class EmployeeDashboardController implements EmployeeSubController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label empIdLabel;
    @FXML private Label dateTimeLabel;

    private CurrentUserInfo currentUser;
    private String authToken;

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @Override
    public void initializeController() {
        if (currentUser != null) {
            Platform.runLater(() -> {
                welcomeLabel.setText(String.format("欢迎回来，%s！", currentUser.getEmployeeName()));
                roleLabel.setText(currentUser.getRoleName() + " (" + currentUser.getPositionName() + ")");
                // 使用修正后的 CurrentUserInfo 中的 EmpID
                empIdLabel.setText("员工编号 (EmpID): " + currentUser.getEmpId());
                updateTime();
            });
        }
    }

    /**
     * 假设我们定期更新时间
     */
    private void updateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
        // 这里可以启动一个定时任务来更新时间，但为简化，只显示加载时的时间
        dateTimeLabel.setText("当前时间 (北京时间): " + LocalDateTime.now().format(formatter));
    }
}