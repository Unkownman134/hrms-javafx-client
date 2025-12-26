package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.service.manager.AttendanceManagerService;
import com.gd.hrmsjavafxclient.service.manager.EmployeeManagerService;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门考勤记录视图控制器
 * 🌟 最终进化版：
 * 1. 使用 DatePicker 并通过 StringConverter 锁定“年-月”显示格式。
 * 2. 彻底移除导出功能，界面清爽 100%。
 * 3. 逻辑依然保持严谨的客户端聚合查询。
 */
public class DeptAttendanceController implements ManagerSubController {

    @FXML private Label deptNameLabel;
    @FXML private DatePicker monthDatePicker;
    @FXML private Button queryButton;

    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, Integer> empIdCol;
    @FXML private TableColumn<AttendanceRecord, String> nameCol;
    @FXML private TableColumn<AttendanceRecord, LocalDate> dateCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> checkInTimeCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> checkOutTimeCol;
    @FXML private TableColumn<AttendanceRecord, String> statusCol;
    @FXML private TableColumn<AttendanceRecord, String> noteCol;

    private String authToken;
    private CurrentUserInfo currentUser;
    private final AttendanceManagerService attendanceService = new AttendanceManagerService();
    private final EmployeeManagerService employeeService = new EmployeeManagerService();

    private final ObservableList<AttendanceRecord> attendanceData = FXCollections.observableArrayList();

    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;

        Platform.runLater(() -> {
            if (userInfo != null && userInfo.getDepartmentName() != null) {
                deptNameLabel.setText("当前部门: " + userInfo.getDepartmentName());
            }
            initTable();
            initDatePicker();
        });
    }

    /**
     * 初始化表格列绑定
     */
    private void initTable() {
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        checkInTimeCol.setCellValueFactory(new PropertyValueFactory<>("clockInTime"));
        checkOutTimeCol.setCellValueFactory(new PropertyValueFactory<>("clockOutTime"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        attendanceTable.setItems(attendanceData);
    }

    /**
     * 配置 DatePicker ，让它只显示年月
     */
    private void initDatePicker() {
        monthDatePicker.setValue(LocalDate.now());

        monthDatePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月");

            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.isEmpty()) return null;
                return monthDatePicker.getValue();
            }
        });

        monthDatePicker.getEditor().setEditable(false);
    }

    /**
     * 查询按钮逻辑
     */
    @FXML
    private void handleQueryAttendance(ActionEvent event) {
        LocalDate selectedDate = monthDatePicker.getValue();
        if (selectedDate == null) {
            showAlert("提示", "请选择要查询的月份！", Alert.AlertType.WARNING);
            return;
        }

        YearMonth targetMonth = YearMonth.from(selectedDate);

        queryButton.setDisable(true);
        queryButton.setText("查询中...");
        attendanceData.clear();
        attendanceTable.setPlaceholder(new ProgressIndicator());

        Task<List<AttendanceRecord>> loadTask = new Task<>() {
            @Override
            protected List<AttendanceRecord> call() throws Exception {
                List<Employee> allEmployees = employeeService.getAllEmployees(authToken);
                List<Employee> deptEmps = allEmployees.stream()
                        .filter(e -> e.getDeptId() != null && e.getDeptId().equals(currentUser.getDeptId()))
                        .collect(Collectors.toList());

                Map<Integer, String> empNameMap = deptEmps.stream()
                        .collect(Collectors.toMap(Employee::getEmpId, Employee::getEmpName, (v1, v2) -> v1));

                List<AttendanceRecord> results = new ArrayList<>();

                for (Employee emp : deptEmps) {
                    if (isCancelled()) break;
                    List<AttendanceRecord> empRecords = attendanceService.getAttendanceRecordsByEmpId(emp.getEmpId(), authToken);

                    List<AttendanceRecord> filtered = empRecords.stream()
                            .filter(r -> r.getDate() != null && YearMonth.from(r.getDate()).equals(targetMonth))
                            .peek(r -> r.setEmployeeName(empNameMap.get(r.getEmpId())))
                            .collect(Collectors.toList());

                    results.addAll(filtered);
                }
                return results;
            }

            @Override
            protected void succeeded() {
                attendanceData.setAll(getValue());
                resetQueryButton();
                if (attendanceData.isEmpty()) {
                    attendanceTable.setPlaceholder(new Label(targetMonth.toString() + " 暂无记录数据。"));
                }
            }

            @Override
            protected void failed() {
                resetQueryButton();
                attendanceTable.setPlaceholder(new Label("加载失败"));
                showAlert("错误", "获取考勤数据时崩溃了：" + getException().getMessage(), Alert.AlertType.ERROR);
            }

            private void resetQueryButton() {
                queryButton.setDisable(false);
                queryButton.setText("查 询");
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