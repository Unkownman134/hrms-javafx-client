package com.gd.hrmsjavafxclient.controller.employee;

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
import javafx.scene.layout.VBox;

import java.io.IOException;

public class EmployeeMainController implements MainController {

    @FXML private Label userInfoLabel;
    @FXML private StackPane contentPane;
    @FXML private VBox sideBar;

    private Button activeNavButton = null;
    private CurrentUserInfo currentUser;
    private String authToken;

    public interface EmployeeSubController {
        void setUserInfo(CurrentUserInfo userInfo, String authToken);
        void initializeController();
    }

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;

        Platform.runLater(() -> {
            if (userInfo != null) {
                userInfoLabel.setText("员工: " + userInfo.getEmployeeName());
            }
            loadView("EmployeeDashboardView");
        });
    }

    private void loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gd/hrmsjavafxclient/fxml/employee/" + fxmlFileName + ".fxml"));
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof EmployeeSubController sub) {
                sub.setUserInfo(currentUser, authToken);
                sub.initializeController();
                contentPane.getChildren().setAll(view);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showDashboardView(ActionEvent event) {
        updateNavStyle(event);
        loadView("EmployeeDashboardView");
    }

    @FXML
    public void showApplicationView(ActionEvent event) {
        updateNavStyle(event);
        loadView("EmployeeApplicationView");
    }

    @FXML
    public void showAttendanceRecordView(ActionEvent event) {
        updateNavStyle(event);
        loadView("AttendanceRecordView");
    }

    @FXML
    public void showChangePasswordView(ActionEvent event) {
        updateNavStyle(event);
        loadView("ChangePasswordView");
    }

    @FXML
    public void showSalaryRecordView(ActionEvent event) {
        updateNavStyle(event);
        loadView("SalaryRecordView");
    }

    @FXML
    public void showScheduleView(ActionEvent event) {
        updateNavStyle(event);
        loadView("EmployeeScheduleView");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要注销并退出系统吗？", ButtonType.YES, ButtonType.NO);
        alert.setTitle("确认退出");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                App.logout();
            }
        });
    }

    private void updateNavStyle(ActionEvent event) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("active");
        }
        if (event != null && event.getSource() instanceof Button btn) {
            activeNavButton = btn;
            if (!btn.getStyleClass().contains("active")) {
                btn.getStyleClass().add("active");
            }
        }
    }
}