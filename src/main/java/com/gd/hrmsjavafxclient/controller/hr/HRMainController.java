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
import javafx.scene.control.ButtonType;
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
    @FXML private Button shiftButton;

    private CurrentUserInfo currentUser;
    private String authToken;

    private static final String HR_DASHBOARD_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/HRDashboardView.fxml";
    private static final String HR_EMPLOYEE_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/EmployeeView.fxml";
    private static final String HR_DEPARTMENT_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/DepartmentView.fxml";
    private static final String HR_POSITION_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/PositionView.fxml";
    private static final String HR_RECRUITMENT_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/RecruitmentView.fxml";
    private static final String HR_SHIFT_VIEW = "/com/gd/hrmsjavafxclient/fxml/hr/ShiftView.fxml";

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
        userInfoLabel.setText("HR: " + userInfo.getEmployeeName() + " | " + userInfo.getDepartmentName());
        loadView(HR_DASHBOARD_VIEW);
        setActiveButton(dashboardButton);
    }


    private void loadView(String fxmlPath) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new IOException("找不到 FXML 资源文件: " + fxmlPath + "\n请检查 resources 目录下的路径是否正确。");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof HRSubController) {
                ((HRSubController) controller).setHRContext(currentUser, authToken);
            }

            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("界面加载失败");
            alert.setHeaderText("无法切换视图");
            alert.setContentText("错误详情: " + e.getMessage());
            alert.show();
        }
    }


    @FXML
    private void showDashboardView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_DASHBOARD_VIEW);
    }

    @FXML
    private void showEmployeeView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_EMPLOYEE_VIEW);
    }

    @FXML
    private void showDepartmentView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_DEPARTMENT_VIEW);
    }

    @FXML
    private void showPositionView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_POSITION_VIEW);
    }

    @FXML
    private void showRecruitmentView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_RECRUITMENT_VIEW);
    }

    @FXML
    private void showShiftView(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView(HR_SHIFT_VIEW);
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


    private void setActiveButton(Button button) {
        sideBar.getChildren().forEach(node -> {
            if (node instanceof Button) {
                node.getStyleClass().remove("active");
            }
        });
        button.getStyleClass().add("active");
    }
}