package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.controller.MainController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;

public class AdminMainController implements MainController {

    @FXML private Label userInfoLabel;
    @FXML private StackPane contentPane;
    @FXML private VBox sideBar;

    private Button activeBtn;
    private String token;
    private CurrentUserInfo currentUser;

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.token = authToken;

        Platform.runLater(() -> {
            if (userInfo != null) {
                userInfoLabel.setText("管理员: " + userInfo.getUsername());
            }
            showDashboardView();
        });
    }

    @FXML public void showDashboardView() {
        loadView("/com/gd/hrmsjavafxclient/fxml/admin/AdminDashboardView.fxml", null);
    }

    @FXML public void showUserView() {
        loadView("/com/gd/hrmsjavafxclient/fxml/admin/UserManagementView.fxml", "btnUser");
    }

    @FXML public void showEmployeeView() {
        loadView("/com/gd/hrmsjavafxclient/fxml/admin/EmployeeManagementView.fxml", "btnEmployee");
    }

    @FXML public void showDepartmentView() {
        loadView("/com/gd/hrmsjavafxclient/fxml/admin/DepartmentManagementView.fxml", "btnDept");
    }

    @FXML public void showPositionView() {
        loadView("/com/gd/hrmsjavafxclient/fxml/admin/PositionManagementView.fxml", "btnPos");
    }

    @FXML public void showSalaryView() {
        loadView("/com/gd/hrmsjavafxclient/fxml/admin/SalaryStandardManagementView.fxml", "btnSalary");
    }

    private void loadView(String path, String btnId) {
        try {
            URL fxmlLocation = getClass().getResource(path);
            if (fxmlLocation == null) {
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            if (contentPane != null) {
                contentPane.getChildren().setAll(root);
            }

            if (btnId != null) {
                updateNavStyle(btnId);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNavStyle(String id) {
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("active");
        }

        sideBar.getChildren().forEach(n -> {
            if (n instanceof Button b && id.equals(b.getId())) {
                activeBtn = b;
                if (!b.getStyleClass().contains("active")) {
                    b.getStyleClass().add("active");
                }
            }
        });
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要注销并退出系统吗？", ButtonType.YES, ButtonType.NO);
        alert.setTitle("确认退出");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                App.logout();
            }
        });
    }
}