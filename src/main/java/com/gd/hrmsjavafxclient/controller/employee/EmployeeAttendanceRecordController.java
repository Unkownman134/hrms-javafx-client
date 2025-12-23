package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.service.employee.AttendanceEmpService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 考勤记录视图控制器 - 优化版 (强制显示年月格式)
 */
public class EmployeeAttendanceRecordController implements EmployeeSubController {

    @FXML private DatePicker monthPicker;
    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private Button queryButton;

    @FXML private TableColumn<AttendanceRecord, LocalDate> dateCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> clockInCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> clockOutCol;
    @FXML private TableColumn<AttendanceRecord, String> statusCol;
    @FXML private TableColumn<AttendanceRecord, String> noteCol;

    private final ObservableList<AttendanceRecord> data = FXCollections.observableArrayList();
    private CurrentUserInfo currentUser;
    private String authToken;
    private final AttendanceEmpService attendanceEmpService = new AttendanceEmpService();

    // 🌟 定义年月格式化器
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
        initializeController();
    }

    @Override
    public void initializeController() {
        Platform.runLater(this::initialize);
    }

    private void initialize() {
        // 1. 绑定表格列
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        clockInCol.setCellValueFactory(new PropertyValueFactory<>("clockInTime"));
        clockOutCol.setCellValueFactory(new PropertyValueFactory<>("clockOutTime"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        attendanceTable.setItems(data);
        attendanceTable.setPlaceholder(new Label("正在努力加载数据中..."));

        // 2. 🌟 设置 DatePicker 仅显示年月
        setupMonthPicker();

        // 3. 进入界面直接自动查一遍！
        loadAttendanceData();
    }

    /**
     * 🌟 核心改动：自定义 DatePicker 的显示格式
     */
    private void setupMonthPicker() {
        monthPicker.setValue(LocalDate.now());

        monthPicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return monthFormatter.format(date);
                }
                return "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    // 解析时默认补上 1 号，因为 LocalDate 必须有日
                    return LocalDate.parse(string + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                return null;
            }
        });

        // 提示用户只能选年月
        monthPicker.setPromptText("yyyy-MM");
    }

    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        loadAttendanceData();
    }

    private void loadAttendanceData() {
        LocalDate selectedDate = monthPicker.getValue();
        if (selectedDate == null) {
            showAlert("提示 💡", "请先选择查询月份！", Alert.AlertType.WARNING);
            return;
        }

        YearMonth targetYearMonth = YearMonth.from(selectedDate);
        queryButton.setDisable(true);
        queryButton.setText("查询中...");
        data.clear();

        Task<List<AttendanceRecord>> loadTask = new Task<>() {
            @Override
            protected List<AttendanceRecord> call() throws Exception {
                return attendanceEmpService.getAttendanceRecords(
                        currentUser.getEmpId(), null, authToken
                );
            }

            @Override
            protected void succeeded() {
                List<AttendanceRecord> allRecords = getValue();
                List<AttendanceRecord> filtered = allRecords.stream()
                        .filter(r -> r.getDate() != null && YearMonth.from(r.getDate()).equals(targetYearMonth))
                        .collect(Collectors.toList());

                Platform.runLater(() -> {
                    data.setAll(filtered);
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    if (data.isEmpty()) {
                        attendanceTable.setPlaceholder(new Label(targetYearMonth + " 没有找到记录~"));
                    }
                });
            }

            @Override
            protected void failed() {
                Throwable e = getException();
                Platform.runLater(() -> {
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    attendanceTable.setPlaceholder(new Label("加载失败 ❌"));
                    showAlert("加载失败", "错误：" + e.getMessage(), Alert.AlertType.ERROR);
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