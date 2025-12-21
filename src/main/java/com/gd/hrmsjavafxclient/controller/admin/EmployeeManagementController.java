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

public class EmployeeManagementController {
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> empIdCol;
    @FXML private TableColumn<Employee, String> empNameCol;
    @FXML private TableColumn<Employee, String> genderCol;
    @FXML private TableColumn<Employee, String> phoneCol;
    @FXML private TableColumn<Employee, String> statusCol;
    @FXML private TableColumn<Employee, Integer> deptIdCol;
    @FXML private TableColumn<Employee, Void> actionCol;

    private final EmployeeAdminService empService = new EmployeeAdminService();
    private final ObservableList<Employee> empList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        empNameCol.setCellValueFactory(new PropertyValueFactory<>("empName"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        deptIdCol.setCellValueFactory(new PropertyValueFactory<>("deptId"));

        addActionButtons();

        loadData();
    }

    @FXML
    private void loadEmployeeData() {
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                var list = empService.getAllEmployees();
                Platform.runLater(() -> {
                    empList.setAll(list);
                    employeeTable.setItems(empList);
                });
            } catch (Exception e) {
                e.printStackTrace();
                showError("数据加载失败", e.getMessage());
            }
        }).start();
    }

    private void addActionButtons() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("编辑");
            private final Button delBtn = new Button("删除");
            private final HBox container = new HBox(10, editBtn, delBtn);

            {
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
                delBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                editBtn.setOnAction(e -> showEditDialog(getTableView().getItems().get(getIndex())));

                delBtn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定要删除员工 " + emp.getEmpName() + " 吗？");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            try {
                                empService.deleteEmployee(emp.getEmpId());
                                loadData();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showError("删除失败", ex.getMessage());
                            }
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
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
        stage.setTitle(emp.getEmpId() == null ? "新增员工" : "编辑员工");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 20;");

        TextField nameIn = new TextField(emp.getEmpName());
        TextField phoneIn = new TextField(emp.getPhone());
        TextField deptIn = new TextField(emp.getDeptId() == null ? "" : String.valueOf(emp.getDeptId()));
        ComboBox<String> genderIn = new ComboBox<>(FXCollections.observableArrayList("男", "女"));
        genderIn.setValue(emp.getGender() == null ? "男" : emp.getGender());

        grid.add(new Label("姓名:"), 0, 0); grid.add(nameIn, 1, 0);
        grid.add(new Label("性别:"), 0, 1); grid.add(genderIn, 1, 1);
        grid.add(new Label("电话:"), 0, 2); grid.add(phoneIn, 1, 2);
        grid.add(new Label("部门ID:"), 0, 3); grid.add(deptIn, 1, 3);

        Button save = new Button("保存提交");
        save.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        save.setOnAction(e -> {
            try {
                emp.setEmpName(nameIn.getText());
                emp.setGender(genderIn.getValue());
                emp.setPhone(phoneIn.getText());
                emp.setDeptId(Integer.parseInt(deptIn.getText()));

                if (emp.getEmpId() == null) {
                    empService.createEmployee(emp);
                } else {
                    empService.updateEmployee(emp.getEmpId(), emp);
                }
                stage.close();
                loadData();
            } catch (NumberFormatException nfe) {
                showError("输入错误", "部门ID必须是数字！");
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("保存失败", ex.getMessage());
            }
        });

        VBox root = new VBox(20, grid, save);
        root.setStyle("-fx-alignment: center; -fx-padding: 20;");
        stage.setScene(new Scene(root, 350, 300));
        stage.show();
    }

    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}