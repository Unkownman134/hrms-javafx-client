package com.gd.hrmsjavafxclient.controller.finance;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.controller.MainController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class FinanceMainController implements MainController {

    @FXML private Label userInfoLabel;
    @FXML private StackPane contentPane;

    private CurrentUserInfo currentUser;
    private String token;

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.token = authToken;

        userInfoLabel.setText(String.format("欢迎您，%s (%s)",
                userInfo.getEmployeeName(), userInfo.getRoleName()));

        showDashboardView(); // 默认进入仪表盘
    }

    @FXML
    public void showDashboardView() {
        loadView("/com/gd/hrmsjavafxclient/fxml/finance/FinanceDashboardView.fxml", null);
    }

    @FXML
    public void showSalaryManagementView() {
        loadView("/com/gd/hrmsjavafxclient/fxml/finance/SalaryManagementView.fxml", "salary");
    }

    private void loadView(String fxmlPath, String type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            // 如果是工资管理界面，需要传递 Token 触发数据加载
            if ("salary".equals(type)) {
                SalaryManagementController controller = loader.getController();
                controller.initData(token);
            }

            contentPane.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "确定要注销并退出系统吗？", ButtonType.YES, ButtonType.NO);
        alert.setTitle("确认退出");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                App.logout();
            }
        });
    }
}