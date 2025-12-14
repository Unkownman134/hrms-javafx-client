package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
// ❌ 移除旧的 Service 引用
// import com.gd.hrmsjavafxclient.service.EmployeeService;
// import com.gd.hrmsjavafxclient.service.EmployeeServiceImpl;

// ✅ 导入新的 AttendanceEmpService
import com.gd.hrmsjavafxclient.service.employee.AttendanceEmpService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 考勤记录视图控制器 (对应 AttendanceRecordView.fxml)
 * 🌟 修正：实例化 AttendanceEmpService，并在 API 调用时使用 EmpID。
 */
public class EmployeeAttendanceRecordController implements EmployeeSubController {

    @FXML private ComboBox<String> monthComboBox;
    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, LocalDate> dateCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> clockInCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> clockOutCol;
    @FXML private TableColumn<AttendanceRecord, String> statusCol;
    @FXML private Button queryButton;

    // --- 数据和状态 ---
    // 🌟 修正：直接实例化 AttendanceEmpService
    private final AttendanceEmpService attendanceEmpService = new AttendanceEmpService();
    private CurrentUserInfo currentUser;
    private String authToken;
    private final ObservableList<AttendanceRecord> data = FXCollections.observableArrayList();

    // --- 初始化和数据设置 ---

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @Override
    public void initializeController() {
        // 初始化月份下拉框，从当前月开始向前推 12 个月
        List<String> months = IntStream.range(0, 12)
                .mapToObj(i -> YearMonth.now().minusMonths(i))
                .map(ym -> ym.format(DateTimeFormatter.ofPattern("yyyy年MM月")))
                .collect(Collectors.toList());
        monthComboBox.setItems(FXCollections.observableArrayList(months));

        // 默认选择当前月
        monthComboBox.getSelectionModel().selectFirst();

        // 绑定 TableView
        attendanceTable.setItems(data);
        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        clockInCol.setCellValueFactory(cellData -> cellData.getValue().clockInTimeProperty());
        clockOutCol.setCellValueFactory(cellData -> cellData.getValue().clockOutTimeProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // 默认加载当前月份数据
        handleQueryButtonAction(null);
    }

    // --- 查询方法 ---

    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        String selectedMonthDisplay = monthComboBox.getSelectionModel().getSelectedItem();
        if (selectedMonthDisplay == null) {
            showAlert("提示", "请选择要查询的月份哦。", Alert.AlertType.WARNING);
            return;
        }

        // 提取 API 所需的 YYYY-MM 格式
        String selectedMonthText = selectedMonthDisplay.substring(0, 4) + "-" + selectedMonthDisplay.substring(5, 7);

        queryButton.setDisable(true);
        queryButton.setText("查询中...");

        Task<List<AttendanceRecord>> loadTask = new Task<>() {
            @Override
            protected List<AttendanceRecord> call() throws Exception {
                if (currentUser.getEmpId() == null) {
                    throw new IllegalStateException("员工ID缺失，无法查询记录！");
                }
                // 🌟 调用新的 AttendanceEmpService 方法
                return attendanceEmpService.getAttendanceRecords(
                        currentUser.getEmpId(), selectedMonthText, authToken
                );
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    if (data.isEmpty()) {
                        showAlert("提示", selectedMonthDisplay + " 暂时没有考勤记录呢。", Alert.AlertType.INFORMATION);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("错误 ❌", "加载考勤记录失败：\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    getException().printStackTrace();
                });
            }
        };

        new Thread(loadTask).start();
    }

    @FXML
    private void handleExportButtonAction() {
        showAlert("提示", "导出记录功能正在开发中哦！🚀", Alert.AlertType.INFORMATION);
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