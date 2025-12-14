package com.gd.hrmsjavafxclient.controller.manager;

import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Schedule; // 排班 Model
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.service.manager.ScheduleManagerService;
import com.gd.hrmsjavafxclient.service.manager.EmployeeManagerService;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Map;

/**
 * 部门排班查询视图控制器 (t_employee_schedule)
 * 🌟 修正：为 LocalDate 和 LocalTime 类型的列添加了 CellFactory 以确保显示。
 * 🚨 新增：在 succeeded() 中添加了调试打印。
 */
public class DeptScheduleController implements ManagerSubController {

    @FXML private ComboBox<String> monthComboBox;
    @FXML private Button queryButton;
    @FXML private TableView<Schedule> scheduleTable;
    @FXML private Label deptNameLabel;

    // Table Columns (确保 fx:id 与 FXML 文件一致)
    @FXML private TableColumn<Schedule, String> employeeNameCol;
    @FXML private TableColumn<Schedule, LocalDate> dateCol;
    @FXML private TableColumn<Schedule, String> shiftNameCol;
    @FXML private TableColumn<Schedule, LocalTime> clockInTimeCol;
    @FXML private TableColumn<Schedule, LocalTime> clockOutTimeCol;
    @FXML private TableColumn<Schedule, String> noteCol;

    // --- 数据和状态 ---
    private final ObservableList<Schedule> data = FXCollections.observableArrayList();
    private final ScheduleManagerService scheduleManagerService = new ScheduleManagerService();
    private final EmployeeManagerService employeeManagerService = new EmployeeManagerService(); // 用于获取员工列表
    private Map<Integer, Employee> employeeMap; // 员工ID -> 员工对象 的映射表
    private String authToken;
    private CurrentUserInfo currentUserInfo;

