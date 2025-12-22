package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.ApprovalRequest;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.service.employee.ApplicationEmpService;
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
 * 业务申请控制器 - 已修正方法访问权限
 */
public class EmployeeApplicationController implements EmployeeSubController {

    @FXML private TextField applicantNameField;
    @FXML private ComboBox<String> applicationTypeComboBox;
    @FXML private DatePicker relatedDateField;
    @FXML private TextField relatedDetailField;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button submitButton;

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
        List<String> types = Arrays.asList("请假申请", "加班申请", "报销申请", "调岗申请", "离职申请");
        applicationTypeComboBox.setItems(FXCollections.observableArrayList(types));
        relatedDateField.setValue(LocalDate.now());

        if (currentUser != null) {
            // 对应 Employee model 中的 empName 逻辑
            applicantNameField.setText(currentUser.getEmployeeName());
        }
    }

    @FXML
    public void handleSubmitButtonAction(ActionEvent event) {
        if (!validateInput()) return;

        submitButton.setDisable(true);

        ApprovalRequest request = new ApprovalRequest();
        request.setApplicantId(currentUser.getEmpId());
        request.setApplicationType(applicationTypeComboBox.getValue());
        request.setRelatedDate(relatedDateField.getValue());
        request.setRelatedDetail(relatedDetailField.getText().trim());
        request.setDescription(descriptionTextArea.getText().trim());
        request.setStatus("PENDING");

        Task<Boolean> task = new Task<>() {
            @Override protected Boolean call() throws Exception {
                return applicationEmpService.submitApplication(request, authToken);
            }
            @Override protected void succeeded() {
                if (getValue()) {
                    showAlert("成功", "申请已提交！", Alert.AlertType.INFORMATION);
                    clearForm();
                } else {
                    showAlert("错误", "提交失败，请重试。", Alert.AlertType.ERROR);
                }
                submitButton.setDisable(false);
            }
            @Override protected void failed() {
                showAlert("错误", "系统异常", Alert.AlertType.ERROR);
                submitButton.setDisable(false);
            }
        };
        new Thread(task).start();
    }

    @FXML
    public void clearForm() {
        Platform.runLater(() -> {
            applicationTypeComboBox.getSelectionModel().clearSelection();
            relatedDateField.setValue(LocalDate.now());
            relatedDetailField.clear();
            descriptionTextArea.clear();
        });
    }

    private boolean validateInput() {
        if (applicationTypeComboBox.getValue() == null ||
                relatedDetailField.getText().trim().isEmpty() ||
                descriptionTextArea.getText().trim().isEmpty()) {
            showAlert("提示", "请完整填写申请信息内容！", Alert.AlertType.WARNING);
            return false;
        }
        return true;
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