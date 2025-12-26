package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.ShiftRule;
import com.gd.hrmsjavafxclient.service.manager.ShiftRuleManagerService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;


public class ShiftRuleController implements ManagerSubController {

    @FXML private TableView<ShiftRule> shiftRuleTable;
    @FXML private TableColumn<ShiftRule, Integer> ruleIdCol;
    @FXML private TableColumn<ShiftRule, String> ruleNameCol;
    @FXML private TableColumn<ShiftRule, String> startTimeCol;
    @FXML private TableColumn<ShiftRule, String> endTimeCol;
    @FXML private TableColumn<ShiftRule, Integer> toleranceCol;

    private final ShiftRuleManagerService shiftRuleService = new ShiftRuleManagerService();
    private final ObservableList<ShiftRule> data = FXCollections.observableArrayList();
    private String authToken;

    @FXML
    public void initialize() {
        ruleIdCol.setCellValueFactory(new PropertyValueFactory<>("ruleId"));
        ruleNameCol.setCellValueFactory(new PropertyValueFactory<>("ruleName"));
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("workStartTime"));
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("workEndTime"));
        toleranceCol.setCellValueFactory(new PropertyValueFactory<>("lateToleranceMin"));

        shiftRuleTable.setItems(data);
    }

    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadShiftRules();
    }


    @FXML
    public void handleRefresh(ActionEvent event) {
        loadShiftRules();
    }


    private void loadShiftRules() {
        if (authToken == null || authToken.isEmpty()) {
            shiftRuleTable.setPlaceholder(new Label("未检测到登录状态，请重新登录。"));
            return;
        }

        Task<List<ShiftRule>> loadTask = new Task<>() {
            @Override
            protected List<ShiftRule> call() throws Exception {
                return shiftRuleService.getAllShiftRules(authToken);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    if (data.isEmpty()) {
                        shiftRuleTable.setPlaceholder(new Label("目前没有定义任何班次规则。"));
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    shiftRuleTable.setPlaceholder(new Label("加载班次规则失败"));
                    showAlert("错误", "无法连接至服务器，请检查网络设置。", Alert.AlertType.ERROR);
                    getException().printStackTrace();
                });
            }
        };

        new Thread(loadTask).start();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}