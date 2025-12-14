package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.AttendanceRecord;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.service.AttendanceManagerService;
import com.gd.hrmsjavafxclient.service.EmployeeManagerService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 部门考勤记录视图控制器 (t_attendance_record)
 * 🌟 修正：通过客户端聚合 (Client Aggregation) 的方式实现部门考勤查询功能，并统一了上下文接收方法。
 * 🚨 修正：更新 TableColumn 绑定属性，修复 clockOutTime 命名错误。
 */
public class DeptAttendanceController implements ManagerSubController {

    @FXML private ComboBox<String> monthComboBox;
    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, Integer> empIdCol;
    @FXML private TableColumn<AttendanceRecord, String> nameCol;
    @FXML private TableColumn<AttendanceRecord, LocalDate> dateCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> checkInTimeCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> checkOutTimeCol;
    @FXML private TableColumn<AttendanceRecord, String> statusCol;
    @FXML private TableColumn<AttendanceRecord, String> noteCol;
    @FXML private Button queryButton;
    @FXML private Label deptNameLabel;

    // --- 数据和状态 ---
    private final ObservableList<AttendanceRecord> data = FXCollections.observableArrayList();
    private final AttendanceManagerService attendanceManagerService = new AttendanceManagerService();
    private final EmployeeManagerService employeeManagerService = new EmployeeManagerService();

    private CurrentUserInfo currentUserInfo;
    private String authToken;
    private Map<Integer, Employee> departmentEmployeeMap;

    @FXML
    public void initialize() {
        // 初始化 ComboBox：填充最近 12 个月
        YearMonth currentMonth = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        List<String> months = IntStream.range(0, 12)
                .mapToObj(currentMonth::minusMonths)
                .map(formatter::format)
                .collect(Collectors.toList());
        monthComboBox.setItems(FXCollections.observableArrayList(months));
        monthComboBox.getSelectionModel().selectFirst(); // 默认选择本月

        // 初始化 TableView 列绑定
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("employeeName"));

