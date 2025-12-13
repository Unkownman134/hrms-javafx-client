package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.ApprovalRequest;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.service.EmployeeService;
import com.gd.hrmsjavafxclient.service.EmployeeServiceImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

/**
 * 我的申请视图控制器 (对应 EmployeeApplicationView.fxml)
 * 🌟 修正：实例化 EmployeeServiceImpl，并在提交申请时使用 EmpID。
 */
public class EmployeeApplicationController implements EmployeeSubController {

    @FXML private TextField applicantNameField;
    @FXML private ComboBox<String> applicationTypeComboBox;
    @FXML private DatePicker relatedDateField;
    @FXML private TextField relatedDetailField;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button submitButton;
    // 假设还有一个 TableView 来展示历史申请

    // --- 数据和状态 ---
    // 🌟 修正：直接实例化实现类
    private final EmployeeService employeeService = new EmployeeServiceImpl();
    private CurrentUserInfo currentUser;
    private String authToken;

    // --- 初始化和数据设置 ---
    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @Override
    public void initializeController() {
        if (currentUser != null) {
            Platform.runLater(() -> {
                // 1. 设置申请人姓名（只读）
                applicantNameField.setText(currentUser.getEmployeeName());
                applicantNameField.setEditable(false);

                // 2. 初始化申请类型 ComboBox
                if (applicationTypeComboBox.getItems().isEmpty()) {
                    applicationTypeComboBox.setItems(FXCollections.observableArrayList(
                            "请假申请", "加班申请", "报销申请", "其他"
                    ));
                    applicationTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                        updatePlaceholders(newVal);
                    });
                }
                updatePlaceholders(applicationTypeComboBox.getValue()); // 初始化提示

                // 3. 重置表单
                // clearForm(); // 不应该在每次 initialize 都清空，但这里保证表单状态
            });
        }
    }

    // --- 关键修正：添加缺失的 FXML 事件方法 ---
    /**
     * 处理“查看历史申请”按钮的点击事件 (修复 LoadException)
     */
    @FXML
    public void handleViewHistoryButtonAction(ActionEvent event) {
        showAlert("提示 ⏳", "查看历史申请功能正在努力实现中哦！", Alert.AlertType.INFORMATION);
        // 这里将来可以添加加载历史申请列表的逻辑
    }
    // --- 关键修正结束 ---

    @FXML
    private void handleSubmitButtonAction() {
        if (!validateInput()) {
            return;
        }

        submitButton.setDisable(true);
        submitButton.setText("提交中...");

        // 构建请求模型
        ApprovalRequest request = new ApprovalRequest();

        // 🌟 关键修正 1: setEmpId -> setApplicantId
        request.setApplicantId(currentUser.getEmpId()); // 使用员工 ID

        // 🌟 关键修正 2: setRequestType -> setApplicationType
        request.setApplicationType(applicationTypeComboBox.getValue());

        // 🌟 关键修正 3: setRequestDate -> setSubmissionDate
        request.setSubmissionDate(LocalDate.now()); // 提交日期为今天

        request.setRelatedDate(relatedDateField.getValue());
        request.setRelatedDetail(relatedDetailField.getText().trim());
        request.setDescription(descriptionTextArea.getText().trim());
        request.setStatus("待审批"); // 初始状态

        Task<Boolean> submitTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // 调用服务层 API
                return employeeService.submitApplication(request, authToken);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    submitButton.setDisable(false);
                    submitButton.setText("提 交");

                    if (success) {
                        showAlert("成功 🎉", "申请已成功提交，等待上级审批哦！", Alert.AlertType.INFORMATION);
                        clearForm();
                    } else {
                        showAlert("失败 😢", "申请提交失败，请稍后再试或联系管理员。", Alert.AlertType.ERROR);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    submitButton.setDisable(false);
                    submitButton.setText("提 交");
                    showAlert("错误 ❌", "申请提交过程中发生错误：" + getException().getMessage(), Alert.AlertType.ERROR);
                    getException().printStackTrace();
                });
            }
        };
        new Thread(submitTask).start();
    }

    private boolean validateInput() {
        if (applicationTypeComboBox.getValue() == null) {
            showAlert("验证失败", "请选择申请类型哦。", Alert.AlertType.WARNING);
            return false;
        }
        if (relatedDateField.getValue() == null) {
            showAlert("验证失败", "请选择相关日期哦。", Alert.AlertType.WARNING);
            return false;
        }
        if (relatedDetailField.getText().trim().isEmpty()) {
            showAlert("验证失败", "请填写关联事项，例如时长或金额。", Alert.AlertType.WARNING);
            return false;
        }
        if (descriptionTextArea.getText().trim().isEmpty()) {
            showAlert("验证失败", "请填写详细的申请描述和理由哦。", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void clearForm() {
        Platform.runLater(() -> {
            applicationTypeComboBox.getSelectionModel().clearSelection();
            relatedDateField.setValue(null);
            relatedDetailField.clear();
            descriptionTextArea.clear();
            updatePlaceholders(null); // 重置提示
        });
    }

    private void updatePlaceholders(String type) {
        String detailPrompt = "请填写相关细节（如：请假时长4小时 / 报销金额300元）";
        if (type != null) {
            if (type.contains("请假")) {
                detailPrompt = "请假时长（例如：8小时 或 2天）";
            } else if (type.contains("报销")) {
                detailPrompt = "报销金额（例如：300.50元）";
            }
        }
        relatedDetailField.setPromptText(detailPrompt);
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