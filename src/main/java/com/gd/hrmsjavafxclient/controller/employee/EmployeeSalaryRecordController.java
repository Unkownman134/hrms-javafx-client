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


public class EmployeeSalaryRecordController implements EmployeeSubController {

    @FXML private DatePicker yearDatePicker;
    @FXML private TableView<SalaryRecord> salaryRecordTable;
    @FXML private TableColumn<SalaryRecord, String> monthCol;
    @FXML private TableColumn<SalaryRecord, LocalDate> payDateCol;
    @FXML private TableColumn<SalaryRecord, BigDecimal> grossPayCol;
    @FXML private TableColumn<SalaryRecord, BigDecimal> netPayCol;
    @FXML private Button queryButton;

    private final SalaryEmpService salaryEmpService = new SalaryEmpService();
    private CurrentUserInfo currentUser;
    private String authToken;
    private final ObservableList<SalaryRecord> data = FXCollections.observableArrayList();


    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @Override
    public void initializeController() {
        yearDatePicker.setValue(LocalDate.now());

        salaryRecordTable.setItems(data);
        monthCol.setCellValueFactory(cellData -> cellData.getValue().salaryMonthProperty());
        payDateCol.setCellValueFactory(cellData -> cellData.getValue().payDateProperty());
        grossPayCol.setCellValueFactory(cellData -> cellData.getValue().grossPayProperty());
        netPayCol.setCellValueFactory(cellData -> cellData.getValue().netPayProperty());

        handleQueryButtonAction(null);
    }


    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        LocalDate selectedDate = yearDatePicker.getValue();
        if (selectedDate == null) {
            showAlert("提示", "请在日历中选择一个日期来确定年份。", Alert.AlertType.WARNING);
            return;
        }

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
                        showAlert("提示", selectedYear + " 年暂时没有工资记录。", Alert.AlertType.INFORMATION);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("错误", "加载工资记录失败：\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
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