package com.gd.hrmsjavafxclient.controller.finance;

import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.service.finance.FinanceService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SalaryManagementController {

    @FXML private StackPane employeeListContainer;
    @FXML private TextField searchEmployeeField;
    @FXML private CheckBox selectAllCheckBox;
    @FXML private DatePicker salaryDatePicker;

    @FXML private TableView<SalaryRecord> salaryTable;
    @FXML private TableColumn<SalaryRecord, Integer> colId;
    @FXML private TableColumn<SalaryRecord, Integer> colEmpId;
    @FXML private TableColumn<SalaryRecord, String> colMonth;
    @FXML private TableColumn<SalaryRecord, BigDecimal> colGross;
    @FXML private TableColumn<SalaryRecord, BigDecimal> colTax;
    @FXML private TableColumn<SalaryRecord, BigDecimal> colNet;
    @FXML private TableColumn<SalaryRecord, LocalDate> colDate;

    private ListView<Employee> employeeListView;
    private final Map<Employee, BooleanProperty> selectionMap = new HashMap<>();

    private final FinanceService financeService = new FinanceService();
    private String token;
    private ObservableList<SalaryRecord> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // 1. 初始化左侧员工列表
        employeeListView = new ListView<>();
        employeeListView.setCellFactory(CheckBoxListCell.forListView(selectionMap::get, new StringConverter<Employee>() {
            @Override
            public String toString(Employee object) {
                return object == null ? "" : object.getEmpName() + " (ID: " + object.getEmpId() + ")";
            }
            @Override
            public Employee fromString(String string) { return null; }
        }));
        employeeListContainer.getChildren().add(employeeListView);

        // 🌟 核心修正：纯 Java 逻辑控制 DatePicker 只显示和处理“年月”
        // 设置日期显示转换器
        salaryDatePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? formatter.format(date) : "";
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    // 解析 yyyy-MM 时，内部补齐为该月 1 号
                    return LocalDate.parse(string + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                return null;
            }
        });

        // 监听值变化：无论用户在弹窗点哪一天，都自动修正为该月 1 号，配合 Converter 达到只选月份的效果
        salaryDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getDayOfMonth() != 1) {
                salaryDatePicker.setValue(LocalDate.of(newVal.getYear(), newVal.getMonth(), 1));
            }
        });

        // 2. 工资表格列绑定
        colId.setCellValueFactory(new PropertyValueFactory<>("recordId"));
        colEmpId.setCellValueFactory(new PropertyValueFactory<>("empId"));
        colMonth.setCellValueFactory(new PropertyValueFactory<>("salaryMonth"));
        colGross.setCellValueFactory(new PropertyValueFactory<>("grossPay"));
        colTax.setCellValueFactory(new PropertyValueFactory<>("taxDeduction"));
        colNet.setCellValueFactory(new PropertyValueFactory<>("netPay"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("payDate"));

        salaryTable.setItems(masterData);

        // 搜索过滤逻辑
        searchEmployeeField.textProperty().addListener((obs, oldVal, newVal) -> updateFilteredEmployeeList(newVal));
    }

    public void initData(String token) {
        this.token = token;
        loadEmployees();
        loadAllData();
    }

    private void loadEmployees() {
        try {
            List<Employee> employees = financeService.getAllEmployees(token);
            selectionMap.clear();
            employees.forEach(e -> selectionMap.put(e, new SimpleBooleanProperty(false)));
            employeeListView.setItems(FXCollections.observableArrayList(employees));
        } catch (Exception e) {
            showError("错误", "无法加载员工列表: " + e.getMessage());
        }
    }

    @FXML
    public void handleSelectAll() {
        boolean selected = selectAllCheckBox.isSelected();
        selectionMap.values().forEach(prop -> prop.set(selected));
    }

    @FXML
    public void loadPersonalHistory() {
        Employee selected = employeeListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("提示", "请先在列表中点击选择一位员工！");
            return;
        }
        try {
            List<SalaryRecord> history = financeService.getSalaryHistory(token, selected.getEmpId());
            masterData.setAll(history);
        } catch (Exception e) {
            showError("查询失败", e.getMessage());
        }
    }

    @FXML
    public void loadAllData() {
        try {
            List<SalaryRecord> records = financeService.getAllSalaryRecords(token);
            masterData.setAll(records);
        } catch (Exception e) {
            showError("加载失败", e.getMessage());
        }
    }

    @FXML
    public void handleBatchCalculate() {
        List<Employee> selectedEmployees = selectionMap.entrySet().stream()
                .filter(entry -> entry.getValue().get())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        LocalDate date = salaryDatePicker.getValue();
        if (selectedEmployees.isEmpty() || date == null) {
            showError("提示", "请勾选员工并选择结算月份！");
            return;
        }

        // 强制格式化为 yyyy-MM 传给后端
        String monthStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        int success = 0;
        for (Employee emp : selectedEmployees) {
            try {
                financeService.calculateSalary(token, emp.getEmpId(), monthStr);
                success++;
            } catch (Exception e) {
                System.err.println("结算失败 ID " + emp.getEmpId() + ": " + e.getMessage());
            }
        }
        showInfo("完成", "已成功结算 " + success + " 位员工的 " + monthStr + " 账期工资！");
        loadAllData();
    }

    private void updateFilteredEmployeeList(String filter) {
        List<Employee> all = new ArrayList<>(selectionMap.keySet());
        if (filter == null || filter.isEmpty()) {
            employeeListView.setItems(FXCollections.observableArrayList(all));
        } else {
            List<Employee> filtered = all.stream()
                    .filter(e -> e.getEmpName().contains(filter) || String.valueOf(e.getEmpId()).contains(filter))
                    .collect(Collectors.toList());
            employeeListView.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}