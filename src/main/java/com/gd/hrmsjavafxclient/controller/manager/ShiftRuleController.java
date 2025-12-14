package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.ShiftRule;
import com.gd.hrmsjavafxclient.service.ShiftRuleManagerService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.util.List;

/**
 * 班次规则视图控制器 (t_shift_rule)
 * 🌟 修正：将 TableColumn 绑定到 ShiftRule Model 中正确的属性名称 (workStartTime / workEndTime)。
 */
public class ShiftRuleController implements ManagerSubController {

    @FXML private TableView<ShiftRule> shiftRuleTable;
    @FXML private TableColumn<ShiftRule, Integer> ruleIdCol;
    @FXML private TableColumn<ShiftRule, String> ruleNameCol;
    // 尽管 FXML 中的 fx:id 是 startTimeCol/endTimeCol，但 PropertyValueFactory 必须使用 Model 中的属性名
    @FXML private TableColumn<ShiftRule, String> startTimeCol;
    @FXML private TableColumn<ShiftRule, String> endTimeCol;
    @FXML private TableColumn<ShiftRule, Integer> toleranceCol;

    // --- 数据和状态 ---
    private final ObservableList<ShiftRule> data = FXCollections.observableArrayList();
    private final ShiftRuleManagerService shiftRuleService = new ShiftRuleManagerService();

    private String authToken;

    @FXML
    public void initialize() {
        // 初始化 TableView 列绑定
        ruleIdCol.setCellValueFactory(new PropertyValueFactory<>("ruleId"));
        ruleNameCol.setCellValueFactory(new PropertyValueFactory<>("ruleName"));

        // 🚨 修正点 1：将 "startTime" 绑定到 ShiftRule 模型中的 "workStartTime"
        startTimeCol.setCellValueFactory(new PropertyValueFactory<>("workStartTime"));

        // 🚨 修正点 2：将 "endTime" 绑定到 ShiftRule 模型中的 "workEndTime"
        endTimeCol.setCellValueFactory(new PropertyValueFactory<>("workEndTime"));

        // 假设 Model 中的 lateToleranceMin 对应 FXML 中的 toleranceCol
        toleranceCol.setCellValueFactory(new PropertyValueFactory<>("lateToleranceMin"));

        shiftRuleTable.setItems(data);

        // 初始化时设置占位符
        shiftRuleTable.setPlaceholder(new Label("等待加载班次规则..."));
    }

    /**
     * 实现统一接口：接收并设置上下文
     */
    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        // 接收到上下文后，立即加载数据
        Platform.runLater(this::loadShiftRules);
    }

    /**
     * 异步加载班次规则列表
     */
    private void loadShiftRules() {
        if (authToken == null) {
            showAlert("错误 ❌", "认证信息丢失，无法加载班次规则。", Alert.AlertType.ERROR);
            return;
        }

        Task<List<ShiftRule>> loadTask = new Task<>() {
            @Override
            protected List<ShiftRule> call() throws Exception {
                // 🌟 Service 抛出 InterruptedException 和 IOException，必须处理
                try {
                    return shiftRuleService.getAllShiftRules(authToken);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 重新设置中断标志
                    throw new IOException("API 请求被中断。", e);
                }
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    if (data.isEmpty()) {
                        shiftRuleTable.setPlaceholder(new Label("目前没有定义任何班次规则哦。"));
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    shiftRuleTable.setPlaceholder(new Label("加载班次规则失败 😭: " + getException().getMessage()));
                    showAlert("错误 ❌", "加载班次规则失败：\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    getException().printStackTrace();
                });
            }
        };

        new Thread(loadTask).start();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}