        // 绑定 Model 中的 Property
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        checkInTimeCol.setCellValueFactory(new PropertyValueFactory<>("clockInTime"));
        // 🌟 修正点：将错误的 "clockOutOutTime" 修正为正确的 "clockOutTime"
        checkOutTimeCol.setCellValueFactory(new PropertyValueFactory<>("clockOutTime"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        // 设置日期/时间列的格式（省略格式化代码，与上次提供的一致）
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        dateCol.setCellFactory(column -> new TableCell<AttendanceRecord, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : dateFormatter.format(item));
            }
        });
        checkInTimeCol.setCellFactory(column -> new TableCell<AttendanceRecord, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : timeFormatter.format(item));
            }
        });
        checkOutTimeCol.setCellFactory(column -> new TableCell<AttendanceRecord, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : timeFormatter.format(item));
            }
        });

        attendanceTable.setItems(data);
    }

    // ... (其他方法如 setManagerContext, loadDepartmentEmployeesAndInitQuery, handleQueryAttendance, handleExportButtonAction, showAlert 保持不变，请沿用上一轮的带调试信息的版本)
    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUserInfo = userInfo;
        this.authToken = authToken;
        Platform.runLater(this::loadDepartmentEmployeesAndInitQuery);
    }

    private void loadDepartmentEmployeesAndInitQuery() {
        if (currentUserInfo == null || authToken == null || currentUserInfo.getDeptId() == null) {
            showAlert("错误 ❌", "用户、认证信息或部门ID丢失，无法加载数据。", Alert.AlertType.ERROR);
            return;
        }

        Integer deptId = currentUserInfo.getDeptId();
        deptNameLabel.setText(currentUserInfo.getDepartmentName() + " 部门考勤记录");
        attendanceTable.setPlaceholder(new Label("正在加载部门员工列表... 🏃‍♀️"));

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<Employee> allEmployees = employeeManagerService.getAllEmployees(authToken);
                departmentEmployeeMap = allEmployees.stream()
                        .filter(e -> deptId.equals(e.getDeptId()))
                        .collect(Collectors.toMap(Employee::getEmpId, e -> e));

                Platform.runLater(() -> handleQueryAttendance(null));
                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    attendanceTable.setPlaceholder(new Label("初始化数据失败 😭: " + getException().getMessage()));
                    showAlert("错误 ❌", "初始化数据失败：\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    getException().printStackTrace();
                });
            }
        };
        new Thread(task).start();
    }


    /**
     * 🌟 修正并添加调试信息：查询考勤记录的事件处理方法
     * 请注意：此方法内容与上一轮提供的一致，包含调试信息。
     */
    @FXML
    public void handleQueryAttendance(ActionEvent event) {
        String selectedMonthText = monthComboBox.getSelectionModel().getSelectedItem();

        if (selectedMonthText == null || currentUserInfo == null || departmentEmployeeMap == null) return;

        System.out.println("--- 🔎 开始考勤查询调试 (北京时间 " + java.time.LocalDateTime.now() + ") ---");
        System.out.println("查询月份: " + selectedMonthText);

        List<Integer> deptEmpIds = new ArrayList<>(departmentEmployeeMap.keySet());
        YearMonth selectedYearMonth = YearMonth.parse(selectedMonthText, DateTimeFormatter.ofPattern("yyyy-MM"));

        queryButton.setDisable(true);
        queryButton.setText("查询中... 🔎");
        attendanceTable.setPlaceholder(new Label("正在查询 " + selectedMonthText + " 的考勤记录..."));

        Task<List<AttendanceRecord>> loadTask = new Task<>() {
            @Override
            protected List<AttendanceRecord> call() throws Exception {
                List<AttendanceRecord> aggregatedRecords = new ArrayList<>();
                int totalFetchedCount = 0;

                // 1. 遍历部门所有员工ID，逐个调用 API
                for (Integer empId : deptEmpIds) {
                    try {
                        List<AttendanceRecord> empRecords = attendanceManagerService.getAttendanceRecordsByEmpId(empId, authToken);

                        System.out.println(" -> 员工 " + empId + " (姓名: " + departmentEmployeeMap.get(empId).getEmpName() + ") 成功获取 " + empRecords.size() + " 条记录。");

                        empRecords.forEach(r -> {
                            r.setEmpId(empId);
                            // 调试：检查日期是否被正确解析
                            System.out.println("    [DEBUG] Record ID: " + r.getRecordId() + ", AttDate: " + r.getDate() + ", Status: " + r.getStatus());
                        });

                        aggregatedRecords.addAll(empRecords);
                        totalFetchedCount += empRecords.size();

                    } catch (IOException e) {
                        System.err.println("❌ API 错误：无法加载员工 ID: " + empId + " 的考勤记录：" + e.getMessage());
                        // 遇到单个员工的 API 错误，跳过该员工，继续查询下一个。
                    }
                }

                System.out.println("总共从 API 获取到的记录数 (聚合前): " + totalFetchedCount + " 条。");

                // 2. 客户端过滤：按选择的月份筛选数据
                List<AttendanceRecord> filteredRecords = aggregatedRecords.stream()
                        // r.getDate() 不为 null 且月份匹配
                        .filter(r -> r.getDate() != null && YearMonth.from(r.getDate()).equals(selectedYearMonth))
                        .collect(Collectors.toList());

                System.out.println("经过月份过滤后的记录数: " + filteredRecords.size() + " 条。");

                // 3. 客户端聚合：设置员工姓名到每个记录中
                for (AttendanceRecord record : filteredRecords) {
                    Employee emp = departmentEmployeeMap.get(record.getEmpId());
                    record.setEmployeeName(emp != null ? emp.getEmpName() : "N/A (ID: " + record.getEmpId() + ")");
                }

                return filteredRecords;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    if (data.isEmpty()) {
                        attendanceTable.setPlaceholder(new Label(selectedMonthText + " 暂时没有考勤记录呢。"));
                    }
                    System.out.println("✅ 考勤查询完成，表格显示 " + data.size() + " 条记录。");
                    System.out.println("--- 调试结束 ---");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    attendanceTable.setPlaceholder(new Label("加载考勤记录失败 ❌: " + getException().getMessage()));
                    showAlert("错误 ❌", "加载考勤记录失败：\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    getException().printStackTrace();
                    System.err.println("❌ 考勤查询失败，请检查 Service 或网络连接。");
                    System.out.println("--- 调试结束 ---");
                });
            }
        };

        new Thread(loadTask).start();
    }

    @FXML
    private void handleExportButtonAction(ActionEvent event) {
        showAlert("提示 💡", "导出记录功能尚未实现哦！", Alert.AlertType.INFORMATION);
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