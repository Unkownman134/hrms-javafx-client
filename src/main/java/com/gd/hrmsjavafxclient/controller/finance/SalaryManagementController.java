package com.gd.hrmsjavafxclient.controller.finance;

import com.gd.hrmsjavafxclient.model.SalaryRecord;
import com.gd.hrmsjavafxclient.service.finance.FinanceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class SalaryManagementController {

    @FXML private ComboBox<String> monthFilterCombo;
    @FXML private TableView<SalaryRecord> salaryTable;
    @FXML private TableColumn<SalaryRecord, Integer> colId;
    @FXML private TableColumn<SalaryRecord, Integer> colEmpId;
    @FXML private TableColumn<SalaryRecord, String> colMonth;
    @FXML private TableColumn<SalaryRecord, BigDecimal> colGross;
    @FXML private TableColumn<SalaryRecord, BigDecimal> colTax;
    @FXML private TableColumn<SalaryRecord, BigDecimal> colNet;
    @FXML private TableColumn<SalaryRecord, LocalDate> colDate;

    private final FinanceService financeService = new FinanceService();

    // 原始总数据
    private final ObservableList<SalaryRecord> masterData = FXCollections.observableArrayList();
    // 过滤后的视图数据
    private FilteredList<SalaryRecord> filteredData;

    private String token;

    @FXML
    public void initialize() {
        // 1. 初始化表格列绑定
        colId.setCellValueFactory(data -> data.getValue().recordIdProperty().asObject());
        colEmpId.setCellValueFactory(data -> data.getValue().empIdProperty().asObject());
        colMonth.setCellValueFactory(data -> data.getValue().salaryMonthProperty());
        colGross.setCellValueFactory(data -> data.getValue().grossPayProperty());
        colTax.setCellValueFactory(data -> data.getValue().taxDeductionProperty());
        colNet.setCellValueFactory(data -> data.getValue().netPayProperty());
        colDate.setCellValueFactory(data -> data.getValue().payDateProperty());

        // 2. 建立 FilteredList 并绑定到表格
        filteredData = new FilteredList<>(masterData, p -> true);
        salaryTable.setItems(filteredData);

        // 3. 监听下拉框变化，触发筛选
        monthFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            applyFilter(newVal);
        });
    }

    public void initData(String token) {
        this.token = token;
        loadData();
    }

    @FXML
    private void loadData() {
        try {
            List<SalaryRecord> records = financeService.getAllSalaryRecords(token);
            masterData.setAll(records);

            // 4. 自动提取所有月份，用于填充下拉框
            updateMonthComboOptions(records);

        } catch (Exception e) {
            e.printStackTrace();
            showError("加载失败", "无法从服务器获取工资记录。");
        }
    }

    private void updateMonthComboOptions(List<SalaryRecord> records) {
        // 提取去重后的月份列表，并排序（倒序，最新的在前面）
        List<String> months = records.stream()
                .map(SalaryRecord::getSalaryMonth)
                .distinct()
                .sorted((a, b) -> b.compareTo(a))
                .collect(Collectors.toList());

        monthFilterCombo.setItems(FXCollections.observableArrayList(months));
    }

    private void applyFilter(String selectedMonth) {
        filteredData.setPredicate(record -> {
            // 如果没选或者选了空，显示全部
            if (selectedMonth == null || selectedMonth.isEmpty()) {
                return true;
            }
            // 匹配年月 (假设格式一致)
            return record.getSalaryMonth().equals(selectedMonth);
        });
    }

    @FXML
    private void handleReset() {
        monthFilterCombo.getSelectionModel().clearSelection();
        filteredData.setPredicate(p -> true);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}