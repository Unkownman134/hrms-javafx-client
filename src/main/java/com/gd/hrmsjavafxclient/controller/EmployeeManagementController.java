package com.gd.hrmsjavafxclient.controller;

import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.service.EmployeeAdminService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * R10: 员工档案管理控制器 (超级管理员/人事管理员子视图)
 */
public class EmployeeManagementController {

    // --- TableView 控件 ---
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> empIdCol;
    @FXML private TableColumn<Employee, String> empNameCol;
    @FXML private TableColumn<Employee, String> genderCol;
    @FXML private TableColumn<Employee, String> phoneCol;
    @FXML private TableColumn<Employee, LocalDate> joinDateCol; // 🌟 LocalDate 类型
    @FXML private TableColumn<Employee, String> statusCol;
    @FXML private TableColumn<Employee, Integer> deptIdCol;
    @FXML private TableColumn<Employee, Integer> posIdCol;
    @FXML private TableColumn<Employee, Integer> managerIdCol;
    @FXML private TableColumn<Employee, Void> actionCol;

    // --- Form 控件 ---
    @FXML private Label formTitle;
    @FXML private TextField empIdField;
    @FXML private TextField empNameField;
    @FXML private TextField genderField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker joinDateField; // 🌟 DatePicker 控件
    @FXML private TextField statusField;
    @FXML private TextField deptIdField;
    @FXML private TextField posIdField;
    @FXML private TextField managerIdField;
    @FXML private Button saveButton;

    // --- 业务常量 ---
    private static final List<String> GENDERS = Arrays.asList("男", "女");
    private static final List<String> STATUSES = Arrays.asList("在职", "离职", "休假");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // --- 数据和 Service ---
    private final EmployeeAdminService employeeService = new EmployeeAdminService();
    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();
    private Employee selectedEmployee = null; // 用于跟踪当前编辑的员工

