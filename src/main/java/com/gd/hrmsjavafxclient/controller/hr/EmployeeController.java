package com.gd.hrmsjavafxclient.controller.hr;

import com.gd.hrmsjavafxclient.controller.hr.HRMainController.HRSubController;
import com.gd.hrmsjavafxclient.model.CurrentUserInfo;
import com.gd.hrmsjavafxclient.model.Employee;
import com.gd.hrmsjavafxclient.service.hr.HRDataService;
import javafx.application.Platform;
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

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EmployeeController implements HRSubController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, Integer> empIdCol;
    @FXML private TableColumn<Employee, String> empNameCol;
    @FXML private TableColumn<Employee, String> genderCol;
    @FXML private TableColumn<Employee, String> phoneCol;
    @FXML private TableColumn<Employee, String> emailCol;
    @FXML private TableColumn<Employee, String> statusCol;
    @FXML private TableColumn<Employee, String> joinDateCol;
    @FXML private TableColumn<Employee, Void> actionCol;

    private final HRDataService hrDataService = new HRDataService();
    private String authToken;

    @FXML
    public void initialize() {
        empIdCol.setCellValueFactory(new PropertyValueFactory<>("empId"));
        empNameCol.setCellValueFactory(new PropertyValueFactory<>("empName"));
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        joinDateCol.setCellValueFactory(new PropertyValueFactory<>("joinDate"));

        setupActionColumn();
    }

    @Override
    public void setHRContext(CurrentUserInfo userInfo, String authToken) {
        this.authToken = authToken;
        loadEmployeeData();
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

                editBtn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    showEditDialog(emp);
                });

                deleteBtn.setOnAction(e -> {
                    Employee emp = getTableView().getItems().get(getIndex());
                    handleDelete(emp);
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
    private void handleRefresh(ActionEvent event) {
        loadEmployeeData();
    }

    @FXML
    private void handleNewEmployee(ActionEvent event) {
        showEditDialog(new Employee());
    }

    private void loadEmployeeData() {
        if (authToken == null) return;
        Task<List<Employee>> task = new Task<>() {
            @Override
            protected List<Employee> call() throws IOException, InterruptedException {
                return hrDataService.getAllEmployees(authToken);
            }
            @Override
            protected void succeeded() {
                employeeTable.setItems(FXCollections.observableArrayList(getValue()));
            }
        };
        new Thread(task).start();
    }

    private void showEditDialog(Employee emp) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(emp.getEmpId() == null ? "新增员工" : "编辑员工");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = new TextField(emp.getEmpName());
        TextField phoneField = new TextField(emp.getPhone());
        TextField emailField = new TextField(emp.getEmail());
        ComboBox<String> genderBox = new ComboBox<>(FXCollections.observableArrayList("男", "女"));
        genderBox.setValue(emp.getGender() != null ? emp.getGender() : "男");

        grid.add(new Label("姓名:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("性别:"), 0, 1); grid.add(genderBox, 1, 1);
        grid.add(new Label("电话:"), 0, 2); grid.add(phoneField, 1, 2);
        grid.add(new Label("邮箱:"), 0, 3); grid.add(emailField, 1, 3);

        Button saveBtn = new Button("保存");
        saveBtn.getStyleClass().add("action-button-primary");
        saveBtn.setOnAction(e -> {
            emp.setEmpName(nameField.getText());
            emp.setGender(genderBox.getValue());
            emp.setPhone(phoneField.getText());
            emp.setEmail(emailField.getText());

            new Thread(() -> {
                boolean success;
                if (emp.getEmpId() == null) {
                    success = hrDataService.createEmployee(emp, authToken).isPresent();
                } else {
                    success = hrDataService.updateEmployee(emp, authToken);
                }

                Platform.runLater(() -> {
                    if (success) {
                        stage.close();
                        loadEmployeeData();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "保存失败").show();
                    }
                });
            }).start();
        });

        VBox layout = new VBox(20, grid, saveBtn);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new javafx.geometry.Insets(10));
        stage.setScene(new Scene(layout, 350, 400));
        stage.show();
    }

    private void handleDelete(Employee emp) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "确定要注销员工 [" + emp.getEmpName() + "] 吗？");
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                new Thread(() -> {
                    boolean success = hrDataService.deleteEmployee(emp.getEmpId(), authToken);
                    Platform.runLater(() -> {
                        if (success) loadEmployeeData();
                        else new Alert(Alert.AlertType.ERROR, "删除失败").show();
                    });
                }).start();
            }
        });
    }
}