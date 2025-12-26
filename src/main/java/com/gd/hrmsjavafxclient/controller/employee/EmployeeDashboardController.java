package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


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
                empIdLabel.setText("员工编号 (EmpID): " + currentUser.getEmpId());
                updateTime();
            });
        }
    }

    private void updateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");
        dateTimeLabel.setText("当前时间 (北京时间): " + LocalDateTime.now().format(formatter));
    }
}