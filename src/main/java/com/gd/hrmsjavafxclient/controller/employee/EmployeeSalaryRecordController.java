package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
// ❌ 移除旧的 Service 引用
// import com.gd.hrmsjavafxclient.service.EmployeeService;
// import com.gd.hrmsjavafxclient.service.EmployeeServiceImpl;

// ✅ 导入新的 SalaryEmpService
import com.gd.hrmsjavafxclient.service.employee.SalaryEmpService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 工资条视图控制器 (对应 SalaryRecordView.fxml)
 * 🌟 修正：实例化 SalaryEmpService，并在 API 调用时使用 EmpID。
 */
public class EmployeeSalaryRecordController implements EmployeeSubController {

    @FXML private ComboBox<String> yearComboBox;
    @FXML private TableView<SalaryRecord> salaryRecordTable;
    @FXML private TableColumn<SalaryRecord, String> monthCol;
    @FXML private TableColumn<SalaryRecord, LocalDate> payDateCol;
    @FXML private TableColumn<SalaryRecord, BigDecimal> grossPayCol;
    @FXML private TableColumn<SalaryRecord, BigDecimal> netPayCol;
    @FXML private TableColumn<SalaryRecord, Void> actionCol;
    @FXML private Button queryButton;

    // --- 数据和状态 ---
    // 🌟 修正：直接实例化 SalaryEmpService
    private final SalaryEmpService salaryEmpService = new SalaryEmpService();
    private CurrentUserInfo currentUser;
    private String authToken;
    private final ObservableList<SalaryRecord> data = FXCollections.observableArrayList();

    // --- 初始化和数据设置 ---

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @Override
    public void initializeController() {
        // 初始化年份下拉框，从当前年开始向前推 5 年
        int currentYear = Year.now().getValue();
        List<String> years = IntStream.rangeClosed(currentYear - 5, currentYear)
                .mapToObj(String::valueOf)
                .sorted((s1, s2) -> s2.compareTo(s1)) // 降序排列
                .collect(Collectors.toList());
        yearComboBox.setItems(FXCollections.observableArrayList(years));

        // 默认选择当前年
        yearComboBox.getSelectionModel().selectFirst();

        // 绑定 TableView
        salaryRecordTable.setItems(data);
        monthCol.setCellValueFactory(cellData -> cellData.getValue().monthProperty());
        payDateCol.setCellValueFactory(cellData -> cellData.getValue().payDateProperty());
        grossPayCol.setCellValueFactory(cellData -> cellData.getValue().grossPayProperty());
        netPayCol.setCellValueFactory(cellData -> cellData.getValue().netPayProperty());

        // 添加详情按钮列
        addDetailButtonToTable();

        // 默认加载当前年份数据
        handleQueryButtonAction(null);
    }

    // --- 查询方法 ---

    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        String selectedYearString = yearComboBox.getSelectionModel().getSelectedItem();
        if (selectedYearString == null) {
            showAlert("提示", "请选择要查询的年份哦。", Alert.AlertType.WARNING);
            return;
        }

        int selectedYear = Integer.parseInt(selectedYearString);

        queryButton.setDisable(true);
        queryButton.setText("查询中...");

        Task<List<SalaryRecord>> loadTask = new Task<>() {
            @Override
            protected List<SalaryRecord> call() throws Exception {
                if (currentUser.getEmpId() == null) {
                    throw new IllegalStateException("员工ID缺失，无法查询记录！");
                }
                // 🌟 调用新的 SalaryEmpService 方法
                return salaryEmpService.getSalaryRecords(
                        currentUser.getEmpId(), selectedYear, authToken
                );
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    if (data.isEmpty()) {
                        showAlert("提示", selectedYearString + " 暂时没有工资记录呢。", Alert.AlertType.INFORMATION);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showAlert("错误 ❌", "加载工资记录失败：\n" + getException().getMessage(), Alert.AlertType.ERROR);
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    getException().printStackTrace();
                });
            }
        };

        new Thread(loadTask).start();
    }

    // --- 辅助方法 ---

    private void addDetailButtonToTable() {
        Callback<TableColumn<SalaryRecord, Void>, TableCell<SalaryRecord, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<SalaryRecord, Void> call(final TableColumn<SalaryRecord, Void> param) {
                final TableCell<SalaryRecord, Void> cell = new TableCell<>() {

                    private final Button detailButton = new Button("详 情");
                    {
                        detailButton.getStyleClass().add("table-action-button"); // 假设有这个样式
                        detailButton.setOnAction((ActionEvent event) -> {
                            SalaryRecord record = getTableView().getItems().get(getIndex());
                            showDetailAlert(record);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            // 按钮放在 HBox 中，便于居中和控制边距
                            HBox box = new HBox(detailButton);
                            box.setStyle("-fx-alignment: center;");
                            setGraphic(box);
                        }
                    }
                };
                return cell;
            }
        };

        actionCol.setCellFactory(cellFactory);
    }

    private void showDetailAlert(SalaryRecord record) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("工资条详细信息 💰");
        alert.setHeaderText(record.getMonth() + " 工资详情");

        // 假设 SalaryRecord 包含所有必要的字段
        String content = String.format(
                "发放日期: %s\n" +
                        "应发总额: %s\n" +
                        "扣税额: %s\n" +
                        "实发净额: %s\n" +
                        "\n(这里可以展示更详细的五险一金和津贴信息)",
                record.getPayDate(), record.getGrossPay(), record.getTaxDeduction(), record.getNetPay()
        );
        alert.setContentText(content);
        alert.showAndWait();
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