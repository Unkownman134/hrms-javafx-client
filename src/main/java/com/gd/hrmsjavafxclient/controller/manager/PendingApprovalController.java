package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.ApprovalRequest;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.service.manager.ApprovalManagerService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PendingApprovalController implements ManagerSubController {

    @FXML private TableView<ApprovalRequest> approvalTable;
    @FXML private TableColumn<ApprovalRequest, Integer> idCol;
    @FXML private TableColumn<ApprovalRequest, String> typeCol;
    @FXML private TableColumn<ApprovalRequest, Integer> empCol;
    @FXML private TableColumn<ApprovalRequest, String> reasonCol;
    @FXML private TableColumn<ApprovalRequest, String> statusCol;

    private String authToken;
    private CurrentUserInfo userInfo;
    private final ApprovalManagerService approvalService = new ApprovalManagerService();

    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.userInfo = userInfo;
        this.authToken = authToken;
        loadData();
    }

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("requestType"));
        // 修正：这里对应 ApprovalRequest 类中的字段名，报错提示是 empId
        empCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    public void loadData() {
        if (authToken == null || userInfo == null) return;

        new Thread(() -> {
            try {
                List<ApprovalRequest> data = approvalService.getMyPendingApprovals(
                        authToken,
                        userInfo.getEmpId()
                );
                Platform.runLater(() -> {
                    approvalTable.setItems(FXCollections.observableArrayList(data));
                    approvalTable.refresh();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("刷新失败", "无法连接服务器 QAQ"));
            }
        }).start();
    }

    @FXML
    private void handleProcess() {
        ApprovalRequest selected = approvalTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("提示", "请先选择一行申请哦！");
            return;
        }

        // 选择动作
        List<String> choices = Arrays.asList("同意", "拒绝");
        ChoiceDialog<String> actionDialog = new ChoiceDialog<>("同意", choices);
        actionDialog.setTitle("审批操作");
        actionDialog.setHeaderText("正在处理 ID: " + selected.getRequestId());
        actionDialog.setContentText("操作:");

        Optional<String> actionResult = actionDialog.showAndWait();

        actionResult.ifPresent(action -> {
            // 输入备注
            TextInputDialog commentsDialog = new TextInputDialog();
            commentsDialog.setTitle("审批意见");
            commentsDialog.setHeaderText("动作：" + action);
            commentsDialog.setContentText("请输入理由:");

            Optional<String> commentsResult = commentsDialog.showAndWait();

            commentsResult.ifPresent(comments -> {
                executeSubmit(selected.getRequestId(), action, comments);
            });
        });
    }

    private void executeSubmit(Integer requestId, String action, String comments) {
        new Thread(() -> {
            try {
                approvalService.handleApprovalAction(
                        requestId,
                        action,
                        comments,
                        userInfo.getEmpId(),
                        authToken
                );

                Platform.runLater(() -> {
                    showAlert("成功", "审批已提交！");
                    loadData();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("处理失败", "API 返回错误：" + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}