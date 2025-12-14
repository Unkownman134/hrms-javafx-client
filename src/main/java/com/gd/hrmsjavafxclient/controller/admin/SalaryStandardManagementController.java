package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.SalaryStandard;
import com.gd.hrmsjavafxclient.service.admin.SalaryStandardAdminService;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.List;

/**
 * R8: 薪酬标准配置控制器 (超级管理员/财务管理员子视图)
 * 🌟 修复：新增 handleRefresh 方法，用于响应 FXML 中的刷新按钮。
 */
public class SalaryStandardManagementController {

    // --- TableView 控件 ---
    @FXML private TableView<SalaryStandard> standardTable;
    @FXML private TableColumn<SalaryStandard, Integer> standardIdCol;
    @FXML private TableColumn<SalaryStandard, String> standardNameCol;
    @FXML private TableColumn<SalaryStandard, Double> baseSalaryCol;
    @FXML private TableColumn<SalaryStandard, Double> allowanceCol;
    @FXML private TableColumn<SalaryStandard, Double> bonusCol;
    @FXML private TableColumn<SalaryStandard, Double> totalAmountCol;
    @FXML private TableColumn<SalaryStandard, Void> actionCol;

    // --- Form 控件 ---
    @FXML private Label formTitle;
    @FXML private TextField standardIdField;
    @FXML private TextField standardNameField;
    @FXML private TextField baseSalaryField;
    @FXML private TextField allowanceField;
    @FXML private TextField bonusField;
    @FXML private Label totalAmountLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final SalaryStandardAdminService service = new SalaryStandardAdminService();
    private final ObservableList<SalaryStandard> standardData = FXCollections.observableArrayList();
    private SalaryStandard selectedStandard = null;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    @FXML
    public void initialize() {
        // 🌟 修正 PropertyValueFactory 的字段名
        standardIdCol.setCellValueFactory(new PropertyValueFactory<>("stdId"));
        standardNameCol.setCellValueFactory(new PropertyValueFactory<>("standardName"));
        baseSalaryCol.setCellValueFactory(new PropertyValueFactory<>("basicSalary"));
        allowanceCol.setCellValueFactory(new PropertyValueFactory<>("mealAllowance"));
        bonusCol.setCellValueFactory(new PropertyValueFactory<>("allowances"));
        totalAmountCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        // 格式化数字列显示
        formatDoubleColumn(baseSalaryCol);
        formatDoubleColumn(allowanceCol);
        formatDoubleColumn(bonusCol);
        formatDoubleColumn(totalAmountCol);

        standardTable.setItems(standardData);

        // 初始化 TotalAmount Label 绑定
        totalAmountLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            try {
                double basic = parseDouble(baseSalaryField.getText());
                double allowance = parseDouble(allowanceField.getText());
                double bonus = parseDouble(bonusField.getText());
                return decimalFormat.format(basic + allowance + bonus);
            } catch (NumberFormatException e) {
                return "0.00 (输入错误)";
            }
        }, baseSalaryField.textProperty(), allowanceField.textProperty(), bonusField.textProperty()));

        // TableView 行选择监听器
        standardTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedStandard = newSelection;
                editStandard(newSelection);
            } else {
                clearForm();
            }
        });

        // 添加操作列
        addActionColumn();

        // 首次加载数据
        loadStandardData();
    }

    // --- 按钮处理方法 ---

    // 🌟 核心修复：新增 handleRefresh 方法来调用加载逻辑，解决 FXML LoadException
    @FXML
    public void handleRefresh() {
        loadStandardData();
    }

    @FXML
    public void handleSave() {
        try {
            // 1. 校验输入
            if (standardNameField.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "保存失败 ❌", "薪酬标准名称不能为空！");
                return;
            }
            if (parseDouble(baseSalaryField.getText()) <= 0) {
                showAlert(Alert.AlertType.ERROR, "保存失败 ❌", "基本工资必须大于 0！");
                return;
            }
            // ...

            // 2. 构造薪酬标准对象，使用修正后的字段名
            SalaryStandard standardToSave = new SalaryStandard();
            standardToSave.setStandardName(standardNameField.getText().trim());
            standardToSave.setBasicSalary(parseDouble(baseSalaryField.getText()));
            standardToSave.setMealAllowance(parseDouble(allowanceField.getText()));
            standardToSave.setAllowances(parseDouble(bonusField.getText()));

            if (selectedStandard == null) {
                // 新增 (C)
                createStandard(standardToSave);
            } else {
                // 更新 (U)
                standardToSave.setStdId(selectedStandard.getStdId());
                updateStandard(standardToSave);
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "输入错误 ❌", "工资字段必须输入有效的数字！");
        }
    }

    @FXML
    public void handleCancel() {
        standardTable.getSelectionModel().clearSelection();
        clearForm();
        formTitle.setText("新增薪酬标准");
        saveButton.setText("保存");
        saveButton.setDisable(false);
    }

    // --- 核心业务逻辑 ---

    private void loadStandardData() {
        Task<List<SalaryStandard>> loadTask = new Task<>() {
            @Override
            protected List<SalaryStandard> call() throws Exception {
                // ⚠️ 注意：这里调用的是 Service 中配置了大小写不敏感的 ObjectMapper 的方法
                return service.getAllSalaryStandards();
            }

            @Override
            protected void succeeded() {
                standardData.clear();
                standardData.addAll(getValue());
                standardTable.setItems(standardData);
            }

            @Override
            protected void failed() {
                showAlert(Alert.AlertType.ERROR, "加载失败 ❌", "无法从服务器加载数据：" + getException().getMessage());
                getException().printStackTrace();
            }
        };
        new Thread(loadTask).start();
    }

    private void createStandard(SalaryStandard standard) {
        Task<SalaryStandard> createTask = new Task<>() {
            @Override
            protected SalaryStandard call() throws Exception {
                return service.createSalaryStandard(standard);
            }

            @Override
            protected void succeeded() {
                standardData.add(0, getValue());
                showAlert(Alert.AlertType.INFORMATION, "新增成功 ✅", "薪酬标准 " + getValue().getStandardName() + " 已创建！");
                handleCancel();
            }

            @Override
            protected void failed() {
                showAlert(Alert.AlertType.ERROR, "新增失败 ❌", "创建操作失败：\n" + getException().getMessage());
                getException().printStackTrace();
            }
        };
        new Thread(createTask).start();
    }

    private void updateStandard(SalaryStandard standard) {
        Task<SalaryStandard> updateTask = new Task<>() {
            @Override
            protected SalaryStandard call() throws Exception {
                return service.updateSalaryStandard(standard.getStdId(), standard);
            }

            @Override
            protected void succeeded() {
                // 找到旧对象并替换（或通知TableView更新）
                int index = standardData.indexOf(selectedStandard);
                if (index != -1) {
                    standardData.set(index, getValue());
                }
                showAlert(Alert.AlertType.INFORMATION, "更新成功 ✅", "薪酬标准 " + getValue().getStandardName() + " 已更新！");
                handleCancel();
            }

            @Override
            protected void failed() {
                showAlert(Alert.AlertType.ERROR, "更新失败 ❌", "更新操作失败：\n" + getException().getMessage());
                getException().printStackTrace();
            }
        };
        new Thread(updateTask).start();
    }

    private void deleteStandard(SalaryStandard standard) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText("即将删除薪酬标准：" + standard.getStandardName());
        alert.setContentText("此操作不可逆，确定删除吗？");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    service.deleteSalaryStandard(standard.getStdId());
                    return null;
                }

                @Override
                protected void succeeded() {
                    standardData.remove(standard);
                    showAlert(Alert.AlertType.INFORMATION, "删除成功 ✅", "薪酬标准 " + standard.getStandardName() + " 已被删除。");
                    handleCancel();
                }

                @Override
                protected void failed() {
                    showAlert(Alert.AlertType.ERROR, "删除失败 ❌", "删除操作失败：\n" + getException().getMessage());
                    getException().printStackTrace();
                }
            };
            new Thread(deleteTask).start();
        }
    }

    // --- Form/TableView 辅助方法 ---

    private void editStandard(SalaryStandard standard) {
        formTitle.setText("编辑薪酬标准 (ID: " + standard.getStdId() + ")");
        saveButton.setText("更新");

        standardIdField.setText(String.valueOf(standard.getStdId()));
        standardNameField.setText(standard.getStandardName());

        baseSalaryField.setText(decimalFormat.format(standard.getBasicSalary() != null ? standard.getBasicSalary() : 0.0));
        allowanceField.setText(decimalFormat.format(standard.getMealAllowance() != null ? standard.getMealAllowance() : 0.0));
        bonusField.setText(decimalFormat.format(standard.getAllowances() != null ? standard.getAllowances() : 0.0));
    }

    private void clearForm() {
        standardIdField.setText("");
        standardNameField.setText("");
        baseSalaryField.setText("0.00");
        allowanceField.setText("0.00");
        bonusField.setText("0.00");
        selectedStandard = null;
    }

    private double parseDouble(String text) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(text.trim());
    }

    private void formatDoubleColumn(TableColumn<SalaryStandard, Double> column) {
        column.setCellFactory(tc -> new TableCell<>() {
            private final DecimalFormat format = new DecimalFormat("0.00");

            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });
    }

    private void addActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("编辑");
            private final Button deleteButton = new Button("删除");
            private final HBox pane = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(event -> {
                    selectedStandard = getTableView().getItems().get(getIndex());
                    editStandard(selectedStandard);
                    formTitle.setText("编辑薪酬标准 (ID: " + selectedStandard.getStdId() + ")");
                    saveButton.setText("更新");
                    saveButton.setDisable(false);
                });

                deleteButton.setOnAction(event -> {
                    SalaryStandard standard = getTableView().getItems().get(getIndex());
                    deleteStandard(standard);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}