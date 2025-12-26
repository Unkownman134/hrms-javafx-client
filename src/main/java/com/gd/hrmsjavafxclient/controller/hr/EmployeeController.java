package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Department;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.model.Position;
import com.gd.hrmsjavafxclient.service.hr.HRDataService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeController implements HRSubController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> empNameCol;
    @FXML private TableColumn<Employee, String> genderCol;
    @FXML private TableColumn<Employee, String> phoneCol;
    @FXML private TableColumn<Employee, String> emailCol;
    @FXML private TableColumn<Employee, String> statusCol;
    @FXML private TableColumn<Employee, String> joinDateCol;

    @FXML private TableColumn<Employee, String> deptNameCol;
    @FXML private TableColumn<Employee, String> posNameCol;
    @FXML private TableColumn<Employee, String> managerNameCol;

    @FXML private TableColumn<Employee, Void> actionCol;

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    private List<Department> departmentList = new ArrayList<>();
    private List<Position> positionList = new ArrayList<>();
    private List<Employee> allEmployeesList = new ArrayList<>();

    private Map<Integer, String> deptMap = new HashMap<>();
    private Map<Integer, String> posMap = new HashMap<>();
    private Map<Integer, String> empNameMap = new HashMap<>();

    @FXML
    public void initialize() {
        empNameCol.setCellValueFactory(new PropertyValueFactory<>("empName"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        joinDateCol.setCellValueFactory(new PropertyValueFactory<>("joinDate"));

        deptNameCol.setCellValueFactory(cellData -> {
            Integer id = cellData.getValue().getDeptId();
            return new SimpleStringProperty(deptMap.getOrDefault(id, "未分配"));
        });

        posNameCol.setCellValueFactory(cellData -> {
            Integer id = cellData.getValue().getPosId();
            return new SimpleStringProperty(posMap.getOrDefault(id, "未分配"));
        });

        managerNameCol.setCellValueFactory(cellData -> {
            Integer id = cellData.getValue().getManagerId();
            if (id == null || id == 0) return new SimpleStringProperty("无");
            return new SimpleStringProperty(empNameMap.getOrDefault(id, "未知主管"));
        });

        setupActionColumn();
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadAllData();
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button deleteBtn = new Button("删除");
            private final HBox container = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("action-button-edit");
                deleteBtn.getStyleClass().add("action-button-delete");
                container.setAlignment(Pos.CENTER);
                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadAllData();
    }

    @FXML
    private void handleNewEmployee(ActionEvent event) {
        showEditDialog(new Employee());
    }

    private void loadAllData() {
        if (authToken == null) return;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                departmentList = hrDataService.getAllDepartments(authToken);
                deptMap = departmentList.stream().collect(Collectors.toMap(Department::getDeptId, Department::getDeptName, (v1,v2)->v1));

                positionList = hrDataService.getAllPositions(authToken);
                posMap = positionList.stream().collect(Collectors.toMap(Position::getPosId, Position::getPosName, (v1,v2)->v1));

                allEmployeesList = hrDataService.getAllEmployees(authToken);
                empNameMap = allEmployeesList.stream().collect(Collectors.toMap(Employee::getEmpId, Employee::getEmpName, (v1,v2)->v1));

                Platform.runLater(() -> {
                    employeeTable.setItems(FXCollections.observableArrayList(allEmployeesList));
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private void showEditDialog(Employee emp) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(emp.getEmpId() == null ? "新增员工档案" : "编辑员工档案");

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setPadding(new javafx.geometry.Insets(25));

        TextField nameField = new TextField(emp.getEmpName());
        ComboBox<String> genderBox = new ComboBox<>(FXCollections.observableArrayList("男", "女"));
        genderBox.setValue(emp.getGender() != null ? emp.getGender() : "男");
        TextField phoneField = new TextField(emp.getPhone());
        TextField emailField = new TextField(emp.getEmail());

        ComboBox<Department> deptComboBox = new ComboBox<>(FXCollections.observableArrayList(departmentList));
        deptComboBox.setPromptText("选择部门");
        deptComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Department d) { return d == null ? "" : d.getDeptName(); }
            @Override public Department fromString(String s) { return null; }
        });
        if (emp.getDeptId() != null) {
            departmentList.stream().filter(d -> d.getDeptId().equals(emp.getDeptId())).findFirst().ifPresent(deptComboBox::setValue);
        }

        ComboBox<Position> posComboBox = new ComboBox<>(FXCollections.observableArrayList(positionList));
        posComboBox.setPromptText("选择职位");
        posComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Position p) { return p == null ? "" : p.getPosName(); }
            @Override public Position fromString(String s) { return null; }
        });
        if (emp.getPosId() != null) {
            positionList.stream().filter(p -> p.getPosId().equals(emp.getPosId())).findFirst().ifPresent(posComboBox::setValue);
        }

        ComboBox<Employee> managerComboBox = new ComboBox<>(FXCollections.observableArrayList(allEmployeesList));
        managerComboBox.setPromptText("选择主管 (可选)");
        managerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Employee e) { return e == null ? "无" : e.getEmpName(); }
            @Override public Employee fromString(String s) { return null; }
        });
        if (emp.getManagerId() != null) {
            allEmployeesList.stream().filter(e -> e.getEmpId().equals(emp.getManagerId())).findFirst().ifPresent(managerComboBox::setValue);
        }

        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList("在职", "离职", "休假"));
        statusBox.setValue(emp.getStatus() != null ? emp.getStatus() : "在职");

        grid.add(new Label("姓名:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("性别:"), 0, 1); grid.add(genderBox, 1, 1);
        grid.add(new Label("电话:"), 0, 2); grid.add(phoneField, 1, 2);
        grid.add(new Label("邮箱:"), 0, 3); grid.add(emailField, 1, 3);
        grid.add(new Label("部门:"), 0, 4); grid.add(deptComboBox, 1, 4);
        grid.add(new Label("职位:"), 0, 5); grid.add(posComboBox, 1, 5);
        grid.add(new Label("主管:"), 0, 6); grid.add(managerComboBox, 1, 6);
        grid.add(new Label("状态:"), 0, 7); grid.add(statusBox, 1, 7);

        Button saveBtn = new Button("保存提交");
        saveBtn.getStyleClass().add("action-button-primary");
        saveBtn.setMinWidth(120);
        saveBtn.setOnAction(e -> {
            emp.setEmpName(nameField.getText());
            emp.setGender(genderBox.getValue());
            emp.setPhone(phoneField.getText());
            emp.setEmail(emailField.getText());
            emp.setStatus(statusBox.getValue());
            if (deptComboBox.getValue() != null) emp.setDeptId(deptComboBox.getValue().getDeptId());
            if (posComboBox.getValue() != null) emp.setPosId(posComboBox.getValue().getPosId());
            if (managerComboBox.getValue() != null) emp.setManagerId(managerComboBox.getValue().getEmpId());

            new Thread(() -> {
                boolean success = (emp.getEmpId() == null) ?
                        hrDataService.createEmployee(emp, authToken).isPresent() :
                        hrDataService.updateEmployee(emp, authToken);

                Platform.runLater(() -> {
                    if (success) { stage.close(); loadAllData(); }
                    else { new Alert(Alert.AlertType.ERROR, "保存员工失败，请检查网络！").show(); }
                });
            }).start();
        });

        VBox layout = new VBox(25, grid, saveBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new javafx.geometry.Insets(10, 10, 30, 10));
        stage.setScene(new Scene(layout, 420, 580));
        stage.show();
    }

    private void handleDelete(Employee emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定要删除员工 [" + emp.getEmpName() + "] 吗？\n该操作无法撤销！");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                new Thread(() -> {
                    boolean success = hrDataService.deleteEmployee(emp.getEmpId(), authToken);
                    Platform.runLater(() -> {
                        if (success) loadAllData();
                        else new Alert(Alert.AlertType.ERROR, "删除失败").show();
                    });
                }).start();
            }
        });
    }
}