package com.gd.hrmsjavafxclient.controller.manager;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gd.hrmsjavafxclient.controller.manager.ManagerMainController.ManagerSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Schedule;
import com.gd.hrmsjavafxclient.model.ShiftRule;
import com.gd.hrmsjavafxclient.service.manager.ScheduleManagerService;
import com.gd.hrmsjavafxclient.util.ServiceUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 部门经理排班界面控制器
 * 🌟 已修复 queryButton 为 null 的报错，并优化了员工遍历逻辑
 */
public class DeptScheduleController implements ManagerSubController {

    @FXML private Label deptNameLabel;
    @FXML private ComboBox<String> monthComboBox;
    @FXML private ComboBox<Employee> employeeComboBox;
    @FXML private ComboBox<ShiftRule> shiftRuleComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, String> employeeNameCol;
    @FXML private TableColumn<Schedule, LocalDate> dateCol;
    @FXML private TableColumn<Schedule, String> shiftNameCol;
    @FXML private TableColumn<Schedule, String> statusCol;

    private String authToken;
    private CurrentUserInfo currentUser;

    private final ScheduleManagerService scheduleService = new ScheduleManagerService();
    private final ObservableList<Schedule> scheduleData = FXCollections.observableArrayList();
    private final ObservableList<Employee> deptEmployees = FXCollections.observableArrayList();

    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;

        Platform.runLater(() -> {
            deptNameLabel.setText("当前部门: " + (userInfo.getDepartmentName() != null ? userInfo.getDepartmentName() : "未知"));
            initUI();
            loadInitialData(); // 加载员工和规则
        });
    }

    private void initUI() {
        // 月份初始化
        monthComboBox.setItems(FXCollections.observableArrayList("2025-11", "2025-12", "2026-01"));
        monthComboBox.setValue("2025-12");

        // 表格列绑定
        employeeNameCol.setCellValueFactory(d -> d.getValue().employeeNameProperty());
        dateCol.setCellValueFactory(d -> d.getValue().dateProperty());
        shiftNameCol.setCellValueFactory(d -> d.getValue().shiftNameProperty());
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());
        scheduleTable.setItems(scheduleData);

        // 员工 ComboBox 转换器
        employeeComboBox.setConverter(new StringConverter<Employee>() {
            @Override
            public String toString(Employee e) { return e == null ? "" : e.getEmpName() + " (ID:" + e.getEmpId() + ")"; }
            @Override
            public Employee fromString(String s) { return null; }
        });
        employeeComboBox.setItems(deptEmployees);

        // 班次 ComboBox 转换器
        shiftRuleComboBox.setConverter(new StringConverter<ShiftRule>() {
            @Override
            public String toString(ShiftRule r) { return r == null ? "" : r.getRuleName(); }
            @Override
            public ShiftRule fromString(String s) { return null; }
        });
    }

    /**
     * 加载元数据：获取所有员工后进行前端过滤
     */
    private void loadInitialData() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 1. 获取所有员工并过滤
                Optional<List<Employee>> allEmpsOpt = ServiceUtil.sendGet("/employees", authToken, new TypeReference<List<Employee>>() {});
                List<Employee> filtered = allEmpsOpt.orElse(new ArrayList<>()).stream()
                        .filter(e -> e.getDeptId() != null && e.getDeptId().equals(currentUser.getDeptId()))
                        .collect(Collectors.toList());

                // 2. 获取所有班次规则
                Optional<List<ShiftRule>> rulesOpt = ServiceUtil.sendGet("/shift/rules", authToken, new TypeReference<List<ShiftRule>>() {});
                List<ShiftRule> rules = rulesOpt.orElse(new ArrayList<>());

                Platform.runLater(() -> {
                    deptEmployees.setAll(filtered);
                    shiftRuleComboBox.setItems(FXCollections.observableArrayList(rules));
                    // 加载完员工后自动刷一次排班表
                    handleRefresh();
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    /**
     * 刷新逻辑：遍历本部门员工请求 API
     */
    @FXML
    private void handleRefresh() {
        String monthStr = monthComboBox.getValue();
        if (monthStr == null || deptEmployees.isEmpty()) return;

        YearMonth ym = YearMonth.parse(monthStr);
        String startDate = ym.atDay(1).toString();
        String endDate = ym.atEndOfMonth().toString();

        // 提示正在查询
        scheduleTable.setPlaceholder(new Label("正在同步部门排班数据，请稍候..."));

        Task<List<Schedule>> task = new Task<>() {
            @Override
            protected List<Schedule> call() throws Exception {
                List<Schedule> totalSchedules = new ArrayList<>();
                // 遍历每个员工 ID 进行 API 调用
                for (Employee emp : deptEmployees) {
                    try {
                        List<Schedule> res = scheduleService.getSchedulesByRange(emp.getEmpId(), startDate, endDate, authToken);
                        totalSchedules.addAll(res);
                    } catch (Exception e) {
                        System.err.println("❌ 获取员工 " + emp.getEmpName() + " 的排班失败: " + e.getMessage());
                    }
                }
                return totalSchedules;
            }

            @Override
            protected void succeeded() {
                scheduleData.setAll(getValue());
                if (scheduleData.isEmpty()) {
                    scheduleTable.setPlaceholder(new Label(monthStr + " 暂无任何排班记录。"));
                }
            }

            @Override
            protected void failed() {
                showAlert("刷新失败", "无法获取排班数据：" + getException().getMessage(), Alert.AlertType.ERROR);
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleBatchAdd() {
        Employee selectedEmp = employeeComboBox.getValue();
        ShiftRule rule = shiftRuleComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (selectedEmp == null || rule == null || start == null || end == null) {
            showAlert("提示", "请填写完整的排班信息！", Alert.AlertType.WARNING);
            return;
        }

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                int count = 0;
                LocalDate curr = start;
                while (!curr.isAfter(end)) {
                    Schedule s = new Schedule();
                    s.setEmpId(selectedEmp.getEmpId());
                    s.setShiftRuleId(rule.getRuleId());
                    s.setScheduleDate(curr);
                    if (scheduleService.addSchedule(s, authToken)) count++;
                    curr = curr.plusDays(1);
                }
                return count;
            }

            @Override
            protected void succeeded() {
                showAlert("操作成功", "已为 " + selectedEmp.getEmpName() + " 批量排班 " + getValue() + " 天！", Alert.AlertType.INFORMATION);
                handleRefresh();
            }

            @Override
            protected void failed() {
                showAlert("操作失败", "错误详情：" + getException().getMessage(), Alert.AlertType.ERROR);
            }
        };
        new Thread(task).start();
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}