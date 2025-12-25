package com.gd.hrmsjavafxclient.controller.employee;

import com.fasterxml.jackson.databind.JsonNode;
import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Schedule;
import com.gd.hrmsjavafxclient.service.employee.ScheduleEmpService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工排班日历视图控制器 🌸
 */
public class EmployeeScheduleController implements EmployeeSubController {

    @FXML private Label monthLabel;
    @FXML private GridPane calendarGrid;

    private CurrentUserInfo currentUser;
    private String authToken;
    private final ScheduleEmpService scheduleService = new ScheduleEmpService();

    private YearMonth currentYearMonth = YearMonth.now();

    // 缓存班次规则详情：RuleID -> ShiftInfo(名称, 时间范围)
    private final Map<Integer, ShiftInfo> ruleCache = new HashMap<>();
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");

    /**
     * 内部类：保存班次规则的关键信息
     */
    private static class ShiftInfo {
        String name;
        String timeRange;

        ShiftInfo(String name, String start, String end) {
            this.name = name;
            // 格式化时间，去掉秒，比如 09:00:00 -> 09:00
            this.timeRange = formatTime(start) + " - " + formatTime(end);
        }

        private String formatTime(String t) {
            if (t == null || t.length() < 5) return "00:00";
            return t.substring(0, 5);
        }
    }

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
        initializeController();
    }

    @Override
    public void initializeController() {
        if (currentUser != null) {
            Platform.runLater(this::renderCalendar);
        }
    }

    private void renderCalendar() {
        monthLabel.setText(currentYearMonth.format(displayFormatter));
        calendarGrid.getChildren().clear();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        loadDataAndPopulate(firstOfMonth, firstDayOfWeek);
    }

    private void loadDataAndPopulate(LocalDate start, int firstDayOffset) {
        LocalDate end = currentYearMonth.atEndOfMonth();
        Task<List<Schedule>> task = new Task<>() {
            @Override
            protected List<Schedule> call() throws Exception {
                return scheduleService.getMySchedules(currentUser.getEmpId(), start, end, authToken);
            }
            @Override
            protected void succeeded() {
                List<Schedule> schedules = getValue();
                Platform.runLater(() -> {
                    Map<LocalDate, Schedule> scheduleMap = schedules.stream()
                            .collect(Collectors.toMap(Schedule::getScheduleDate, s -> s, (s1, s2) -> s1));
                    populateGrid(firstDayOffset, scheduleMap);
                });
            }
        };
        new Thread(task).start();
    }

    private void populateGrid(int offset, Map<LocalDate, Schedule> dataMap) {
        int row = 0;
        int col = offset - 1;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox cell = createDayCell(date, dataMap.get(date));
            calendarGrid.add(cell, col, row);
            col++;
            if (col > 6) { col = 0; row++; }
        }
    }

    private VBox createDayCell(LocalDate date, Schedule schedule) {
        VBox cell = new VBox(2);
        cell.setMinHeight(100);
        cell.setPadding(new Insets(10));
        cell.setAlignment(Pos.TOP_LEFT);

        // 样式：简约无阴影边框
        String baseStyle = "-fx-background-color: white; -fx-border-color: #D5DBDB; -fx-border-width: 0.5; -fx-border-radius: 2;";
        if (date.equals(LocalDate.now())) {
            baseStyle = "-fx-background-color: #F4FBFF; -fx-border-color: #3498DB; -fx-border-width: 1.5; -fx-border-radius: 2;";
        }
        cell.setStyle(baseStyle);

        // 1. 日期数字
        Label dateLbl = new Label(String.valueOf(date.getDayOfMonth()));
        dateLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        cell.getChildren().add(dateLbl);

        if (schedule != null) {
            // 2. 班次名
            Label shiftLbl = new Label("加载中...");
            shiftLbl.setMaxWidth(Double.MAX_VALUE);
            shiftLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #27AE60; -fx-font-weight: bold; -fx-padding: 5 0 0 0;");

            // 3. 时间段
            Label timeLbl = new Label("");
            timeLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #7F8C8D;");

            Integer ruleId = schedule.getShiftRuleId();
            if (ruleCache.containsKey(ruleId)) {
                ShiftInfo info = ruleCache.get(ruleId);
                shiftLbl.setText(info.name);
                timeLbl.setText(info.timeRange);
            } else {
                fetchShiftDetailAsync(ruleId, shiftLbl, timeLbl);
            }

            cell.getChildren().addAll(shiftLbl, timeLbl);
        }

        return cell;
    }

    /**
     * 核心逻辑：从 API 获取详细的 Rule 信息
     */
    private void fetchShiftDetailAsync(int ruleId, Label nameTarget, Label timeTarget) {
        new Thread(() -> {
            try {
                // 注意：这里需要后端返回包含 workStartTime 和 workEndTime 的 Json
                // 我们直接使用 scheduleService 获取原始 JsonNode
                JsonNode node = scheduleService.getShiftRuleFullNode(ruleId, authToken);

                if (node != null) {
                    String name = node.path("ruleName").asText("未知班次");
                    String start = node.path("workStartTime").asText("00:00:00");
                    String end = node.path("workEndTime").asText("00:00:00");

                    ShiftInfo info = new ShiftInfo(name, start, end);
                    ruleCache.put(ruleId, info);

                    Platform.runLater(() -> {
                        nameTarget.setText(info.name);
                        timeTarget.setText(info.timeRange);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> nameTarget.setText("无法获取"));
            }
        }).start();
    }

    @FXML
    private void handleToday(ActionEvent event) {
        currentYearMonth = YearMonth.now();
        renderCalendar();
    }

    @FXML
    private void handlePrevMonth(ActionEvent event) {
        currentYearMonth = currentYearMonth.minusMonths(1);
        renderCalendar();
    }

    @FXML
    private void handleNextMonth(ActionEvent event) {
        currentYearMonth = currentYearMonth.plusMonths(1);
        renderCalendar();
    }
}