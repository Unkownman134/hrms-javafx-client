package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.ApprovalRequest;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
// ✅ 导入新的 ApplicationEmpService，专门负责提交申请！
import com.gd.hrmsjavafxclient.service.ApplicationEmpService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 我的申请视图控制器 (对应 EmployeeApplicationView.fxml)
 * 🌟 修正：实例化 ApplicationEmpService，并在提交申请时使用 EmpID。
 */
public class EmployeeApplicationController implements EmployeeSubController {

    @FXML private TextField applicantNameField;
    @FXML private ComboBox<String> applicationTypeComboBox;
    @FXML private DatePicker relatedDateField;
    @FXML private TextField relatedDetailField;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button submitButton;

    // --- 数据和状态 ---
    // 实例化专门的申请服务
    private final ApplicationEmpService applicationEmpService = new ApplicationEmpService();
    private CurrentUserInfo currentUser;
    private String authToken;

    // 申请类型列表
    private final List<String> APPLICATION_TYPES = Arrays.asList("请假申请", "加班申请", "报销申请", "调岗申请", "其他");

    // --- 接口实现 ---

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @FXML
    public void initialize() {
        // 初始化 ComboBox
        applicationTypeComboBox.setItems(FXCollections.observableArrayList(APPLICATION_TYPES));

        // 绑定 ComboBox 监听器，用于更新提示文本（UX 优化！）
        applicationTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updatePlaceholders(newVal);
        });

        // 确保在 JavaFX 线程中初始化控制器数据
        Platform.runLater(this::initializeController);
    }

    @Override
    public void initializeController() {
        if (currentUser != null) {
            Platform.runLater(() -> {
                // 设置申请人姓名（只读）
                applicantNameField.setText(currentUser.getEmployeeName());
                // 设置默认的提示文本
                updatePlaceholders(null);
                // 默认日期设置为今天
                relatedDateField.setValue(LocalDate.now());
            });
        }
    }

    // --- 事件处理：提交申请 (R7) ---

    @FXML
    private void handleSubmitButtonAction(ActionEvent event) {
        // 1. 输入验证
        if (!validateInput()) {
            return;
        }

        // 禁用按钮并显示加载中
        submitButton.setText("提交中...");
        submitButton.setDisable(true);

        // 2. 构造请求对象
        ApprovalRequest request = new ApprovalRequest();
        request.setApplicationType(applicationTypeComboBox.getValue());
        request.setRelatedDate(relatedDateField.getValue());
        request.setRelatedDetail(relatedDetailField.getText().trim());
        request.setDescription(descriptionTextArea.getText().trim());
        // 🌟 关键：设置申请人的 EmpID
        request.setApplicantId(currentUser.getEmpId());

        // 3. 使用 Task 进行异步提交
        Task<Boolean> submitTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                // 调用服务层 API
                return applicationEmpService.submitApplication(request, authToken);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    boolean success = getValue();
                    if (success) {
                        showAlert("提交成功 🎉", "您的申请已提交，等待审批。请关注后续状态。", Alert.AlertType.INFORMATION);
                        clearForm();
                    } else {
                        // 理论上 ServiceUtil 应该抛异常，这里是处理服务器返回的失败情况
                        showAlert("提交失败 💔", "服务器返回处理失败，请稍后再试。", Alert.AlertType.ERROR);
                    }
                    submitButton.setText("提 交 申 请");
                    submitButton.setDisable(false);
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("提交失败 ❌", "申请提交过程中发生错误：" + getException().getMessage(), Alert.AlertType.ERROR);
                    submitButton.setText("提 交 申 请");
                    submitButton.setDisable(false);
                    getException().printStackTrace();
                });
            }
        };
        new Thread(submitTask).start();
    }

    /**
     * R7 员工申请历史记录查询功能 (占位符)
     */
    @FXML
    private void handleViewHistoryButtonAction(ActionEvent event) {
        showAlert("提示", "查看历史申请记录功能正在开发中哦！🏗️", Alert.AlertType.INFORMATION);
    }

    // --- 辅助方法 ---

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
            relatedDateField.setValue(LocalDate.now()); // 重置为今天
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