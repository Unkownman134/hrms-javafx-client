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
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;

public class HRMainController implements MainController {

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

    private CurrentUserInfo currentUser;
    private String token;
    private Button activeNavButton;

    private static final String HR_DASHBOARD_VIEW = "hr/HRDashboardView";
    private static final String HR_EMPLOYEE_VIEW = "hr/EmployeeView";
    private static final String HR_DEPARTMENT_VIEW = "hr/DepartmentView";
    private static final String HR_POSITION_VIEW = "hr/PositionView";
    private static final String HR_RECRUITMENT_VIEW = "hr/RecruitmentView";

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.token = authToken;
        if (userInfoLabel != null && userInfo != null) {
            userInfoLabel.setText("欢迎, " + userInfo.getUsername() + " (HR)");
        }
        // 默认加载仪表盘
        loadView(HR_DASHBOARD_VIEW);
        setActiveButton(dashboardButton);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        App.logout();
    }

    private void loadView(String fxmlPath) {
        try {
            URL url = App.class.getResource("fxml/" + fxmlPath + ".fxml");
            if (url == null) return;
            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof HRSubController) {
                ((HRSubController) controller).setHRContext(currentUser, token);
            }

            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void showDashboardView(ActionEvent event) { setActiveButton((Button) event.getSource()); loadView(HR_DASHBOARD_VIEW); }
    @FXML private void showEmployeeView(ActionEvent event) { setActiveButton((Button) event.getSource()); loadView(HR_EMPLOYEE_VIEW); }
    @FXML private void showDepartmentView(ActionEvent event) { setActiveButton((Button) event.getSource()); loadView(HR_DEPARTMENT_VIEW); }
    @FXML private void showPositionView(ActionEvent event) { setActiveButton((Button) event.getSource()); loadView(HR_POSITION_VIEW); }
    @FXML private void showRecruitmentView(ActionEvent event) { setActiveButton((Button) event.getSource()); loadView(HR_RECRUITMENT_VIEW); }

    private void setActiveButton(Button button) {
        if (button == null) return;
        if (activeNavButton != null) activeNavButton.getStyleClass().remove("nav-button-active");
        activeNavButton = button;
        activeNavButton.getStyleClass().add("nav-button-active");
    }
}