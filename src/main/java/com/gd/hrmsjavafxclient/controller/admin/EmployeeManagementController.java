package com.gd.hrmsjavafxclient.controller.admin;

import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.service.admin.EmployeeAdminService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Map;

public class EmployeeManagementController {
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> empIdCol;
    @FXML private TableColumn<Employee, String> empNameCol;
    @FXML private TableColumn<Employee, String> genderCol;
    @FXML private TableColumn<Employee, String> phoneCol;
    @FXML private TableColumn<Employee, String> emailCol;
    @FXML private TableColumn<Employee, String> statusCol;
    @FXML private TableColumn<Employee, Integer> deptIdCol;
    @FXML private TableColumn<Employee, Integer> posIdCol;
    @FXML private TableColumn<Employee, Integer> managerIdCol;
    @FXML private TableColumn<Employee, Void> actionCol;

    private final EmployeeAdminService empService = new EmployeeAdminService();
    private final ObservableList<Employee> empList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        empNameCol.setCellValueFactory(new PropertyValueFactory<>("empName"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        deptIdCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));
        posIdCol.setCellValueFactory(new PropertyValueFactory<>("posId"));
        managerIdCol.setCellValueFactory(new PropertyValueFactory<>("managerId"));

        setupActionColumn();
        loadEmployeeData();
    }

    @FXML
    private void loadEmployeeData() {
        new Thread(() -> {
            try {
                var data = empService.getAllEmployees();
                Platform.runLater(() -> {
                    empList.setAll(data);
                    employeeTable.setItems(empList);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("修改");
            private final Button delBtn = new Button("删除");
            private final HBox box = new HBox(10, editBtn, delBtn);
            {
                editBtn.getStyleClass().add("action-button-white");
                delBtn.getStyleClass().add("action-button-delete");
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    private void handleNewEmployee() {
        showEditDialog(new Employee());
    }

    private void showEditDialog(Employee emp) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(emp.getEmpId() == null ? "入职新员工" : "修改员工信息");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(15);
        grid.setStyle("-fx-padding: 25;");

        // 基础输入
        TextField nameIn = new TextField(emp.getEmpName());
        ComboBox<String> genderIn = new ComboBox<>(FXCollections.observableArrayList("男", "女"));
        genderIn.setValue(emp.getGender() == null ? "男" : emp.getGender());
        TextField phoneIn = new TextField(emp.getPhone());
        TextField emailIn = new TextField(emp.getEmail());

        // 🌟 核心：下拉列表控件
        ComboBox<Map<String, Object>> deptCombo = new ComboBox<>();
        ComboBox<Map<String, Object>> posCombo = new ComboBox<>();
        ComboBox<Employee> managerCombo = new ComboBox<>();
        ComboBox<String> statusIn = new ComboBox<>(FXCollections.observableArrayList("在职", "离职", "休假"));
        statusIn.setValue(emp.getStatus() == null ? "在职" : emp.getStatus());

        // 设置 ComboBox 的显示逻辑 (展示名称，后台存ID)
        setupMapConverter(deptCombo, "deptName");
        setupMapConverter(posCombo, "posName");
        managerCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Employee e) { return e == null ? "无上级" : e.getEmpName(); }
            @Override public Employee fromString(String s) { return null; }
        });

        // 异步加载下拉数据
        new Thread(() -> {
            try {
                var depts = empService.getAllDepartments();
                var positions = empService.getAllPositions();
                var managers = empService.getAllEmployees();

                Platform.runLater(() -> {
                    deptCombo.setItems(FXCollections.observableArrayList(depts));
                    posCombo.setItems(FXCollections.observableArrayList(positions));
                    managerCombo.setItems(FXCollections.observableArrayList(managers));

                    // 默认回显选中
                    if (emp.getDeptId() != null)
                        depts.stream().filter(m -> emp.getDeptId().equals(m.get("deptId"))).findFirst().ifPresent(deptCombo::setValue);
                    if (emp.getPosId() != null)
                        positions.stream().filter(m -> emp.getPosId().equals(m.get("posId"))).findFirst().ifPresent(posCombo::setValue);
                    if (emp.getManagerId() != null)
                        managers.stream().filter(m -> emp.getManagerId().equals(m.getManagerId())).findFirst().ifPresent(managerCombo::setValue);
                });
            } catch (Exception ex) { ex.printStackTrace(); }
        }).start();

        grid.add(new Label("员工姓名:"), 0, 0); grid.add(nameIn, 1, 0);
        grid.add(new Label("员工性别:"), 0, 1); grid.add(genderIn, 1, 1);
        grid.add(new Label("联系电话:"), 0, 2); grid.add(phoneIn, 1, 2);
        grid.add(new Label("电子邮箱:"), 0, 3); grid.add(emailIn, 1, 3);
        grid.add(new Label("所属部门:"), 0, 4); grid.add(deptCombo, 1, 4);
        grid.add(new Label("担任职位:"), 0, 5); grid.add(posCombo, 1, 5);
        grid.add(new Label("汇报上级:"), 0, 6); grid.add(managerCombo, 1, 6);
        grid.add(new Label("当前状态:"), 0, 7); grid.add(statusIn, 1, 7);

        Button save = new Button("保存提交");
        save.getStyleClass().add("action-button");
        save.setMaxWidth(Double.MAX_VALUE);

        save.setOnAction(e -> {
            try {
                emp.setEmpName(nameIn.getText());
                emp.setGender(genderIn.getValue());
                emp.setPhone(phoneIn.getText());
                emp.setEmail(emailIn.getText());
                emp.setStatus(statusIn.getValue());

                // 从下拉框获取 ID
                if (deptCombo.getValue() != null) emp.setDeptId((Integer) deptCombo.getValue().get("deptId"));
                if (posCombo.getValue() != null) emp.setPosId((Integer) posCombo.getValue().get("posId"));
                if (managerCombo.getValue() != null) emp.setManagerId(managerCombo.getValue().getEmpId());

                if (emp.getJoinDate() == null) emp.setJoinDate(LocalDate.now());

                new Thread(() -> {
                    try {
                        if (emp.getEmpId() == null) empService.createEmployee(emp);
                        else empService.updateEmployee(emp.getEmpId(), emp);
                        Platform.runLater(() -> { stage.close(); loadEmployeeData(); });
                    } catch (Exception ex) { Platform.runLater(() -> showError("保存失败", ex.getMessage())); }
                }).start();
            } catch (Exception ex) { showError("错误", "请检查输入项！"); }
        });

        VBox root = new VBox(20, grid, save);
        root.setStyle("-fx-alignment: center; -fx-padding: 10;");
        stage.setScene(new Scene(root, 420, 650));
        stage.show();
    }

    private void setupMapConverter(ComboBox<Map<String, Object>> combo, String nameKey) {
        combo.setConverter(new StringConverter<>() {
            @Override public String toString(Map<String, Object> map) { return map == null ? "" : map.get(nameKey).toString(); }
            @Override public Map<String, Object> fromString(String s) { return null; }
        });
    }

    private void handleDelete(Employee emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定删除员工 [" + emp.getEmpName() + "] 吗？");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        empService.deleteEmployee(emp.getEmpId());
                        Platform.runLater(this::loadEmployeeData);
                    } catch (Exception ex) { Platform.runLater(() -> showError("删除失败", ex.getMessage())); }
                }).start();
            }
        });
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }
}