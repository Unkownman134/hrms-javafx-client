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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工排班日历视图控制器 🌸
 * 增加了“已通过”状态的请假和出差渲染功能
 */
public class EmployeeScheduleController implements EmployeeSubController {

    @FXML private Label monthLabel;
    @FXML private GridPane calendarGrid;

    private CurrentUserInfo currentUser;
    private String authToken;
    private final ScheduleEmpService scheduleService = new ScheduleEmpService();

    private YearMonth currentYearMonth = YearMonth.now();
    private final Map<Integer, ShiftInfo> ruleCache = new HashMap<>();
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("yyyy年MM月");

    // 缓存已通过的申请
    private List<JsonNode> approvedApps = new ArrayList<>();

    private static class ShiftInfo {
        String name;
        String timeRange;
        ShiftInfo(String name, String start, String end) {
            this.name = name;
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
            renderCalendar();
        }
    }

    private void renderCalendar() {
        monthLabel.setText(currentYearMonth.format(displayFormatter));
        calendarGrid.getChildren().clear();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        loadAllData(firstOfMonth, firstDayOfWeek);
    }

    private void loadAllData(LocalDate start, int firstDayOffset) {
        LocalDate end = currentYearMonth.atEndOfMonth();

        Task<Map<LocalDate, Schedule>> task = new Task<>() {
            @Override
            protected Map<LocalDate, Schedule> call() throws Exception {
                // 1. 获取申请列表，匹配状态为“已通过”的请假或出差
                List<JsonNode> allApps = scheduleService.getMyApplications(currentUser.getEmpId(), authToken);
                approvedApps = allApps.stream()
                        .filter(node -> {
                            String status = node.path("status").asText();
                            String type = node.path("requestType").asText();
                            // 修正：匹配“已通过”状态
                            return "已通过".equals(status) && ("请假".equals(type) || "出差".equals(type));
                        })
                        .collect(Collectors.toList());

                // 2. 获取排班表
                List<Schedule> schedules = scheduleService.getMySchedules(currentUser.getEmpId(), start, end, authToken);
                return schedules.stream().collect(Collectors.toMap(Schedule::getScheduleDate, s -> s, (s1, s2) -> s1));
            }

            @Override
            protected void succeeded() {
                Map<LocalDate, Schedule> scheduleMap = getValue();
                Platform.runLater(() -> populateGrid(firstDayOffset, scheduleMap));
            }
        };
        new Thread(task).start();
    }

    private void populateGrid(int offset, Map<LocalDate, Schedule> scheduleMap) {
        int row = 0;
        int col = offset - 1;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            VBox cell = createDayCell(date, scheduleMap.get(date));
            calendarGrid.add(cell, col, row);
            col++;
            if (col > 6) { col = 0; row++; }
        }
    }

    private VBox createDayCell(LocalDate date, Schedule schedule) {
        VBox cell = new VBox(5);
        cell.setMinHeight(100);
        cell.setPadding(new Insets(10));
        cell.setAlignment(Pos.TOP_LEFT);

        // 1. 基础样式
        String baseStyle = "-fx-background-color: white; -fx-border-color: #D5DBDB; -fx-border-width: 0.5; -fx-border-radius: 5;";
        if (date.equals(LocalDate.now())) {
            baseStyle = "-fx-background-color: #F4FBFF; -fx-border-color: #3498DB; -fx-border-width: 1.5; -fx-border-radius: 5;";
        }
        cell.setStyle(baseStyle);

        // 2. 日期数字
        Label dateLbl = new Label(String.valueOf(date.getDayOfMonth()));
        dateLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");
        cell.getChildren().add(dateLbl);

        // 3. 检查并渲染“已通过”的申请（请假/出差）
        JsonNode activeApp = findAppForDate(date);
        if (activeApp != null) {
            String type = activeApp.path("requestType").asText();
            Label appLbl = new Label(type);

            // 根据类型设置不同颜色
            String color = "请假".equals(type) ? "#E74C3C" : "#E67E22"; // 请假红色，出差橙色
            appLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-background-color: " + color + "; -fx-padding: 2 5; -fx-background-radius: 3;");
            cell.getChildren().add(appLbl);

            // 改变格子背景色表示这一天有特殊状态
            cell.setStyle(cell.getStyle() + "-fx-background-color: #FDEDEC;");
        }

        // 4. 渲染排班
        if (schedule != null) {
            Label shiftLbl = new Label("加载中...");
            shiftLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #27AE60; -fx-font-weight: bold;");

            Label timeLbl = new Label("");
            timeLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #7F8C8D;");

            Integer ruleId = schedule.getShiftRuleId();
            if (ruleCache.containsKey(ruleId)) {
                ShiftInfo info = ruleCache.get(ruleId);
                shiftLbl.setText(info.name);
                timeLbl.setText(info.timeRange);
            } else {
                fetchShiftDetailAsync(ruleId, shiftLbl, timeLbl);
            }
            cell.getChildren().addAll(shiftLbl, timeLbl);
        } else if (activeApp == null) {
            // 既没排班也没申请才显示休息
            Label restLbl = new Label("休息");
            restLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #BDC3C7;");
            cell.getChildren().add(restLbl);
        }

        return cell;
    }

    private JsonNode findAppForDate(LocalDate date) {
        for (JsonNode app : approvedApps) {
            try {
                LocalDate start = LocalDate.parse(app.path("startDate").asText());
                LocalDate end = LocalDate.parse(app.path("endDate").asText());
                // 包含起止日期的判断
                if (!date.isBefore(start) && !date.isAfter(end)) {
                    return app;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void fetchShiftDetailAsync(int ruleId, Label nameTarget, Label timeTarget) {
        new Thread(() -> {
            try {
                JsonNode node = scheduleService.getShiftRuleFullNode(ruleId, authToken);
                if (node != null) {
                    ShiftInfo info = new ShiftInfo(
                            node.path("ruleName").asText("未知"),
                            node.path("workStartTime").asText("00:00:00"),
                            node.path("workEndTime").asText("00:00:00")
                    );
                    ruleCache.put(ruleId, info);
                    Platform.runLater(() -> {
                        nameTarget.setText(info.name);
                        timeTarget.setText(info.timeRange);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> nameTarget.setText("未获取"));
            }
        }).start();
    }

    @FXML private void handleToday(ActionEvent event) { currentYearMonth = YearMonth.now(); renderCalendar(); }
    @FXML private void handlePrevMonth(ActionEvent event) { currentYearMonth = currentYearMonth.minusMonths(1); renderCalendar(); }
    @FXML private void handleNextMonth(ActionEvent event) { currentYearMonth = currentYearMonth.plusMonths(1); renderCalendar(); }
}