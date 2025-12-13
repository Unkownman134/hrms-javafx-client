package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.service.EmployeeService;
import com.gd.hrmsjavafxclient.service.EmployeeServiceImpl; // 🌟 导入实现类
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
 * 🌟 修正：实例化 EmployeeServiceImpl，并在 API 调用时使用 EmpID。
 */
public class AttendanceRecordController implements EmployeeSubController {

    @FXML private ComboBox<String> monthComboBox;
    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, LocalDate> dateCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> clockInCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> clockOutCol;
    @FXML private TableColumn<AttendanceRecord, String> statusCol;
    @FXML private TableColumn<AttendanceRecord, String> noteCol;
    @FXML private Button queryButton;

    // --- 数据和状态 ---
    // 🌟 修正：直接实例化实现类
    private final EmployeeService employeeService = new EmployeeServiceImpl();
    private CurrentUserInfo currentUser;
    private String authToken;
    private final ObservableList<AttendanceRecord> data = FXCollections.observableArrayList();

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @Override
    public void initializeController() {
        // ... (省略 ComboBox 和 TableColumn 初始化，与上文相同)
        List<String> months = IntStream.range(0, 12)
                .mapToObj(i -> YearMonth.now().minusMonths(i))
                .map(ym -> ym.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .collect(Collectors.toList());
        monthComboBox.setItems(FXCollections.observableArrayList(months));
        monthComboBox.getSelectionModel().selectFirst();

        dateCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        clockInCol.setCellValueFactory(cellData -> cellData.getValue().clockInTimeProperty());
        clockOutCol.setCellValueFactory(cellData -> cellData.getValue().clockOutTimeProperty());
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        noteCol.setCellValueFactory(cellData -> cellData.getValue().noteProperty());

        attendanceTable.setItems(data);

        handleQueryButtonAction(null);
    }

    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        String selectedMonthText = monthComboBox.getSelectionModel().getSelectedItem();
        if (selectedMonthText == null || currentUser == null || authToken == null || currentUser.getEmpId() == null) {
            showAlert("提示", "请选择月份或等待用户信息加载。", Alert.AlertType.WARNING);
            return;
        }

        queryButton.setDisable(true);
        queryButton.setText("加载中...");

        Task<List<AttendanceRecord>> loadTask = new Task<>() {
            @Override
            protected List<AttendanceRecord> call() throws Exception {
                // 🌟 修正点：使用 currentUser.getEmpId() 进行 API 调用
                return employeeService.getAttendanceRecords(currentUser.getEmpId(), selectedMonthText, authToken);
            }

            @Override
            protected void succeeded() {
                // ... (省略成功逻辑，与上文相同)
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    if (data.isEmpty()) {
                        showAlert("提示", selectedMonthText + " 暂时没有考勤记录呢。", Alert.AlertType.INFORMATION);
                    }
                });
            }

            @Override
            protected void failed() {
                // ... (省略失败逻辑，与上文相同)
                Platform.runLater(() -> {
                    showAlert("错误 ❌", "加载考勤记录失败：" + getException().getMessage(), Alert.AlertType.ERROR);
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