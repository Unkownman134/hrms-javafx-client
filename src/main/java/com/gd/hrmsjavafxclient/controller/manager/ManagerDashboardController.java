package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;


public class ManagerDashboardController implements ManagerSubController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;

    private CurrentUserInfo currentUser;

    @FXML
    public void initialize() {
    }


    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;

        loadContextAndData();
    }

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