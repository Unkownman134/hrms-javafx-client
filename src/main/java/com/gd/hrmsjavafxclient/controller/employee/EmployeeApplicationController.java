package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.ApprovalRequest;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.service.employee.ApplicationEmpService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EmployeeApplicationController implements EmployeeSubController {

    @FXML private TableView<ApprovalRequest> applicationTable;
    @FXML private TableColumn<ApprovalRequest, Integer> idCol;
    @FXML private TableColumn<ApprovalRequest, String> typeCol;
    @FXML private TableColumn<ApprovalRequest, LocalDate> startCol;
    @FXML private TableColumn<ApprovalRequest, LocalDate> endCol;
    @FXML private TableColumn<ApprovalRequest, String> reasonCol;
    @FXML private TableColumn<ApprovalRequest, String> statusCol;

    private final ApplicationEmpService applicationEmpService = new ApplicationEmpService();
    private CurrentUserInfo currentUser;
    private String authToken;

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @Override
    public void initializeController() {
        setupTableColumns();
        loadApplicationData();
    }

    private void setupTableColumns() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("requestType"));
        startCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endCol.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("待审批")) {
                        setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");
                    } else if (item.contains("已通过")) {
                        setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
                    } else if (item.contains("已拒绝")) {
                        setStyle("-fx-text-fill: #C0392B; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    @FXML
    public void loadApplicationData() {
        if (currentUser == null || authToken == null) return;

        Task<List<ApprovalRequest>> task = new Task<>() {
            @Override
            protected List<ApprovalRequest> call() throws Exception {
                return applicationEmpService.getMyApplications(currentUser.getEmpId(), authToken);
            }

            @Override
            protected void succeeded() {
                List<ApprovalRequest> data = getValue();
                if (data != null) {
                    applicationTable.setItems(FXCollections.observableArrayList(data));
                    applicationTable.refresh();
                }
            }

            @Override
            protected void failed() {
                Throwable exception = getException();
                exception.printStackTrace();
                showAlert("列表加载失败", "原因: " + exception.getMessage(), Alert.AlertType.ERROR);
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleWithdrawApplication() {
        ApprovalRequest selected = applicationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("提示", "请先从列表中选择要撤销的申请单！", Alert.AlertType.WARNING);
            return;
        }

        // 业务规则：只有待审批可以撤销
        if (!"待审批".equals(selected.getStatus())) {
            showAlert("操作无效", "只有'待审批'状态的申请单才可以撤销。", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认撤销");
        confirm.setHeaderText(null);
        confirm.setContentText("确定要撤销申请单 #" + selected.getRequestId() + " 吗？");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Boolean> withdrawTask = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        return applicationEmpService.withdrawApplication(
                                selected.getRequestId(),
                                currentUser.getEmpId(),
                                authToken
                        );
                    }

                    @Override
                    protected void succeeded() {
                        showAlert("成功", "申请已成功撤销！", Alert.AlertType.INFORMATION);
                        loadApplicationData();
                    }

                    @Override
                    protected void failed() {
                        showAlert("错误", "撤销失败: " + getException().getMessage(), Alert.AlertType.ERROR);
                    }
                };
                new Thread(withdrawTask).start();
            }
        });
    }

    @FXML
    public void handleAddNewApplication() {
        Dialog<ApprovalRequest> dialog = new Dialog<>();
        dialog.setTitle("新增申请");
        dialog.setHeaderText("填写申请信息");

        ButtonType submitButtonType = new ButtonType("提交", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("请假", "报销", "出差"));
        typeBox.setValue("请假");
        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now());
        TextField reasonField = new TextField();

        grid.add(new Label("类型:"), 0, 0); grid.add(typeBox, 1, 0);
        grid.add(new Label("开始日期:"), 0, 1); grid.add(startPicker, 1, 1);
        grid.add(new Label("结束日期:"), 0, 2); grid.add(endPicker, 1, 2);
        grid.add(new Label("原因:"), 0, 3); grid.add(reasonField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                ApprovalRequest req = new ApprovalRequest();
                req.setEmpId(currentUser.getEmpId());
                req.setRequestType(typeBox.getValue());
                req.setStartDate(startPicker.getValue());
                req.setEndDate(endPicker.getValue());
                req.setReason(reasonField.getText().trim());
                req.setStatus("待审批");
                return req;
            }
            return null;
        });

        Optional<ApprovalRequest> result = dialog.showAndWait();
        result.ifPresent(request -> {
            Task<Boolean> submitTask = new Task<>() {
                @Override protected Boolean call() throws Exception {
                    return applicationEmpService.submitApplication(request, authToken);
                }
                @Override protected void succeeded() { loadApplicationData(); }
                @Override protected void failed() { showAlert("错误", "提交失败", Alert.AlertType.ERROR); }
            };
            new Thread(submitTask).start();
        });
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert a = new Alert(type);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(msg);
            a.showAndWait();
        });
    }
}