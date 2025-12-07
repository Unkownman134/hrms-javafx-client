package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.Department;
import com.gd.hrmsjavafxclient.service.DepartmentAdminService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.util.Optional;
import java.util.List;

/**
 * R1: 部门信息管理控制器 (超级管理员/人事管理员子视图)
 */
public class DepartmentManagementController {

    // --- TableView 控件 ---
    @FXML private TableView<Department> departmentTable;
    @FXML private TableColumn<Department, Integer> deptIdCol;
    @FXML private TableColumn<Department, String> deptNameCol;
    @FXML private TableColumn<Department, Void> actionCol;

    // --- Form 控件 ---
    @FXML private Label formTitle;
    @FXML private TextField deptIdField;
    @FXML private TextField deptNameField;
    @FXML private Button saveButton;

    // --- 数据和 Service ---
    private final DepartmentAdminService departmentService = new DepartmentAdminService();
    private final ObservableList<Department> departmentList = FXCollections.observableArrayList();
    private Department selectedDepartment = null; // 用于跟踪当前编辑/新增的部门

    @FXML
    public void initialize() {
        // 1. 初始化表格列和数据绑定
        deptIdCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));
        deptNameCol.setCellValueFactory(new PropertyValueFactory<>("deptName"));
        departmentTable.setItems(departmentList);

        // 2. 监听表格选择事件，加载详情
        departmentTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDepartmentDetails(newValue));

        // 3. 设置操作列 (Edit/Delete Button)
        setupActionColumn();

        // 4. 默认加载数据
        loadDepartmentData();
    }

    // --- 数据加载 (R) ---

    private void loadDepartmentData() {
        Task<List<Department>> loadTask = new Task<>() {
            @Override
            protected List<Department> call() throws Exception {
                return departmentService.getAllDepartments();
            }

            @Override
            protected void succeeded() {
                departmentList.clear();
                departmentList.addAll(getValue());
                // showAlert(Alert.AlertType.INFORMATION, "数据刷新成功", "已加载 " + departmentList.size() + " 条部门记录。");
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
                showAlert(Alert.AlertType.ERROR, "加载失败 🚨", "无法从服务器获取数据：" + getException().getMessage());
            }
        };

        new Thread(loadTask).start();
    }

    // --- 表格操作列 (Edit/Delete) ---
    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {

            final Button editButton = new Button("编辑");
            final Button deleteButton = new Button("删除");
            final HBox pane = new HBox(5, editButton, deleteButton);

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                    Department department = getTableView().getItems().get(getIndex());

                    editButton.setOnAction(event -> {
                        showDepartmentDetails(department); // 选中并填充表单
                        formTitle.setText("编辑部门 ID: " + department.getDeptId());
                        selectedDepartment = department; // 标记为编辑状态
                    });

                    deleteButton.setOnAction(event -> handleDelete(department));
                }
            }
        });
    }

    // --- 详情显示与编辑 (R/U Form) ---

    private void showDepartmentDetails(Department department) {
        if (department == null) {
            handleCancel();
            return;
        }

        // 填充表单字段
        deptIdField.setText(department.getDeptId() != null ? String.valueOf(department.getDeptId()) : "");
        deptNameField.setText(department.getDeptName());

        // 更新表单标题和状态
        formTitle.setText("部门信息详情/编辑 ID: " + department.getDeptId());
        selectedDepartment = department;
    }

    // --- 按钮事件处理 (C/U/D) ---

    @FXML
    private void handleRefresh() {
        loadDepartmentData();
    }

    @FXML
    private void handleNewDepartment() {
        clearForm();
        formTitle.setText("新增部门信息");
        selectedDepartment = new Department(); // 标记为新增状态
    }

    @FXML
    private void handleCancel() {
        clearForm();
        formTitle.setText("部门信息详情");
        selectedDepartment = null;
        departmentTable.getSelectionModel().clearSelection(); // 清除表格选中
    }

    // 创建/保存 (C/U)
    @FXML
    private void handleSave() {
        if (selectedDepartment == null) {
            showAlert(Alert.AlertType.WARNING, "操作警告", "请先选择要编辑的部门或点击 '新增部门' 按钮。");
            return;
        }

        // 1. 校验和构建数据对象
        Department dataToSend = new Department();
        boolean isNew = selectedDepartment.getDeptId() == null;

        try {
            String name = deptNameField.getText().trim();

            if (name.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "输入错误", "部门名称不能为空！");
                return;
            }

            // --- 赋值 ---
            dataToSend.setDeptName(name);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "请检查输入字段是否正确填写。");
            return;
        }

        // 2. 执行网络操作
        Task<Department> saveTask = new Task<>() {
            @Override
            protected Department call() throws Exception {
                if (isNew) {
                    // C: Create
                    return departmentService.createDepartment(dataToSend);
                } else {
                    // U: Update (更新操作需要 ID)
                    return departmentService.updateDepartment(selectedDepartment.getDeptId(), dataToSend);
                }
            }

            @Override
            protected void succeeded() {
                Department result = getValue();
                showAlert(Alert.AlertType.INFORMATION, "成功 ✅", (isNew ? "新增" : "更新") + "部门信息成功！ID: " + result.getDeptId());
                clearForm();
                loadDepartmentData(); // 刷新数据
            }

            @Override
            protected void failed() {
                showAlert(Alert.AlertType.ERROR, "操作失败 ❌", "执行操作时出错：" + getException().getMessage());
                getException().printStackTrace();
            }
        };
        new Thread(saveTask).start();
    }

    // 删除 (D)
    private void handleDelete(Department department) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("确认删除部门: " + department.getDeptName() + " (ID: " + department.getDeptId() + ") 吗？");
        confirmAlert.setContentText("注意：如果该部门下有员工，后端通常会阻止删除，或者你需要先调整员工的部门！");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    departmentService.deleteDepartment(department.getDeptId());
                    return null;
                }

                @Override
                protected void succeeded() {
                    showAlert(Alert.AlertType.INFORMATION, "删除成功 ✅", "部门档案 " + department.getDeptName() + " 已被删除。");
                    loadDepartmentData();
                    handleCancel();
                }

                @Override
                protected void failed() {
                    showAlert(Alert.AlertType.ERROR, "删除失败 ❌", "删除操作失败：" + getException().getMessage());
                    getException().printStackTrace();
                }
            };
            new Thread(deleteTask).start();
        }
    }

    // --- 辅助方法 ---
    private void clearForm() {
        deptIdField.setText("");
        deptNameField.setText("");
        selectedDepartment = null;
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