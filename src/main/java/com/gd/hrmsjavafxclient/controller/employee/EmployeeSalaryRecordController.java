package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.service.employee.SalaryEmpService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 工资条视图控制器 (对应 SalaryRecordView.fxml)
 * 🌟 修改点：使用 DatePicker 筛选年份，移除明细按钮逻辑。
 */
public class EmployeeSalaryRecordController implements EmployeeSubController {

    @FXML private DatePicker yearDatePicker; // 修改为 DatePicker
    @FXML private TableView<SalaryRecord> salaryRecordTable;
    @FXML private TableColumn<SalaryRecord, String> monthCol;
    @FXML private TableColumn<SalaryRecord, LocalDate> payDateCol;
    @FXML private TableColumn<SalaryRecord, BigDecimal> grossPayCol;
    @FXML private TableColumn<SalaryRecord, BigDecimal> netPayCol;
    @FXML private Button queryButton;

    // --- 数据和状态 ---
    private final SalaryEmpService salaryEmpService = new SalaryEmpService();
    private CurrentUserInfo currentUser;
    private String authToken;
    private final ObservableList<SalaryRecord> data = FXCollections.observableArrayList();

    // --- 初始化和数据设置 ---

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @Override
    public void initializeController() {
        // 默认设置为今天
        yearDatePicker.setValue(LocalDate.now());

        // 绑定 TableView 列
        salaryRecordTable.setItems(data);
        monthCol.setCellValueFactory(cellData -> cellData.getValue().salaryMonthProperty());
        payDateCol.setCellValueFactory(cellData -> cellData.getValue().payDateProperty());
        grossPayCol.setCellValueFactory(cellData -> cellData.getValue().grossPayProperty());
        netPayCol.setCellValueFactory(cellData -> cellData.getValue().netPayProperty());

        // 默认加载当前日期所属年份的数据
        handleQueryButtonAction(null);
    }

    // --- 查询方法 ---

    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        LocalDate selectedDate = yearDatePicker.getValue();
        if (selectedDate == null) {
            showAlert("提示", "请在日历中选择一个日期来确定年份哦。✨", Alert.AlertType.WARNING);
            return;
        }

        // 从选中的日期中提取年份
        int selectedYear = selectedDate.getYear();

        queryButton.setDisable(true);
        queryButton.setText("查询中...");

        Task<List<SalaryRecord>> loadTask = new Task<>() {
            @Override
            protected List<SalaryRecord> call() throws Exception {
                if (currentUser.getEmpId() == null) {
                    throw new IllegalStateException("员工ID缺失，无法查询记录！");
                }
                return salaryEmpService.getSalaryRecords(
                        currentUser.getEmpId(), selectedYear, authToken
                );
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    if (data.isEmpty()) {
                        showAlert("提示", selectedYear + " 年暂时没有工资记录呢。☕", Alert.AlertType.INFORMATION);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("错误 ❌", "加载工资记录失败：\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    getException().printStackTrace();
                });
            }
        };

        new Thread(loadTask).start();
    }

    // --- 辅助方法 ---

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