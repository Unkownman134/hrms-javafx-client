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
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 部门经理排班界面控制器
 * 🌟 升级版：支持自定义日期区间查询，不再受限于下拉框月度选择
 */
public class DeptScheduleController implements ManagerSubController {

    @FXML private Label deptNameLabel;

    // 🌟 查询条件改为 DatePicker
    @FXML private DatePicker queryStartDatePicker;
    @FXML private DatePicker queryEndDatePicker;

    // 下方批量添加区域的控件
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
    private final ObservableList<ShiftRule> allShiftRules = FXCollections.observableArrayList();

    private volatile boolean isRefreshing = false;

    @Override
    public void setManagerContext(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;

        Platform.runLater(() -> {
            if (userInfo.getDepartmentName() != null) {
                deptNameLabel.setText("当前部门: " + userInfo.getDepartmentName());
            }
            initUI();
            loadInitialData();
        });
    }

    private void initUI() {
        // 🌟 初始化查询日期：默认为当月第一天和最后一天
        LocalDate today = LocalDate.now();
        queryStartDatePicker.setValue(today.with(TemporalAdjusters.firstDayOfMonth()));
        queryEndDatePicker.setValue(today.with(TemporalAdjusters.lastDayOfMonth()));

        // 表格绑定
        employeeNameCol.setCellValueFactory(d -> d.getValue().employeeNameProperty());
        dateCol.setCellValueFactory(d -> d.getValue().dateProperty());
        shiftNameCol.setCellValueFactory(d -> d.getValue().shiftNameProperty());
        statusCol.setCellValueFactory(d -> d.getValue().statusProperty());

        scheduleTable.setItems(scheduleData);

        // 下拉框转换器
        employeeComboBox.setConverter(new StringConverter<Employee>() {
            @Override public String toString(Employee e) { return e == null ? "" : e.getEmpName(); }
            @Override public Employee fromString(String s) { return null; }
        });
        employeeComboBox.setItems(deptEmployees);

        shiftRuleComboBox.setConverter(new StringConverter<ShiftRule>() {
            @Override public String toString(ShiftRule r) { return r == null ? "" : r.getRuleName(); }
            @Override public ShiftRule fromString(String s) { return null; }
        });
        shiftRuleComboBox.setItems(allShiftRules);
    }

    private void loadInitialData() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 加载员工
                Optional<List<Employee>> allEmpsOpt = ServiceUtil.sendGet("/employees", authToken, new TypeReference<List<Employee>>() {});
                List<Employee> filtered = allEmpsOpt.orElse(new ArrayList<>()).stream()
                        .filter(e -> e.getDeptId() != null && e.getDeptId().equals(currentUser.getDeptId()))
                        .collect(Collectors.toList());

                // 加载规则
                Optional<List<ShiftRule>> rulesOpt = ServiceUtil.sendGet("/shift/rules", authToken, new TypeReference<List<ShiftRule>>() {});
                List<ShiftRule> rules = rulesOpt.orElse(new ArrayList<>());

                Platform.runLater(() -> {
                    deptEmployees.setAll(filtered);
                    allShiftRules.setAll(rules);
                    handleRefresh(); // 触发初始查询
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    @FXML
    private void handleRefresh() {
        if (isRefreshing) return;

        // 🌟 获取 DatePicker 的值
        LocalDate start = queryStartDatePicker.getValue();
        LocalDate end = queryEndDatePicker.getValue();

        if (start == null || end == null) {
            showAlert("提示", "请先选择完整的查询日期区间区间哦！", Alert.AlertType.WARNING);
            return;
        }

        if (start.isAfter(end)) {
            showAlert("日期错误", "开始日期不能大于结束日期！", Alert.AlertType.ERROR);
            return;
        }

        if (deptEmployees.isEmpty()) return;

        isRefreshing = true;
        scheduleData.clear();
        scheduleTable.setPlaceholder(new ProgressIndicator());

        String startDateStr = start.toString();
        String endDateStr = end.toString();

        Task<List<Schedule>> fetchTask = new Task<>() {
            @Override
            protected List<Schedule> call() throws Exception {
                List<Schedule> combinedResult = new ArrayList<>();

                Map<Integer, String> empMap = deptEmployees.stream()
                        .collect(Collectors.toMap(Employee::getEmpId, Employee::getEmpName, (v1, v2) -> v1));
                Map<Integer, String> ruleMap = allShiftRules.stream()
                        .collect(Collectors.toMap(ShiftRule::getRuleId, ShiftRule::getRuleName, (v1, v2) -> v1));

                for (Employee emp : deptEmployees) {
                    if (isCancelled()) break;
                    // 🌟 使用选择的日期区间发起请求
                    List<Schedule> empSchedules = scheduleService.getSchedulesByRange(
                            emp.getEmpId(), startDateStr, endDateStr, authToken
                    );
                    for (Schedule s : empSchedules) {
                        s.setEmployeeName(empMap.getOrDefault(s.getEmpId(), "未知员工"));
                        s.setShiftName(ruleMap.getOrDefault(s.getShiftRuleId(), "未定义"));
                        combinedResult.add(s);
                    }
                }
                return combinedResult;
            }

            @Override
            protected void succeeded() {
                scheduleData.setAll(getValue());
                finish();
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                finish();
            }

            private void finish() {
                isRefreshing = false;
                if (scheduleData.isEmpty()) {
                    scheduleTable.setPlaceholder(new Label("该时间段内没有排班数据记录。"));
                }
            }
        };
        new Thread(fetchTask).start();
    }

    @FXML
    private void handleBatchAdd() {
        Employee selectedEmp = employeeComboBox.getValue();
        ShiftRule rule = shiftRuleComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (selectedEmp == null || rule == null || start == null || end == null) {
            showAlert("还没填完呢", "要把所有选项都选好才能排班哦！", Alert.AlertType.WARNING);
            return;
        }

        Task<Integer> addTask = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                int count = 0;
                LocalDate current = start;
                while (!current.isAfter(end)) {
                    Schedule s = new Schedule();
                    s.setEmpId(selectedEmp.getEmpId());
                    s.setShiftRuleId(rule.getRuleId());
                    s.setScheduleDate(current);
                    if (scheduleService.addSchedule(s, authToken)) {
                        count++;
                    }
                    current = current.plusDays(1);
                }
                return count;
            }

            @Override
            protected void succeeded() {
                showAlert("成功", "已成功添加 " + getValue() + " 条排班记录。", Alert.AlertType.INFORMATION);
                handleRefresh();
            }
        };
        new Thread(addTask).start();
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