    @FXML
    public void initialize() {
        // 1. 初始化表格列和数据绑定
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        empNameCol.setCellValueFactory(new PropertyValueFactory<>("empName"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        deptIdCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));
        posIdCol.setCellValueFactory(new PropertyValueFactory<>("posId"));
        managerIdCol.setCellValueFactory(new PropertyValueFactory<>("managerId"));

        // 🌟 日期列的特殊处理：确保以 yyyy-MM-dd 格式显示
        joinDateCol.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
        joinDateCol.setCellFactory(column -> new TableCell<Employee, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DATE_FORMATTER));
                }
            }
        });

        employeeTable.setItems(employeeList);

        // 2. 监听表格选择事件，加载详情
        employeeTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showEmployeeDetails(newValue));

        // 3. 设置操作列 (Edit/Delete Button)
        setupActionColumn();

        // 4. 默认加载数据
        loadEmployeeData();
    }

    // --- 数据加载 (R) ---

    private void loadEmployeeData() {
        Task<List<Employee>> loadTask = new Task<>() {
            @Override
            protected List<Employee> call() throws Exception {
                return employeeService.getAllEmployees();
            }

            @Override
            protected void succeeded() {
                employeeList.clear();
                employeeList.addAll(getValue());
//                showAlert(Alert.AlertType.INFORMATION, "数据刷新成功", "已加载 " + employeeList.size() + " 条员工档案记录。");
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
//                showAlert(Alert.AlertType.ERROR, "加载失败 🚨", "无法从服务器获取数据：" + getException().getMessage());
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
                    Employee employee = getTableView().getItems().get(getIndex());

                    editButton.setOnAction(event -> {
                        showEmployeeDetails(employee); // 选中并填充表单
                        formTitle.setText("编辑员工 ID: " + employee.getEmpId());
                        selectedEmployee = employee; // 标记为编辑状态
                    });

                    deleteButton.setOnAction(event -> handleDelete(employee));
                }
            }
        });
    }

    // --- 详情显示与编辑 (R/U Form) ---

    private void showEmployeeDetails(Employee employee) {
        if (employee == null) {
            handleCancel();
            return;
        }

        // 填充表单字段
        empIdField.setText(employee.getEmpId() != null ? String.valueOf(employee.getEmpId()) : "");
        empNameField.setText(employee.getEmpName());
        genderField.setText(employee.getGender());
        phoneField.setText(employee.getPhone());
        emailField.setText(employee.getEmail());
        joinDateField.setValue(employee.getJoinDate()); // 🌟 填充 DatePicker
        statusField.setText(employee.getStatus());
        deptIdField.setText(employee.getDeptId() != null ? String.valueOf(employee.getDeptId()) : "");
        posIdField.setText(employee.getPosId() != null ? String.valueOf(employee.getPosId()) : "");
        managerIdField.setText(employee.getManagerId() != null ? String.valueOf(employee.getManagerId()) : "");

        // 更新表单标题和状态
        formTitle.setText("员工档案详情/编辑 ID: " + employee.getEmpId());
        selectedEmployee = employee;
    }

    // --- 按钮事件处理 (C/U/D) ---

    @FXML
    private void handleRefresh() {
        loadEmployeeData();
    }

    @FXML
    private void handleNewEmployee() {
        clearForm();
        formTitle.setText("新增员工档案");
        selectedEmployee = new Employee(); // 标记为新增状态
    }

    @FXML
    private void handleCancel() {
        clearForm();
        formTitle.setText("员工档案详情");
        selectedEmployee = null;
        employeeTable.getSelectionModel().clearSelection(); // 清除表格选中
    }

    // 创建/保存 (C/U)
    @FXML
    private void handleSave() {
        if (selectedEmployee == null) {
            showAlert(Alert.AlertType.WARNING, "操作警告", "请先选择要编辑的员工或点击 '新增员工' 按钮。");
            return;
        }

        // 1. 校验和构建数据对象
        Employee dataToSend = new Employee();
        boolean isNew = selectedEmployee.getEmpId() == null;

        try {
            // --- 必填字段校验 ---
            String name = empNameField.getText().trim();
            String gender = genderField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String status = statusField.getText().trim();
            LocalDate joinDate = joinDateField.getValue(); // 🌟 获取 DatePicker 值
            String deptIdText = deptIdField.getText().trim();
            String posIdText = posIdField.getText().trim();

            if (name.isEmpty() || gender.isEmpty() || phone.isEmpty() || email.isEmpty() ||
                    joinDate == null || status.isEmpty() || deptIdText.isEmpty() || posIdText.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "输入错误", "员工姓名、性别、电话、邮箱、入职日期、状态、部门ID和职位ID都是必填项！");
                return;
            }

            // --- 格式校验 ---
            if (!GENDERS.contains(gender)) {
                showAlert(Alert.AlertType.ERROR, "输入错误", "性别必须是 '男' 或 '女'。");
                return;
            }
            if (!STATUSES.contains(status)) {
                showAlert(Alert.AlertType.ERROR, "输入错误", "状态必须是 '在职', '离职' 或 '休假'。");
                return;
            }

            // --- 赋值 ---
            dataToSend.setEmpName(name);
            dataToSend.setGender(gender);
            dataToSend.setPhone(phone);
            dataToSend.setEmail(email);
            dataToSend.setJoinDate(joinDate);
            dataToSend.setStatus(status);

            // 外键赋值 (必须是数字)
            dataToSend.setDeptId(Integer.parseInt(deptIdText));
            dataToSend.setPosId(Integer.parseInt(posIdText));

            // 经理ID (可选)
            String managerIdText = managerIdField.getText().trim();
            if (!managerIdText.isEmpty()) {
                dataToSend.setManagerId(Integer.parseInt(managerIdText));
            } else {
                dataToSend.setManagerId(null);
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "部门ID、职位ID和上级经理ID必须是有效的数字。");
            return;
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "输入错误", "请检查所有输入字段是否正确填写。详细错误: " + e.getMessage());
            return;
        }

        // 2. 执行网络操作
        Task<Employee> saveTask = new Task<>() {
            @Override
            protected Employee call() throws Exception {
                if (isNew) {
                    // C: Create
                    return employeeService.createEmployee(dataToSend);
                } else {
                    // U: Update (更新操作需要 ID)
                    return employeeService.updateEmployee(selectedEmployee.getEmpId(), dataToSend);
                }
            }

            @Override
            protected void succeeded() {
                showAlert(Alert.AlertType.INFORMATION, "成功 ✅", (isNew ? "新增" : "更新") + "员工档案信息成功！");
                clearForm();
                loadEmployeeData(); // 刷新数据
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
    private void handleDelete(Employee employee) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("确认删除");
        confirmAlert.setHeaderText("确认删除员工档案: " + employee.getEmpName() + " (ID: " + employee.getEmpId() + ") 吗？");
        confirmAlert.setContentText("注意：删除员工可能会影响到与其关联的其他系统记录，操作不可逆！");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    employeeService.deleteEmployee(employee.getEmpId());
                    return null;
                }

                @Override
                protected void succeeded() {
                    showAlert(Alert.AlertType.INFORMATION, "删除成功 ✅", "员工档案 " + employee.getEmpName() + " 已被删除。");
                    loadEmployeeData();
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
        empIdField.setText("");
        empNameField.setText("");
        genderField.setText("");
        phoneField.setText("");
        emailField.setText("");
        joinDateField.setValue(null); // 清空 DatePicker
        statusField.setText("");
        deptIdField.setText("");
        posIdField.setText("");
        managerIdField.setText("");
        selectedEmployee = null;
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