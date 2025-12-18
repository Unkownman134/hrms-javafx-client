package com.gd.hrmsjavafxclient.controller.finance;

import com.gd.hrmsjavafxclient.App;
import com.gd.hrmsjavafxclient.controller.MainController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
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

        // 设置顶部欢迎信息
        userInfoLabel.setText(String.format("欢迎您，%s (%s) | 部门：%s",
                userInfo.getEmployeeName(),
                userInfo.getRoleName(),
                userInfo.getDepartmentName()));

        // 默认显示仪表盘
        showDashboardView();
    }

    @FXML
    public void showDashboardView() {
        loadView("/com/gd/hrmsjavafxclient/fxml/finance/FinanceDashboardView.fxml");
    }

    @FXML
    public void handlePlaceholderAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("功能开发中");
        alert.setHeaderText(null);
        alert.setContentText("嘿嘿，这个财务管理功能模块还在加紧制作中哦！(๑•̀ㅂ•́)و✧");
        alert.showAndWait();
    }

    @FXML
    public void handleLogout() {
        // 这里可以写退出逻辑，暂时简单打印
        System.out.println("用户请求退出登录...");
        System.exit(0);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node node = loader.load();

            // 如果子界面也有控制器且需要用户信息，可以在这里传递
            // Object controller = loader.getController();

            contentPane.getChildren().setAll(node);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("加载错误");
            alert.setContentText("无法加载界面: " + fxmlPath);
            alert.showAndWait();
        }
    }
}