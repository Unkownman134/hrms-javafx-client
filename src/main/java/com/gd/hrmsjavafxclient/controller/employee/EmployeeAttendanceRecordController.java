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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 考勤记录视图控制器 (对应 AttendanceRecordView.fxml)
 */
public class EmployeeAttendanceRecordController implements EmployeeSubController {

    @FXML private ComboBox<String> monthComboBox;
    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private Button queryButton;

    // 对应 AttendanceRecordView.fxml 中的列 fx:id
    @FXML private TableColumn<AttendanceRecord, LocalDate> dateCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> clockInCol;
    @FXML private TableColumn<AttendanceRecord, LocalTime> clockOutCol;
    @FXML private TableColumn<AttendanceRecord, String> statusCol;
    @FXML private TableColumn<AttendanceRecord, String> noteCol;

    // --- 数据和状态 ---
    private final ObservableList<AttendanceRecord> data = FXCollections.observableArrayList();
    private CurrentUserInfo currentUser;
    private String authToken;
    private final AttendanceEmpService attendanceEmpService = new AttendanceEmpService();

    // ------------------------------------------------------------------
    // ManagerSubController 接口实现
    // ------------------------------------------------------------------

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
        initializeController(); // 在接收到用户信息后初始化
    }

    @Override
    public void initializeController() {
        // 确保 Platform.runLater 在 UI 线程执行初始化
        Platform.runLater(this::initialize);
    }

    /**
     * 自动初始化逻辑：绑定列、填充月份 ComboBox。
     */
    private void initialize() {
        // 1. 绑定 TableColumn 和 Model 属性
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        clockInCol.setCellValueFactory(new PropertyValueFactory<>("clockInTime"));
        clockOutCol.setCellValueFactory(new PropertyValueFactory<>("clockOutTime"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        // 2. 将数据绑定到 TableView
        attendanceTable.setItems(data);

        // 设置默认的占位符
        attendanceTable.setPlaceholder(new Label("请选择月份，点击查询按钮。"));

        // 3. 初始化月份 ComboBox
        initMonthComboBox();

        // 之前注释掉了自动查询，现在保持注释。
        // if (!monthComboBox.getSelectionModel().isEmpty()) {
        //     handleQueryButtonAction(null);
        // }
    }

    /**
     * 填充月份 ComboBox，从今年一月到当前月份。
     */
    private void initMonthComboBox() {
        // 使用北京时间判断当前时间
        YearMonth currentYearMonth = YearMonth.now();
        YearMonth startYearMonth = YearMonth.of(currentYearMonth.getYear(), 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        List<String> months = IntStream.rangeClosed(0, (int) startYearMonth.until(currentYearMonth, java.time.temporal.ChronoUnit.MONTHS))
                .mapToObj(startYearMonth::plusMonths)
                .map(ym -> ym.format(formatter))
                .collect(Collectors.toList());

        monthComboBox.setItems(FXCollections.observableArrayList(months));

        // 默认选中当前月份
        monthComboBox.getSelectionModel().selectLast();
    }

    // ------------------------------------------------------------------
    // FXML 动作方法
    // ------------------------------------------------------------------

    /**
     * 处理查询按钮事件 (FXML: handleQueryButtonAction)
     */
    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        String selectedMonthText = monthComboBox.getSelectionModel().getSelectedItem();

        if (selectedMonthText == null || selectedMonthText.isEmpty()) {
            showAlert("提示 💡", "请先选择要查询的月份哦！", Alert.AlertType.WARNING);
            return;
        }

        // 🌟 解析用户选择的月份，用于筛选
        YearMonth selectedYearMonth = YearMonth.parse(selectedMonthText, DateTimeFormatter.ofPattern("yyyy-MM"));

        // 禁用按钮并更改文本，防止重复点击
        queryButton.setDisable(true);
        queryButton.setText("查询中...");

        // 用于用户提示的友好格式
        String selectedMonthDisplay = selectedMonthText.substring(0, 4) + "年" + selectedMonthText.substring(5) + "月";

        // 清空表格
        data.clear();
        attendanceTable.setPlaceholder(new Label("正在加载所有考勤记录...请稍候。"));

        // 🌟 调试输出 1：查询开始
        System.out.println("--- 考勤查询开始 ---");
        System.out.println("员工 ID: " + currentUser.getEmpId());
        // 修正：现在查询所有，筛选月份写在日志里
        System.out.println("筛选月份: " + selectedMonthText);
        System.out.println("当前 Token: " + (authToken != null ? "已设置" : "未设置"));


        Task<List<AttendanceRecord>> loadTask = new Task<>() {
            @Override
            protected List<AttendanceRecord> call() throws Exception {
                // 调用 Service 方法，传入 EmpID 和月份 (月份参数现在被Service忽略，只请求所有记录)
                return attendanceEmpService.getAttendanceRecords(
                        currentUser.getEmpId(), selectedMonthText, authToken
                );
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<AttendanceRecord> allRecords = getValue();

                    // 🌟 核心修正：客户端筛选逻辑
                    List<AttendanceRecord> filteredRecords = allRecords.stream()
                            .filter(record -> {
                                // 检查 record.getDate() 是否存在且在选中月份内
                                LocalDate recordDate = record.getDate();
                                if (recordDate != null) {
                                    return YearMonth.from(recordDate).equals(selectedYearMonth);
                                }
                                return false; // 如果日期为空，则过滤掉
                            })
                            .collect(Collectors.toList());

                    // 🌟 调试输出 2：查询成功，打印结果数量
                    System.out.println("API 返回记录数 (所有): " + allRecords.size());
                    System.out.println("客户端筛选后记录数 (" + selectedMonthText + "): " + filteredRecords.size());

                    data.setAll(filteredRecords);
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    if (data.isEmpty()) {
                        attendanceTable.setPlaceholder(new Label(selectedMonthDisplay + " 暂时没有考勤记录呢。"));
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    // 🌟 调试输出 3：查询失败，打印异常堆栈
                    System.err.println("考勤查询失败！");
                    getException().printStackTrace();

                    attendanceTable.setPlaceholder(new Label("加载考勤记录失败 ❌: " + getException().getMessage()));
                    showAlert("错误 ❌", "加载考勤记录失败：\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                });
            }
        };

        new Thread(loadTask).start();
    }

    /**
     * 处理导出按钮事件 (FXML: handleExportButtonAction)
     */
    @FXML
    private void handleExportButtonAction() {
        showAlert("提示 💡", "导出记录功能正在开发中哦！🚀", Alert.AlertType.INFORMATION);
    }

    // ------------------------------------------------------------------
    // 辅助方法
    // ------------------------------------------------------------------

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