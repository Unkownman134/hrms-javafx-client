package com.gd.hrmsjavafxclient.controller.employee;

import com.gd.hrmsjavafxclient.controller.employee.EmployeeMainController.EmployeeSubController;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.service.EmployeeService;
import com.gd.hrmsjavafxclient.service.EmployeeServiceImpl; // 🌟 导入实现类
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
 * 🌟 修正：实例化 EmployeeServiceImpl，并在 API 调用时使用 EmpID。
 */
public class SalaryRecordController implements EmployeeSubController {

    @FXML private ComboBox<String> yearComboBox;
    @FXML private TableView<SalaryRecord> salaryRecordTable;
    @FXML private TableColumn<SalaryRecord, String> monthCol;
    @FXML private TableColumn<SalaryRecord, LocalDate> payDateCol;
    @FXML private TableColumn<SalaryRecord, BigDecimal> grossPayCol;
    @FXML private TableColumn<SalaryRecord, BigDecimal> taxDeductionCol;
    @FXML private TableColumn<SalaryRecord, BigDecimal> netPayCol;
    @FXML private TableColumn<SalaryRecord, Void> actionCol;
    @FXML private Button queryButton;

    // --- 数据和状态 ---
    // 🌟 修正：直接实例化实现类
    private final EmployeeService employeeService = new EmployeeServiceImpl();
    private CurrentUserInfo currentUser;
    private String authToken;
    private final ObservableList<SalaryRecord> data = FXCollections.observableArrayList();

    @Override
    public void setUserInfo(CurrentUserInfo userInfo, String authToken) {
        this.currentUser = userInfo;
        this.authToken = authToken;
    }

    @Override
    public void initializeController() {
        // ... (省略 ComboBox 和 TableColumn 初始化，与上文相同)
        List<String> years = IntStream.rangeClosed(Year.now().getValue() - 5, Year.now().getValue())
                .mapToObj(String::valueOf)
                .sorted((a, b) -> b.compareTo(a))
                .collect(Collectors.toList());
        yearComboBox.setItems(FXCollections.observableArrayList(years));
        yearComboBox.getSelectionModel().selectFirst();

        monthCol.setCellValueFactory(cellData -> cellData.getValue().monthProperty());
        payDateCol.setCellValueFactory(cellData -> cellData.getValue().payDateProperty());
        grossPayCol.setCellValueFactory(cellData -> cellData.getValue().grossPayProperty());
        taxDeductionCol.setCellValueFactory(cellData -> cellData.getValue().taxDeductionProperty());
        netPayCol.setCellValueFactory(cellData -> cellData.getValue().netPayProperty());

        addActionColumn(); // 设置“查看详情”按钮列

        salaryRecordTable.setItems(data);

        handleQueryButtonAction(null);
    }

    @FXML
    private void handleQueryButtonAction(ActionEvent event) {
        String selectedYearText = yearComboBox.getSelectionModel().getSelectedItem();
        if (selectedYearText == null || currentUser == null || authToken == null || currentUser.getEmpId() == null) {
            showAlert("提示", "请选择年份或等待用户信息加载。", Alert.AlertType.WARNING);
            return;
        }

        queryButton.setDisable(true);
        queryButton.setText("加载中...");

        int year = Integer.parseInt(selectedYearText);

        Task<List<SalaryRecord>> loadTask = new Task<>() {
            @Override
            protected List<SalaryRecord> call() throws Exception {
                // 🌟 修正点：使用 currentUser.getEmpId() 进行 API 调用
                return employeeService.getSalaryRecords(currentUser.getEmpId(), year, authToken);
            }

            @Override
            protected void succeeded() {
                // ... (省略成功逻辑，与上文相同)
                Platform.runLater(() -> {
                    data.setAll(getValue());
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    if (data.isEmpty()) {
                        showAlert("提示", selectedYearText + " 年暂时没有工资记录呢。", Alert.AlertType.INFORMATION);
                    }
                });
            }

            @Override
            protected void failed() {
                // ... (省略失败逻辑，与上文相同)
                Platform.runLater(() -> {
                    showAlert("错误 ❌", "加载工资记录失败，请检查网络和API连接！" + getException().getMessage(), Alert.AlertType.ERROR);
                    queryButton.setText("查 询");
                    queryButton.setDisable(false);
                    getException().printStackTrace();
                });
            }
        };

        new Thread(loadTask).start();
    }

    // --- 辅助方法 (addActionColumn, showDetailAlert, showAlert 保持不变) ---
    private void addActionColumn() {
        actionCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<SalaryRecord, Void> call(TableColumn<SalaryRecord, Void> param) {
                return new TableCell<>() {
                    private final Button detailButton = new Button("查看详情");
                    {
                        detailButton.setOnAction(event -> {
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
                            setGraphic(detailButton);
                        }
                    }
                };
            }
        });
    }

    private void showDetailAlert(SalaryRecord record) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("工资条详细信息 💰");
        alert.setHeaderText(record.getMonth() + " 工资详情");

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