    // ------------------------------------------------------------------
    // ManagerSubController 接口实现
    // ------------------------------------------------------------------
    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUserInfo = userInfo;
        this.authToken = authToken;
        this.deptNameLabel.setText("当前部门: " + userInfo.getDepartmentName());
        initializeMonthComboBox();
        loadInitialData();
    }

    // ------------------------------------------------------------------
    // 初始化逻辑 (修复显示问题的关键)
    // ------------------------------------------------------------------

    @FXML
    public void initialize() {
        // 初始化 TableView
        scheduleTable.setItems(data);
        scheduleTable.setPlaceholder(new Label("请选择月份，并点击查询按钮 🔍"));

        // 绑定列到 Schedule 对象的属性
        // 员工姓名 (特殊处理的绑定，已正常显示)
        employeeNameCol.setCellValueFactory(cellData -> {
            Integer empId = cellData.getValue().getEmpId();
            String name = employeeMap != null && employeeMap.containsKey(empId)
                    ? employeeMap.get(empId).getEmpName()
                    : "未知员工 (ID: " + empId + ")";
            return new SimpleStringProperty(name);
        });

        // ----------------------------------------------------
        // 🚨 修正 1: Date 列绑定和格式化 (LocalDate)
        // ----------------------------------------------------
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        // 添加 CellFactory 用于格式化 LocalDate (yyyy-MM-dd)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        dateCol.setCellFactory(column -> new TableCell<Schedule, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(item));
                }
            }
        });

        // 班次名称 (String)
        shiftNameCol.setCellValueFactory(new PropertyValueFactory<>("shiftName"));

        // ----------------------------------------------------
        // 🚨 修正 2: Time 列绑定和格式化 (LocalTime)
        // ----------------------------------------------------
        clockInTimeCol.setCellValueFactory(new PropertyValueFactory<>("clockInTime"));
        clockOutTimeCol.setCellValueFactory(new PropertyValueFactory<>("clockOutTime"));

        // 添加 CellFactory 用于格式化 LocalTime (HH:mm)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // 上班时间列 (LocalTime)
        clockInTimeCol.setCellFactory(column -> new TableCell<Schedule, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(timeFormatter.format(item));
                }
            }
        });

        // 下班时间列 (LocalTime)
        clockOutTimeCol.setCellFactory(column -> new TableCell<Schedule, LocalTime>() {
            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(timeFormatter.format(item));
                }
            }
        });

        // 备注 (String)
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
    }

    private void initializeMonthComboBox() {
        // 填充近 6 个月到 ComboBox
        YearMonth currentMonth = YearMonth.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        monthComboBox.getItems().clear();

        IntStream.range(0, 6).mapToObj(currentMonth::minusMonths)
                .map(formatter::format)
                .forEach(monthComboBox.getItems()::add);

        // 默认选中当前月
        monthComboBox.getSelectionModel().selectFirst();
    }

    // ------------------------------------------------------------------
    // 数据加载逻辑
    // ------------------------------------------------------------------

    private void loadInitialData() {
        // 第一次加载时，需要先加载员工列表
        Task<Map<Integer, Employee>> loadEmployeeTask = new Task<>() {
            @Override
            protected Map<Integer, Employee> call() throws Exception {
                // 1. 获取所有员工信息
                List<Employee> allEmployees = employeeManagerService.getAllEmployees(authToken);
                // 2. 客户端过滤出本部门员工
                Integer deptId = currentUserInfo.getDeptId();
                if (deptId == null) {
                    throw new IllegalStateException("用户部门ID缺失，无法查询部门员工。");
                }
                List<Employee> deptEmployees = allEmployees.stream()
                        .filter(e -> deptId.equals(e.getDeptId()))
                        .collect(Collectors.toList());

                // 3. 转化为 Map 供查询
                return deptEmployees.stream().collect(Collectors.toMap(Employee::getEmpId, e -> e));
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    employeeMap = getValue();
                    handleQueryButtonAction(null); // 员工加载成功后，自动执行一次查询
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("初始化失败 ❌", "加载部门员工信息失败：\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    scheduleTable.setPlaceholder(new Label("初始化失败，请检查网络或权限。"));
                    getException().printStackTrace();
                });
            }
        };
        new Thread(loadEmployeeTask).start();
    }


    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        if (employeeMap == null || employeeMap.isEmpty()) {
            showAlert("提示", "未找到本部门员工信息，无法查询排班记录。", Alert.AlertType.WARNING);
            return;
        }

        String selectedMonthText = monthComboBox.getSelectionModel().getSelectedItem();
        if (selectedMonthText == null) {
            showAlert("提示", "请先选择一个月份。", Alert.AlertType.WARNING);
            return;
        }

        queryButton.setDisable(true);
        queryButton.setText("查询中...");
        scheduleTable.setPlaceholder(new Label("正在加载 " + selectedMonthText + " 的排班记录... ⏳"));


        Task<List<Schedule>> loadTask = new Task<>() {
            @Override
            protected List<Schedule> call() throws Exception {
                YearMonth yearMonth = YearMonth.parse(selectedMonthText, DateTimeFormatter.ofPattern("yyyy-MM"));
                // 获取该月的第一天和最后一天
                String startDate = yearMonth.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
                String endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE);

                // 获取本部门所有员工 ID
                List<Integer> empIds = employeeMap.keySet().stream().collect(Collectors.toList());

                // 调用 API 获取排班记录
                List<Schedule> schedules = scheduleManagerService.getSchedulesByEmpIdsAndDateRange(empIds, startDate, endDate, authToken);

                // 对结果进行员工姓名填充 (在客户端完成)
                for (Schedule s : schedules) {
                    if (employeeMap.containsKey(s.getEmpId())) {
                        s.setEmployeeName(employeeMap.get(s.getEmpId()).getEmpName());
                    } else {
                        s.setEmployeeName("未知员工 (ID:" + s.getEmpId() + ")");
                    }
                }

                return schedules;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);

                    // 🚨 调试打印 🚨
                    System.out.println("==============================================");
                    System.out.println("📊 TableView 数据设置完成，最终条目数: " + data.size());
                    if (!data.isEmpty()) {
                        Schedule firstSchedule = data.get(0);
                        System.out.println("📋 第一条排班记录数据检查:");
                        System.out.println(" - 员工ID: " + firstSchedule.getEmpId());
                        System.out.println(" - 员工姓名: " + firstSchedule.getEmployeeName());
                        System.out.println(" - 日期: " + firstSchedule.getDate());
                        System.out.println(" - 班次名称: " + firstSchedule.getShiftName());
                        System.out.println(" - 上班时间: " + firstSchedule.getClockInTime());
                        System.out.println(" - 下班时间: " + firstSchedule.getClockOutTime());
                        System.out.println(" - 备注: " + firstSchedule.getNote());
                    }
                    System.out.println("==============================================");

                    if (data.isEmpty()) {
                        scheduleTable.setPlaceholder(new Label(selectedMonthText + " 暂时没有排班记录呢。"));
                    } else {
                        scheduleTable.setPlaceholder(new Label("没有数据。")); // 默认占位符
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    // 捕捉到 403 错误会在这里抛出 RuntimeException
                    String errorMessage = getException().getMessage();

                    // 明确提示 403 错误是权限问题
                    if (errorMessage != null && errorMessage.contains("状态码: 403")) {
                        showAlert("权限不足 🚫", "加载排班记录失败：\nAPI 访问被拒绝 (403 Forbidden)。\n请联系管理员确认您的 [部门经理] 角色是否拥有 /api/schedules/filter 的访问权限！", Alert.AlertType.ERROR);
                        scheduleTable.setPlaceholder(new Label("权限不足 (403) 🚫"));
                    } else {
                        showAlert("错误 ❌", "加载排班记录失败：\n" + errorMessage, Alert.AlertType.ERROR);
                        scheduleTable.setPlaceholder(new Label("加载失败 ❌"));
                    }

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