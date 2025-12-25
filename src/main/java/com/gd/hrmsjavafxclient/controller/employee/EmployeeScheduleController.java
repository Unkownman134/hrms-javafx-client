package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Schedule;
import com.gd.hrmsjavafxclient.service.employee.ScheduleEmpService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工排班查看控制器 🌸
 */
public class EmployeeScheduleController implements EmployeeSubController {

    @FXML private DatePicker monthPicker;
    @FXML private TableView<Schedule> scheduleTable;
    @FXML private Button queryButton;

    @FXML private TableColumn<Schedule, LocalDate> dateCol;
    @FXML private TableColumn<Schedule, String> shiftCol;

    private final ObservableList<Schedule> data = FXCollections.observableArrayList();
    private CurrentUserInfo currentUser;
    private String authToken;
    private final ScheduleEmpService scheduleService = new ScheduleEmpService();
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    // 缓存班次名
    private final Map<Integer, String> ruleCache = new HashMap<>();

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
        // 绑定日期列
        dateCol.setCellValueFactory(new PropertyValueFactory<>("scheduleDate"));

        // 绑定班次名称列，逻辑由 Service 提供
        shiftCol.setCellValueFactory(cellData -> {
            Schedule s = cellData.getValue();
            Integer ruleId = s.getShiftRuleId();
            if (ruleId == null) return new SimpleStringProperty("未排班");

            if (ruleCache.containsKey(ruleId)) {
                return new SimpleStringProperty(ruleCache.get(ruleId));
            } else {
                fetchShiftNameAsync(ruleId);
                return new SimpleStringProperty("加载中...");
            }
        });

        scheduleTable.setItems(data);
        setupMonthPicker();
        loadScheduleData();
    }

    /**
     * 🌟 调用 Service 层获取名称
     */
    private void fetchShiftNameAsync(int ruleId) {
        new Thread(() -> {
            try {
                // 通过 service 调用 API
                String name = scheduleService.getShiftRuleName(ruleId, authToken);
                ruleCache.put(ruleId, name);
                Platform.runLater(() -> scheduleTable.refresh());
            } catch (Exception e) {
                System.err.println("获取班次名失败: " + e.getMessage());
                ruleCache.put(ruleId, "未知(ID:" + ruleId + ")");
            }
        }).start();
    }

    private void setupMonthPicker() {
        monthPicker.setValue(LocalDate.now());
        monthPicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? monthFormatter.format(date) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                return null;
            }
        });
    }

    @FXML
    private void handleQueryAction(ActionEvent event) {
        loadScheduleData();
    }

    private void loadScheduleData() {
        if (currentUser == null) return;
        LocalDate selectedDate = monthPicker.getValue();
        if (selectedDate == null) return;

        YearMonth ym = YearMonth.from(selectedDate);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        queryButton.setDisable(true);
        queryButton.setText("加载中...");

        Task<List<Schedule>> task = new Task<>() {
            @Override
            protected List<Schedule> call() throws Exception {
                // 调用 service
                return scheduleService.getMySchedules(currentUser.getEmpId(), start, end, authToken);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    queryButton.setDisable(false);
                    queryButton.setText("查询排班");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    queryButton.setDisable(false);
                    queryButton.setText("查询排班");
                });
            }
        };
        new Thread(task).start();
    }
}